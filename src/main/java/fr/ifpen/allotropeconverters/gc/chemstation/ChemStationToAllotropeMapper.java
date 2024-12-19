package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFileFactory;
import fr.ifpen.allotropeconverters.gc.schema.ChromatographyColumnDocument;
import fr.ifpen.allotropeconverters.gc.schema.DetectorControlAggregateDocument;
import fr.ifpen.allotropeconverters.gc.schema.DetectorControlDocument;
import fr.ifpen.allotropeconverters.gc.schema.DeviceSystemDocument;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyAggregateDocument;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyDocument;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyTabularEmbedSchema;
import fr.ifpen.allotropeconverters.gc.schema.InjectionDocument;
import fr.ifpen.allotropeconverters.gc.schema.InjectionVolumeSetting;
import fr.ifpen.allotropeconverters.gc.schema.MeasurementAggregateDocument;
import fr.ifpen.allotropeconverters.gc.schema.MeasurementDocument;
import fr.ifpen.allotropeconverters.gc.schema.Peak;
import fr.ifpen.allotropeconverters.gc.schema.PeakList;
import fr.ifpen.allotropeconverters.gc.schema.SampleDocument;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Maps raw ChemStation gas chromatography data files (.ch, .xml, .txt) into structured Allotrope-format
 * documents such as GasChromatographyTabularEmbedSchema.
 * <p>
 * This class acts as a bridge between ChemStation data and Allotrope models, handling parsing, data extraction,
 * transformation, and mapping processes.
 */
public class ChemStationToAllotropeMapper {

    private final PeakMapper peakMapper;
    private final ColumnInformationMapper columnInformationMapper;
    private final ChromatogramDataCubeMapper chromatogramDataCubeMapper;
    private final ZoneId timeZone;
    private final List<DateTimeFormatter> dateTimeFormatters;
    private final String chFileName;
    private final String xmlFileName;
    private final String txtFileName;
    private final MergeStrategy mergeStrategy;

    ChemStationToAllotropeMapper(ZoneId timeZone, List<DateTimeFormatter> dateTimeFormatters, String chFileName, String xmlFileName,
                                 String txtFileName, MergeStrategy mergeStrategy) {
        this.peakMapper = new PeakMapper();
        this.columnInformationMapper = new ColumnInformationMapper();
        this.chromatogramDataCubeMapper = new ChromatogramDataCubeMapper();

        this.timeZone = timeZone;
        this.dateTimeFormatters = dateTimeFormatters;
        this.chFileName = chFileName;
        this.xmlFileName = xmlFileName;
        this.txtFileName = txtFileName;
        this.mergeStrategy = mergeStrategy;
    }

    private static String getDetectorType(String detectorRawType) {
        if (detectorRawType.toLowerCase().contains("fid")) {
            return "Flame Ionization";
        } else {
            return "Unknown";
        }
    }

    /**
     * Creates an instance of GasChromatographyTabularEmbedSchema by reading data from the specified folder.
     * Parses .ch, .xml and .txt files, maps the necessary fields, and constructs a schema to represent gas chromatography data.
     *
     * @param folderPath
     *         the file path to the folder containing the input data files
     *
     * @return a GasChromatographyTabularEmbedSchema populated with the gas chromatography data from the specified folder
     *
     * @throws JAXBException
     *         if there is an error during XML parsing
     * @throws IOException
     *         if there is an error accessing or reading the required files
     */
    public GasChromatographyTabularEmbedSchema fromFolder(String folderPath) throws JAXBException, IOException {
        ChemStationResult chemStationResult = parseXmlResult(folderPath);
        ChFile chFile = getChFile(folderPath);

        GasChromatographyTabularEmbedSchema schema = new GasChromatographyTabularEmbedSchema();
        GasChromatographyAggregateDocument document = new GasChromatographyAggregateDocument();

        DeviceSystemDocument deviceSystemDocument = new DeviceSystemDocument();
        deviceSystemDocument.setAssetManagementIdentifier(((Element) chemStationResult.acquisition.instrumentName).getTextContent());

        GasChromatographyDocument gasChromatographyDocument = new GasChromatographyDocument();
        applyValue(gasChromatographyDocument::setAnalyst, chFile.getOperator(),
                   ((Element) chemStationResult.sampleInformation.operator).getTextContent());
        applyValue(gasChromatographyDocument::setSubmitter, chFile.getOperator(),
                   ((Element) chemStationResult.sampleInformation.operator).getTextContent());
        applyValue(gasChromatographyDocument::setDeviceMethodIdentifier, chFile.getMethod(),
                   ((Element) chemStationResult.sampleInformation.method).getTextContent());

        ChromatographyColumnDocument chromatographyColumnDocument =
                columnInformationMapper.readColumnDocumentFromFile(folderPath, txtFileName);
        gasChromatographyDocument.setChromatographyColumnDocument(chromatographyColumnDocument);

        DetectorControlAggregateDocument detectorControlAggregateDocument = new DetectorControlAggregateDocument();
        DetectorControlDocument detectorControlDocument = new DetectorControlDocument();
        detectorControlDocument.setDetectionType(
                getDetectorType(((Element) chemStationResult.chromatograms.signal.get(0).detector).getTextContent()));
        detectorControlAggregateDocument.setDetectorControlDocument(List.of(detectorControlDocument));
        gasChromatographyDocument.setDetectorControlAggregateDocument(detectorControlAggregateDocument);

        SampleDocument sampleDocument = new SampleDocument();
        applyValue(sampleDocument::setSampleIdentifier, chFile.getSampleName(),
                   ((Element) chemStationResult.sampleInformation.sampleName).getTextContent());
        applyValue(sampleDocument::setWrittenName, chFile.getSampleName(),
                   ((Element) chemStationResult.sampleInformation.sampleName).getTextContent());
        sampleDocument.setDescription(((Element) chemStationResult.sampleInformation.sampleInfo).getTextContent());
        gasChromatographyDocument.setSampleDocument(sampleDocument);

        InjectionDocument injectionDocument = new InjectionDocument();
        applyValue(injectionDocument::setInjectionTime, getInstant(chFile.getInjectionDateTime()),
                   getInstant(((Element) chemStationResult.sampleInformation.injectionDateTime).getTextContent()));
        injectionDocument.setInjectionIdentifier(((Element) chemStationResult.sampleInformation.inj).getTextContent());

        InjectionVolumeSetting injectionVolumeSetting = new InjectionVolumeSetting();
        injectionVolumeSetting.setValue(Double.parseDouble(((Element) chemStationResult.sampleInformation.inj).getTextContent()));
        injectionVolumeSetting.setUnit("μL");
        injectionDocument.setInjectionVolumeSetting(injectionVolumeSetting);

        gasChromatographyDocument.setInjectionDocument(injectionDocument);

        MeasurementAggregateDocument measurementAggregateDocument = new MeasurementAggregateDocument();
        MeasurementDocument measurementDocument = new MeasurementDocument();
        measurementDocument.setDetectionType(((Element) chemStationResult.chromatograms.signal.get(0).detector).getTextContent());
        measurementDocument.setChromatogramDataCube(chromatogramDataCubeMapper.readChromatogramDataCube(chFile));

        List<Peak> peaks = new ArrayList<>();
        for (CompoundType compoundType : chemStationResult.results.resultsGroup.get(0).peak) {
            peaks.add(peakMapper.mapPeakFromCompound(compoundType));
        }
        PeakList peakList = new PeakList();
        peakList.setPeak(peaks);
        measurementDocument.setPeakList(peakList);

        measurementAggregateDocument.setMeasurementDocument(List.of(measurementDocument));
        gasChromatographyDocument.setMeasurementAggregateDocument(measurementAggregateDocument);

        document.setDeviceSystemDocument(deviceSystemDocument);
        document.setGasChromatographyDocument(List.of(gasChromatographyDocument));

        schema.setGasChromatographyAggregateDocument(document);
        return schema;
    }

    /**
     * Creates an instance of GasChromatographyTabularEmbedSchema by reading data from the .ch file inside the specified folder.
     * Parses .ch file, maps the necessary fields, and constructs a schema to represent gas chromatography data.
     * <p>
     * <strong>Warning: Generating an Allotrope JSON from a single .ch file results in some information not being available.</strong>
     * Some of these unavailable information are:
     * <ul>
     *     <li>Peak list</li>
     *     <li>Sample description</li>
     * </ul>
     *
     * @param folderPath
     *         the file path to the folder containing the .ch file
     *
     * @return a GasChromatographyTabularEmbedSchema populated with the gas chromatography data from the specified .ch file
     *
     * @throws IOException
     *         if there is an error accessing or reading the required file
     */
    public GasChromatographyTabularEmbedSchema fromChFile(String folderPath) throws IOException {
        ChFile chFile = getChFile(folderPath);

        GasChromatographyDocument gasChromatographyDocument = new GasChromatographyDocument();
        gasChromatographyDocument.setAnalyst(chFile.getOperator());
        gasChromatographyDocument.setSubmitter(chFile.getOperator());
        gasChromatographyDocument.setDeviceMethodIdentifier(chFile.getMethod());

        ChromatographyColumnDocument chromatographyColumnDocument = new ChromatographyColumnDocument();
        chromatographyColumnDocument.setChromatographyColumnSerialNumber("");
        gasChromatographyDocument.setChromatographyColumnDocument(chromatographyColumnDocument);

        DetectorControlAggregateDocument detectorControlAggregateDocument = new DetectorControlAggregateDocument();
        DetectorControlDocument detectorControlDocument = new DetectorControlDocument();
        detectorControlDocument.setDetectionType("");
        detectorControlAggregateDocument.setDetectorControlDocument(List.of(detectorControlDocument));
        gasChromatographyDocument.setDetectorControlAggregateDocument(detectorControlAggregateDocument);

        SampleDocument sampleDocument = new SampleDocument();
        sampleDocument.setSampleIdentifier(chFile.getSampleName());
        sampleDocument.setWrittenName(chFile.getSampleName());
        gasChromatographyDocument.setSampleDocument(sampleDocument);

        InjectionDocument injectionDocument = new InjectionDocument();
        InjectionVolumeSetting injectionVolumeSetting = new InjectionVolumeSetting();
        injectionVolumeSetting.setValue(Double.NaN);
        injectionVolumeSetting.setUnit("μL");
        injectionDocument.setInjectionVolumeSetting(injectionVolumeSetting);
        injectionDocument.setInjectionIdentifier("");

        injectionDocument.setInjectionTime(getInstant(chFile.getInjectionDateTime()));
        gasChromatographyDocument.setInjectionDocument(injectionDocument);

        MeasurementDocument measurementDocument = new MeasurementDocument();
        measurementDocument.setDetectionType("");
        measurementDocument.setChromatogramDataCube(chromatogramDataCubeMapper.readChromatogramDataCube(chFile));

        PeakList peakList = new PeakList();
        peakList.setPeak(Collections.emptyList());
        measurementDocument.setPeakList(peakList);

        MeasurementAggregateDocument measurementAggregateDocument = new MeasurementAggregateDocument();
        measurementAggregateDocument.setMeasurementDocument(List.of(measurementDocument));
        gasChromatographyDocument.setMeasurementAggregateDocument(measurementAggregateDocument);

        GasChromatographyAggregateDocument document = new GasChromatographyAggregateDocument();
        DeviceSystemDocument deviceSystemDocument = new DeviceSystemDocument();
        deviceSystemDocument.setAssetManagementIdentifier("");
        document.setDeviceSystemDocument(deviceSystemDocument);
        document.setGasChromatographyDocument(List.of(gasChromatographyDocument));

        GasChromatographyTabularEmbedSchema schema = new GasChromatographyTabularEmbedSchema();
        schema.setGasChromatographyAggregateDocument(document);

        return schema;
    }

    private ChemStationResult parseXmlResult(String folderPath) throws JAXBException {
        File file = new File(folderPath, xmlFileName);

        JAXBContext jaxbContext = JAXBContext.newInstance(ChemStationResult.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbUnmarshaller.setEventHandler(new jakarta.xml.bind.helpers.DefaultValidationEventHandler());

        return (ChemStationResult) jaxbUnmarshaller.unmarshal(file);
    }

    private ChFile getChFile(String folderPath) throws IOException {
        ChFileFactory chFileFactory = new ChFileFactory();
        return chFileFactory.getChFile(new File(folderPath, chFileName).getPath());
    }

    private Instant getInstant(String dateTimeString) {
        LocalDateTime parse = null;
        for (DateTimeFormatter formatter : dateTimeFormatters) {
            try {
                parse = LocalDateTime.parse(dateTimeString, formatter);
            } catch (DateTimeParseException e) {
                // Do nothing
            }
        }

        if (parse == null) {
            throw new IllegalStateException("Injection date has a unknown format. Original string is: '" + dateTimeString + "'");
        }

        return parse.atZone(timeZone).toInstant();
    }

    private void applyValue(Consumer<Object> method, Object chFileValue, Object otherFileValue) {
        if (Objects.equals(chFileValue, otherFileValue)) {
            method.accept(chFileValue);
            return;
        }

        switch (mergeStrategy) {
            case USE_CH_FILE -> method.accept(chFileValue);
            case USE_OTHER_FILES -> method.accept(otherFileValue);
            case ERROR -> throw new IllegalStateException("Different values: " + chFileValue + " and " + otherFileValue);
        }
    }

    /**
     * Represents the merge strategy to use when different values are read for the same field.
     */
    public enum MergeStrategy {
        /**
         * Use the value found in the .ch file.
         */
        USE_CH_FILE,
        /**
         * Use the value found in files other than the .ch file.
         */
        USE_OTHER_FILES,
        /**
         * Raise an error.
         */
        ERROR
    }
}
