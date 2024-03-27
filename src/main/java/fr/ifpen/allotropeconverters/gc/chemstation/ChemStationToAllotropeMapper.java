package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFileFactory;
import fr.ifpen.allotropeconverters.gc.schema.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ChemStationToAllotropeMapper {
    private final PeakMapper peakMapper;
    private final ColumnInformationMapper columnInformationMapper;
    private final ChromatogramDataCubeMapper chromatogramDataCubeMapper;
    private final ZoneId timeZone;

    public ChemStationToAllotropeMapper(ZoneId timeZone) {
        this.timeZone = timeZone;
        this.peakMapper = new PeakMapper();
        this.columnInformationMapper = new ColumnInformationMapper();
        this.chromatogramDataCubeMapper = new ChromatogramDataCubeMapper();
    }

    public GasChromatographyTabularEmbedSchema mapToGasChromatographySchema(
            String folderPath) throws IOException {

        ChemStationResult chemStationResult = parseXmlResult(folderPath);

        if(chemStationResult != null){
            return createSchemaFromXml(chemStationResult, folderPath);
        } else {
            return createSchemaFromChFile(folderPath);
        }
    }

    private GasChromatographyTabularEmbedSchema createSchemaFromXml(ChemStationResult chemStationResult, String folderPath) throws IOException {
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
                chromatogramDataCubeMapper.readChromatogramDataCube(new File(folderPath,getChFileName(folderPath)).getPath())
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

    private GasChromatographyTabularEmbedSchema createSchemaFromChFile(String folderPath) throws IOException {
        ChFileFactory chFileFactory = new ChFileFactory();
        ChFile chFile = chFileFactory.getChFile(new File(folderPath,getChFileName(folderPath)).getPath());

        GasChromatographyTabularEmbedSchema schema = new GasChromatographyTabularEmbedSchema();
        GasChromatographyAggregateDocument document = new GasChromatographyAggregateDocument();


        DeviceSystemDocument deviceSystemDocument = new DeviceSystemDocument();
        deviceSystemDocument.setAssetManagementIdentifier(chFile.getDetector());

        GasChromatographyDocument gasChromatographyDocument = new GasChromatographyDocument();
        gasChromatographyDocument.setAnalyst(chFile.getAnalyst());
        gasChromatographyDocument.setSubmitter(chFile.getAnalyst());
        gasChromatographyDocument.setDeviceMethodIdentifier(chFile.getMethod());

        SampleDocument sampleDocument = new SampleDocument();
        sampleDocument.setSampleIdentifier(chFile.getSampleName());
        sampleDocument.setWrittenName(chFile.getSampleName());
        gasChromatographyDocument.setSampleDocument(sampleDocument);

        InjectionDocument injectionDocument = new InjectionDocument();
        injectionDocument.setInjectionTime(LocalDateTime.parse(
                        chFile.getInjectionTime().RawTime().trim(),
                        DateTimeFormatter.ofPattern(chFile.getInjectionTime().Format(), Locale.US))
                .atZone(timeZone).toInstant());

        gasChromatographyDocument.setInjectionDocument(injectionDocument);



        MeasurementAggregateDocument measurementAggregateDocument = new MeasurementAggregateDocument();
        MeasurementDocument measurementDocument = new MeasurementDocument();
        measurementDocument.setChromatogramDataCube(
                chromatogramDataCubeMapper.readChromatogramDataCube(new File(folderPath,getChFileName(folderPath)).getPath())
        );

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

    private ChemStationResult parseXmlResult(String folderPath) {
        final String resultFileName = "Result.xml";

        File file = new File(folderPath, resultFileName);

        if (file.exists()){
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(ChemStationResult.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                jaxbUnmarshaller.setEventHandler(new jakarta.xml.bind.helpers.DefaultValidationEventHandler());

                return (ChemStationResult) jaxbUnmarshaller.unmarshal(file);
            } catch (JAXBException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private String getChFileName(String folderPath){
        File dir = new File(folderPath);
        if ( dir.isDirectory() )
        {
            String[] list = dir.list(new FilenameFilter()
            {
                @Override
                public boolean accept(File f, String s )
                {
                    return s.toLowerCase().endsWith(".ch");
                }
            });

            if ( list.length > 0 )
            {
                return list[0];
            }
        }
        return null;
    }
}
