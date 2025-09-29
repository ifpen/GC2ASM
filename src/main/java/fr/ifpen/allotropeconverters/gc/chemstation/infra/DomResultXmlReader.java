package fr.ifpen.allotropeconverters.gc.chemstation.infra;

import fr.ifpen.allotropeconverters.gc.chemstation.domain.ChannelKey;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.CompoundPeak;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.IntegrationRow;
import fr.ifpen.allotropeconverters.gc.chemstation.utils.ChemstationDateResolver;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static fr.ifpen.allotropeconverters.gc.chemstation.utils.ChemstationDateResolver.DEFAULT_DATE_TIME_FORMATTERS;

/**
 * Lecteur typé de Result.xml (SRP : parsing uniquement).
 * - Injection: identifiant (inj), volume (InjVolume), date (convertie en OffsetDateTime avec timeZone).
 * - Signals: description, détecteur, lignes d’intégration (liste d’IntegrationResultsType).
 * - Compounds: projection légère des pics (SignalDesc upper + MeasRetTime canonisé + pointeur JAXB).
 */
public class DomResultXmlReader implements ResultXmlReader {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneOffset.UTC;

    private final ZoneId timeZone;
    private final List<DateTimeFormatter> dateTimeFormatters;

    public DomResultXmlReader() {
        this(DEFAULT_ZONE_ID);
    }

    public DomResultXmlReader(ZoneId timeZone) {
        this(timeZone, DEFAULT_DATE_TIME_FORMATTERS);
    }

    public DomResultXmlReader(ZoneId timeZone, List<DateTimeFormatter> dateTimeFormatters) {
        this.timeZone = Objects.requireNonNull(timeZone, "timeZone must not be null");
        this.dateTimeFormatters = Objects.requireNonNullElseGet(dateTimeFormatters, List::of);
    }

    @Override
    public ResultData read(Path resultXmlFile) {
        ChemStationResult root = unmarshal(resultXmlFile);

        // --------- Métadonnées instrument / échantillon ---------
        String instrumentName = root.getAcquisition() != null ? root.getAcquisition().getInstrumentName() : "";
        String analystFromXml = textOf(root.sampleInformation != null ? root.sampleInformation.operator : null);
        String methodFromXml  = root.getSampleInformation() != null ? root.getSampleInformation().getMethod() : "";
        String sampleName     = root.getSampleInformation() != null ? root.getSampleInformation().getSampleName() : "";
        String sampleDescription = textOf(root.sampleInformation != null ? root.sampleInformation.sampleInfo : null);

        // --------- Injection ---------
        String injectionIdentifier = textOf(root.sampleInformation != null ? root.sampleInformation.inj : null);
        String injectionVolumeText = textOf(root.sampleInformation != null ? root.sampleInformation.injVolume : null);
        String injectionDateText   = root.getSampleInformation() != null ? root.getSampleInformation().getInjectionDateTime() : null;
        OffsetDateTime injectionDateTime = parseInjectionDateTime(injectionDateText);

        // --------- Signals + Integration Results (typé) ---------
        List<SignalRecord> signalRecords = new ArrayList<>();
        if (root.chromatograms != null && root.chromatograms.signal != null) {
            for (ChemStationResult.Chromatograms.Signal signal : root.chromatograms.signal) {
                String detectorLabel      = textOf(signal.detector);     // ex: "FID1"
                String signalIdentifier   = signal.signalId;             // ex: "A"
                String signalDescription  = textOf(signal.description);  // ex: "FID1 A, Front Signal"
                ChannelKey channelKey     = ChannelKey.of(detectorLabel, signalIdentifier);

                List<IntegrationRow> integrationRows = readTypedIntegrationRows(signal, signalDescription);

                signalRecords.add(new SignalRecord(
                        channelKey,
                        detectorLabel,
                        signalIdentifier,
                        signalDescription,
                        integrationRows
                ));
            }
        }

        // --------- Compounds (resultsGroup unique) ---------
        List<CompoundPeak> compoundPeaks = readCompoundPeaks(root);

        return new ResultData(
                instrumentName,
                analystFromXml,
                methodFromXml,
                sampleName,
                sampleDescription,
                injectionDateTime,
                injectionIdentifier,
                injectionVolumeText,
                signalRecords,
                compoundPeaks
        );
    }

    // =================================================================================================
    // Unmarshal JAXB
    // =================================================================================================

    private ChemStationResult unmarshal(Path xmlPath) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ChemStationResult.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new jakarta.xml.bind.helpers.DefaultValidationEventHandler());
            return (ChemStationResult) unmarshaller.unmarshal(xmlPath.toFile());
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to parse Result.xml at: " + xmlPath, e);
        }
    }

    // =================================================================================================
    // Integration Results (typé : IntegrationResultsType)
    // =================================================================================================

    /**
     * Lit les lignes d’intégration pour un signal.
     * `signal.integrationresults` peut être un IntegrationResultsType ou une List<IntegrationResultsType>.
     */
    private List<IntegrationRow> readTypedIntegrationRows(ChemStationResult.Chromatograms.Signal signal,
                                                          String fallbackSignalDescription) {
        List<IntegrationRow> rows = new ArrayList<>();
        if (signal == null || signal.integrationResults == null) {
            return rows;
        }


        for (Object item : signal.integrationResults) {
            if (item instanceof IntegrationResultsType integrationResult) {
                IntegrationRow row = toIntegrationRow(integrationResult, fallbackSignalDescription);
                if (row != null) rows.add(row);
            }
        }

        return rows;
    }

    /**
     * Convertit un IntegrationResultsType (une ligne) en IntegrationRow (notre projection métier).
     * On récupère : RetTime, Area, Height, Width (les autres champs existent mais ne sont pas nécessaires ici).
     */
    private IntegrationRow toIntegrationRow(IntegrationResultsType integrationResult,
                                            String fallbackSignalDescription) {

        Double retentionTimeMinutes = toDoubleValue(integrationResult.getRetTime().getContent());
        Double areaValue            = toDoubleValue(integrationResult.getArea().getContent());
        Double heightValue          = toDoubleValue(integrationResult.getHeight().getContent());
        Double widthValue           = toDoubleValue(integrationResult.getWidth().getContent());
        Double peakStart            = toDoubleValue(integrationResult.getTimeStart().getContent());
        Double peakEnd              = toDoubleValue(integrationResult.getTimeEnd().getContent());
        Double relativeHeight       = toDoubleValue(integrationResult.getHeightPercent().getContent());
        Double relativeArea         = toDoubleValue(integrationResult.getAreaPercent().getContent());
        Double symmetry             = toDoubleValue(integrationResult.getSymmetry().getContent());

        // Pas de temps → on ne peut pas faire la jointure (SignalDesc + RetTime)
        if (retentionTimeMinutes == null) return null;

        // Le SignalDesc côté « intégration » n’est pas présent ici : on réutilise la description du signal
        String signalDescription = fallbackSignalDescription != null ? fallbackSignalDescription : "";

        return new IntegrationRow(
               signalDescription,
               retentionTimeMinutes,
               areaValue,
               heightValue,
               widthValue,
                peakStart,
                peakEnd,
                relativeHeight,
                relativeArea,
                symmetry);
    }

    /**
     * Extrait un double à partir d’un champ de type ‘Value’ JAXB ou primitif.
     * Gère: Number, String, DOM Element (texte), et les types JAXB 'Value' avec getValue() retournant Number/String.
     */
    private static Double toDoubleValue(Object rawValue) {
        if (rawValue == null) return null;

        // Directement numérique
        if (rawValue instanceof Number number) {
            return number.doubleValue();
        }

        // String à parser
        if (rawValue instanceof String text) {
            return parseDoubleOrNull(text);
        }

        // DOM Element (texte à parser)
        if (rawValue instanceof Element element) {
            return parseDoubleOrNull(element.getTextContent());
        }

        // Type JAXB "Value" : essaye getValue()
        try {
            var method = rawValue.getClass().getMethod("getValue");
            Object inner = method.invoke(rawValue);
            if (inner instanceof Number n) return n.doubleValue();
            if (inner instanceof String s) return parseDoubleOrNull(s);
        } catch (ReflectiveOperationException ignored) {
            // on ne casse pas : on retentera en toString()
        }

        // Dernier recours
        return parseDoubleOrNull(String.valueOf(rawValue));
    }

    // =================================================================================================
    // Compounds (resultsGroup unique)
    // =================================================================================================

    private List<CompoundPeak> readCompoundPeaks(ChemStationResult root) {
        List<CompoundPeak> compoundPeaks = new ArrayList<>();
        if (root == null || root.results == null || root.results.resultsGroup == null || root.results.resultsGroup.isEmpty()) {
            return compoundPeaks;
        }
        var resultsGroup = root.results.resultsGroup.get(0);
        if (resultsGroup.peak == null) return compoundPeaks;

        for (CompoundType compound : resultsGroup.peak) {
            String signalDescription = compound.getSignalDesc() != null
                    ? compound.getSignalDesc()
                    : textOf(compound.signalDesc);
            String signalDescriptionUpper = signalDescription == null ? "" : signalDescription.trim().toUpperCase();

            compoundPeaks.add(new CompoundPeak(signalDescriptionUpper, toDoubleValue(compound.measRetTime.getContent()), compound));
        }
        return compoundPeaks;
    }

    // =================================================================================================
    // Helpers parsing / canonisation / date
    // =================================================================================================

    private static String textOf(Object xmlField) {
        if (xmlField == null) return null;
        if (xmlField instanceof Element element) return element.getTextContent();
        return String.valueOf(xmlField);
    }

    private static Double parseDoubleOrNull(String rawText) {
        if (rawText == null || rawText.isBlank()) return null;
        try {
            return Double.parseDouble(rawText.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private OffsetDateTime parseInjectionDateTime(String dateTimeText) {
        if (dateTimeText == null || dateTimeText.isBlank()) return null;
        LocalDateTime injectionDate = new ChemstationDateResolver(this.dateTimeFormatters).getLocalDateTime(dateTimeText);
        return Optional.ofNullable(injectionDate).map(lcd -> lcd.atZone(timeZone).toOffsetDateTime()).orElse(null);
    }
}
