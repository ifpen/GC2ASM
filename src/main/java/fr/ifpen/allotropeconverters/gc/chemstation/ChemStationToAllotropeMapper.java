package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.schema.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static fr.ifpen.allotropeconverters.gc.chemstation.ChromatogramDataCubeMapper.readChromatogramDataCube;

public class ChemStationToAllotropeMapper {
    private final PeakMapper peakMapper;
    private final ColumnInformationMapper columnInformationMapper;
    private final ZoneId timeZone;

    public ChemStationToAllotropeMapper(ZoneId timeZone) {
        this.timeZone = timeZone;
        this.peakMapper = new PeakMapper();
        this.columnInformationMapper = new ColumnInformationMapper();
    }

    public GasChromatographyTabularEmbedSchema mapToGasChromatographySchema(
            String folderPath) throws JAXBException, IOException {
        ChemStationResult chemStationResult = parseXmlResult(folderPath);

        GasChromatographyTabularEmbedSchema schema = new GasChromatographyTabularEmbedSchema();
        GasChromatographyAggregateDocument document = new GasChromatographyAggregateDocument();

        DeviceSystemDocument deviceSystemDocument = new DeviceSystemDocument();
        deviceSystemDocument.setAssetManagementIdentifier(
                ((Element) chemStationResult.acquisition.instrumentName).getTextContent());

        GasChromatographyDocument gasChromatographyDocument = new GasChromatographyDocument();
        gasChromatographyDocument.setAnalyst(
                ((Element) chemStationResult.sampleInformation.operator).getTextContent());
        gasChromatographyDocument.setSubmitter(
                ((Element) chemStationResult.sampleInformation.operator).getTextContent());
        gasChromatographyDocument.setDeviceMethodIdentifier(
                ((Element) chemStationResult.sampleInformation.method).getTextContent());

        ChromatographyColumnDocument chromatographyColumnDocument =
                columnInformationMapper.readColumnDocumentFromFile(folderPath);
        gasChromatographyDocument.setChromatographyColumnDocument(chromatographyColumnDocument);

        DetectorControlAggregateDocument detectorControlAggregateDocument = new DetectorControlAggregateDocument();
        DetectorControlDocument detectorControlDocument = new DetectorControlDocument();
        detectorControlDocument.setDetectionType(
                getDetectorType(((Element) chemStationResult.chromatograms.signal.get(0).detector).getTextContent()));
        detectorControlAggregateDocument.setDetectorControlDocument(List.of(detectorControlDocument));
        gasChromatographyDocument.setDetectorControlAggregateDocument(detectorControlAggregateDocument);



        SampleDocument sampleDocument = new SampleDocument();
        sampleDocument.setSampleIdentifier(
                ((Element) chemStationResult.sampleInformation.sampleName).getTextContent());
        sampleDocument.setDescription(
                ((Element) chemStationResult.sampleInformation.sampleInfo).getTextContent());
        sampleDocument.setWrittenName(
                ((Element) chemStationResult.sampleInformation.sampleName).getTextContent());
        gasChromatographyDocument.setSampleDocument(sampleDocument);

        InjectionDocument injectionDocument = new InjectionDocument();
        String injectionTimeString = ((Element) chemStationResult.sampleInformation.injectionDateTime).getTextContent();
        injectionDocument.setInjectionTime(
                LocalDateTime.parse(
                        injectionTimeString,
                        DateTimeFormatter.ofPattern("dd-MMM-yy, HH:mm:ss", Locale.US))
                                .atZone(timeZone).toInstant()
        );
        injectionDocument.setInjectionIdentifier(
                ((Element) chemStationResult.sampleInformation.inj).getTextContent());

        InjectionVolumeSetting injectionVolumeSetting = new InjectionVolumeSetting();
        injectionVolumeSetting.setValue(Double.parseDouble(((Element) chemStationResult.sampleInformation.inj).getTextContent()));
        injectionVolumeSetting.setUnit("μL");
        injectionDocument.setInjectionVolumeSetting(injectionVolumeSetting);

        gasChromatographyDocument.setInjectionDocument(injectionDocument);



        MeasurementAggregateDocument measurementAggregateDocument = new MeasurementAggregateDocument();
        MeasurementDocument measurementDocument = new MeasurementDocument();
        measurementDocument.setDetectionType(
                ((Element) chemStationResult.chromatograms.signal.get(0).detector).getTextContent());
        measurementDocument.setChromatogramDataCube(
                readChromatogramDataCube(new File(folderPath,"FID1A.ch").getPath())
        );

        List<Peak> peaks = new ArrayList<>();
        for(CompoundType compoundType: chemStationResult.results.resultsGroup.get(0).peak){
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

    private String getDetectorType(String detectorRawType){
        if(detectorRawType.toLowerCase().contains("fid")) {
            return "Flame Ionization";
        } else {
            return "Unknown";
        }
    }

    private ChemStationResult parseXmlResult(String folderPath) throws JAXBException {
        final String resultFileName = "Result.xml";

        File file = new File(folderPath, resultFileName);

        JAXBContext jaxbContext   = JAXBContext.newInstance( ChemStationResult.class );
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbUnmarshaller.setEventHandler(new jakarta.xml.bind.helpers.DefaultValidationEventHandler());

        return (ChemStationResult) jaxbUnmarshaller.unmarshal(file);
    }
}
