package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.allotrope_models.*;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFileFactory;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.w3c.dom.Element;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
    private final String chFilename;
    private final String resultXmlFilename;
    private final String acqTxtFilename;
    private final MergeStrategy mergeStrategy;

    ChemStationToAllotropeMapper(ZoneId timeZone, List<DateTimeFormatter> dateTimeFormatters, String chFilename, String resultXmlFilename,
                                 String acqTxtFilename, MergeStrategy mergeStrategy) {
        this.peakMapper = new PeakMapper();
        this.columnInformationMapper = new ColumnInformationMapper();
        this.chromatogramDataCubeMapper = new ChromatogramDataCubeMapper();

        this.timeZone = timeZone;
        this.dateTimeFormatters = dateTimeFormatters;
        this.chFilename = chFilename;
        this.resultXmlFilename = resultXmlFilename;
        this.acqTxtFilename = acqTxtFilename;
        this.mergeStrategy = mergeStrategy;
    }

    private static String getDetectorType(String detectorRawType) {
        if (detectorRawType.toLowerCase().contains("fid")) {
            return "Flame Ionization";
        } else {
            return "Unknown";
        }
    }

    private SampleDocument buildSampleDocument(ChFile chFile, ChemStationResult chemStationResult){
        SampleDocument sampleDocument = new SampleDocument();
        applyValue(sampleDocument::setSampleIdentifier, chFile.getSampleName(), chemStationResult.getSampleInformation().getSampleName());
        sampleDocument.setWrittenName(sampleDocument.getSampleIdentifier());
        sampleDocument.setDescription(((Element) chemStationResult.sampleInformation.sampleInfo).getTextContent());

        return sampleDocument;
    }

    private InjectionDocument buildInjectionDocument(ChFile chFile, ChemStationResult chemStationResult){
        InjectionDocument injectionDocument = new InjectionDocument();
        applyValue(injectionDocument::setInjectionTime, getInjectionDateInstant(chFile.getInjectionDateTime()),
                getInjectionDateInstant(chemStationResult.getSampleInformation().getInjectionDateTime()));
        injectionDocument.setInjectionIdentifier(((Element) chemStationResult.sampleInformation.inj).getTextContent());

        InjectionDocumentInjectionVolumeSetting injectionVolumeSetting =
                new InjectionDocumentInjectionVolumeSetting();
        injectionVolumeSetting.setValue(Double.parseDouble(((Element) chemStationResult.sampleInformation.inj).getTextContent()));
        injectionVolumeSetting.setUnit(InjectionDocumentInjectionVolumeSetting.UnitEnum.Micro_L);
        injectionDocument.setInjectionVolumeSetting(injectionVolumeSetting);

        return injectionDocument;
    }

    private DeviceSystemDocument buildDeviceSystemDocument(ChemStationResult chemStationResult){
        DeviceSystemDocument deviceSystemDocument = new DeviceSystemDocument();
        deviceSystemDocument.setAssetManagementIdentifier(chemStationResult.getAcquisition().getInstrumentName());

        DeviceDocument deviceDocument = new DeviceDocument();
        deviceDocument.setDeviceType(
                getDetectorType(((Element) chemStationResult.chromatograms.signal.get(0).detector).getTextContent()));

        deviceSystemDocument.setDeviceDocument(List.of(deviceDocument));
        return deviceSystemDocument;
    }

    private ProcessedDataAggregateDocument buildProcessedDataAggregateDocument(ChemStationResult chemStationResult){
        ProcessedDataAggregateDocument processedDataAggregateDocument = new ProcessedDataAggregateDocument();
        ProcessedDataDocument processedDataDocument = new ProcessedDataDocument();

        List<Peak> peaks = new ArrayList<>();
        for (CompoundType compoundType : chemStationResult.results.resultsGroup.get(0).peak) {
            peaks.add(peakMapper.mapPeakFromCompound(compoundType));
        }
        PeakList peakList = new PeakList();
        peakList.setPeak(peaks);
        processedDataDocument.setPeakList(peakList);

        processedDataAggregateDocument.setProcessedDataDocument(List.of(processedDataDocument));

        return processedDataAggregateDocument;
    }

    private DeviceControlAggregateDocument buildDeviceControlAggregateDocument(ChemStationResult chemStationResult){
        DeviceControlDocument deviceControlDocument = new DeviceControlDocument();

        deviceControlDocument.setDeviceType(
                getDetectorType(((Element) chemStationResult.chromatograms.signal.get(0).detector).getTextContent()));

        DeviceControlAggregateDocument deviceControlAggregateDocument = new DeviceControlAggregateDocument();
        deviceControlAggregateDocument.setDeviceControlDocument(List.of(deviceControlDocument));

        return deviceControlAggregateDocument;
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
    public GasChromatographySimpleModel fromFolder(Path folderPath) throws JAXBException, IOException {
        ChemStationResult chemStationResult = parseXmlResultFromFolder(folderPath);
        ChFile chFile = getChFileFromFolder(folderPath);

        GasChromatographySimpleModel schema = new GasChromatographySimpleModel();
        GasChromatographyAggregateDocument document = new GasChromatographyAggregateDocument();


        GasChromatographyDocument gasChromatographyDocument = new GasChromatographyDocument();
        gasChromatographyDocument.setAnalyst(chFile.getOperator());
        applyValue(gasChromatographyDocument::setAnalyst, chFile.getOperator(), ((Element) chemStationResult.sampleInformation.operator).getTextContent());
        gasChromatographyDocument.setSubmitter(gasChromatographyDocument.getAnalyst());
        applyValue(gasChromatographyDocument::setDeviceMethodIdentifier, chFile.getMethod(), chemStationResult.getSampleInformation().getMethod());

        MeasurementAggregateDocument measurementAggregateDocument = new MeasurementAggregateDocument();
        MeasurementDocument measurementDocument = new MeasurementDocument();
        measurementDocument.setDetectionType(((Element) chemStationResult.chromatograms.signal.get(0).detector).getTextContent());
        measurementDocument.setChromatogramDataCube(chromatogramDataCubeMapper.readChromatogramDataCube(chFile));
        measurementDocument.setMeasurementIdentifier("");

        ChromatographyColumnDocument chromatographyColumnDocument =
                columnInformationMapper.readColumnDocumentFromFile(folderPath, acqTxtFilename);
        measurementDocument.setChromatographyColumnDocument(chromatographyColumnDocument);


        measurementDocument.setSampleDocument(buildSampleDocument(chFile, chemStationResult));

        measurementDocument.setInjectionDocument(buildInjectionDocument(chFile, chemStationResult));

        measurementDocument.setProcessedDataAggregateDocument(buildProcessedDataAggregateDocument(chemStationResult));

        measurementDocument.setDeviceControlAggregateDocument(buildDeviceControlAggregateDocument(chemStationResult));

        measurementAggregateDocument.setMeasurementDocument(List.of(measurementDocument));

        gasChromatographyDocument.setMeasurementAggregateDocument(measurementAggregateDocument);

        document.setDeviceSystemDocument(buildDeviceSystemDocument(chemStationResult));
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
     * @param chFilePath
     *         the file path to the .ch file
     *
     * @return a GasChromatographyTabularEmbedSchema populated with the gas chromatography data from the specified .ch file
     *
     * @throws IOException
     *         if there is an error accessing or reading the required file
     */
    public GasChromatographySimpleModel fromChFile(Path chFilePath) throws IOException {
        ChFile chFile = getChFile(chFilePath);

        GasChromatographyDocument gasChromatographyDocument = new GasChromatographyDocument();
        gasChromatographyDocument.setAnalyst(chFile.getOperator());
        gasChromatographyDocument.setSubmitter(chFile.getOperator());
        gasChromatographyDocument.setDeviceMethodIdentifier(chFile.getMethod());

        InjectionDocument injectionDocument = new InjectionDocument();
        InjectionDocumentInjectionVolumeSetting injectionVolumeSetting = new InjectionDocumentInjectionVolumeSetting();
        injectionVolumeSetting.setValue(Double.NaN);
        injectionVolumeSetting.setUnit(InjectionDocumentInjectionVolumeSetting.UnitEnum.Micro_L);
        injectionDocument.setInjectionVolumeSetting(injectionVolumeSetting);
        injectionDocument.setInjectionIdentifier("");


        MeasurementDocument measurementDocument = new MeasurementDocument();
        measurementDocument.setDetectionType("");

        injectionDocument.setInjectionTime(getInjectionDateInstant(chFile.getInjectionDateTime()));
        measurementDocument.setInjectionDocument(injectionDocument);


        ChromatographyColumnDocument chromatographyColumnDocument = new ChromatographyColumnDocument();
        chromatographyColumnDocument.setChromatographyColumnSerialNumber("");
        measurementDocument.setChromatographyColumnDocument(chromatographyColumnDocument);

        SampleDocument sampleDocument = new SampleDocument();
        sampleDocument.setSampleIdentifier(chFile.getSampleName());
        sampleDocument.setWrittenName(chFile.getSampleName());
        measurementDocument.setSampleDocument(sampleDocument);

        measurementDocument.setChromatogramDataCube(chromatogramDataCubeMapper.readChromatogramDataCube(chFile));

        PeakList peakList = new PeakList();
        peakList.setPeak(Collections.emptyList());

        ProcessedDataDocument processedDataDocument = new ProcessedDataDocument();
        processedDataDocument.setPeakList(peakList);

        ProcessedDataAggregateDocument processedDataAggregateDocument = new ProcessedDataAggregateDocument();
        processedDataAggregateDocument.setProcessedDataDocument(List.of(processedDataDocument));

        measurementDocument.setProcessedDataAggregateDocument(processedDataAggregateDocument);

        MeasurementAggregateDocument measurementAggregateDocument = new MeasurementAggregateDocument();
        measurementAggregateDocument.setMeasurementDocument(List.of(measurementDocument));
        gasChromatographyDocument.setMeasurementAggregateDocument(measurementAggregateDocument);

        GasChromatographyAggregateDocument document = new GasChromatographyAggregateDocument();
        DeviceSystemDocument deviceSystemDocument = new DeviceSystemDocument();
        deviceSystemDocument.setAssetManagementIdentifier("");
        document.setDeviceSystemDocument(deviceSystemDocument);
        document.setGasChromatographyDocument(List.of(gasChromatographyDocument));

        GasChromatographySimpleModel schema = new GasChromatographySimpleModel();
        schema.setGasChromatographyAggregateDocument(document);

        return schema;
    }

    private ChemStationResult parseXmlResultFromFolder(Path folderPath) throws JAXBException {
        return parseXmlResult(folderPath.resolve(resultXmlFilename));
    }

    /**
     * Creates an instance of ChemStationResult by reading a Result.xml file.
     *
     * @param xmlResultPath
     *         the file path to the Result.xml file
     *
     * @return a ChemStationResult populated with the content of the Result.xml file
     *
     * @throws JAXBException
     *         if there is an error during XML parsing
     */
    public static ChemStationResult parseXmlResult(Path xmlResultPath) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ChemStationResult.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbUnmarshaller.setEventHandler(new jakarta.xml.bind.helpers.DefaultValidationEventHandler());

        return (ChemStationResult) jaxbUnmarshaller.unmarshal(xmlResultPath.toFile());
    }

    private ChFile getChFileFromFolder(Path folderPath) throws IOException {
        return getChFile(folderPath.resolve(chFilename));
    }

    private ChFile getChFile(Path chFilePath) throws IOException {
        return new ChFileFactory().getChFile(chFilePath);
    }

    /**
     * Creates an Instant based on the injection date string provided in the Result.xml file.
     *
     * @param injectionDateString
     *         the injection date string provided in the Result.xml file
     *
     * @return an Instant representing the injection date
     */
    public OffsetDateTime getInjectionDateInstant(String injectionDateString) {
        LocalDateTime injectionDate = getLocalDateTime(injectionDateString);
        if (injectionDate == null) {
            throw new IllegalArgumentException("Injection date has an unknown format. Original string is: '" + injectionDateString + "'");
        }
        return injectionDate.atZone(timeZone).toOffsetDateTime();
    }

    private LocalDateTime getLocalDateTime(String dateTimeString) {
        LocalDateTime parse = null;
        for (DateTimeFormatter formatter : dateTimeFormatters) {
            try {
                parse = LocalDateTime.parse(dateTimeString, formatter);
            } catch (DateTimeParseException e) {
                // Do nothing
            }
        }
        return parse;
    }

    private <T> void applyValue(Consumer<T> method, T chFileValue, T otherFileValue) {
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
