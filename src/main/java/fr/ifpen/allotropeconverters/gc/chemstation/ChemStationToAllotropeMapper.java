package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.allotrope_models.*;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.ChannelKey;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.CompoundPeak;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ChFileResolver;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ResultXmlReader;
import fr.ifpen.allotropeconverters.gc.chemstation.mapping.*;
import fr.ifpen.allotropeconverters.gc.chemstation.service.PeakAssociationService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;


public class ChemStationToAllotropeMapper {

    private final ResultXmlReader resultXmlReader;
    private final ChFileResolver chFileResolver;
    private final DeviceSystemDocumentMapper deviceSystemDocumentMapper;
    private final MeasurementDocumentMapper measurementDocumentMapper;
    private final PeakAssociationService peakAssociationService;
    private final String acqTxtFilename;
    private final String resultXmlFilename;

    public ChemStationToAllotropeMapper(ResultXmlReader resultXmlReader,
                                                ChFileResolver chFileResolver,
                                                DeviceSystemDocumentMapper deviceSystemDocumentMapper,
                                                MeasurementDocumentMapper measurementDocumentMapper,
                                                PeakAssociationService peakAssociationService,
                                                String acqTxtFilename,
                                                String resultXmlFilename) {
        this.resultXmlReader = resultXmlReader;
        this.chFileResolver = chFileResolver;
        this.deviceSystemDocumentMapper = deviceSystemDocumentMapper;
        this.measurementDocumentMapper = measurementDocumentMapper;
        this.peakAssociationService = peakAssociationService;
        this.acqTxtFilename = acqTxtFilename;
        this.resultXmlFilename = resultXmlFilename;
    }


    public GasChromatographySimpleModel fromFolder(Path folderPath) throws IOException {

        ResultXmlReader.ResultData resultData = resultXmlReader.read(folderPath.resolve(resultXmlFilename));

        Map<ChannelKey, ChFile> chFilesByChannel = chFileResolver.resolve(folderPath, resultData);

        Map<String, Map<Double, CompoundPeak>> compoundIndex =
                peakAssociationService.indexCompoundsBySignalAndRt(resultData.compounds());

        DeviceSystemDocument deviceSystem = deviceSystemDocumentMapper.toDeviceSystemDocument(resultData);

        GasChromatographyDocument gc = new GasChromatographyDocument();
        gc.setAnalyst(nonBlank(resultData.analystFromXml()));
        gc.setSubmitter(gc.getAnalyst());
        gc.setDeviceMethodIdentifier(nonBlank(resultData.methodFromXml()));

        List<MeasurementDocument> measurementDocuments = new ArrayList<>();
        for (var signal : resultData.signals()) {
            ChFile chFile = chFilesByChannel.get(signal.channelKey());
            if (chFile == null) continue;

            MeasurementDocument measurement = measurementDocumentMapper.toMeasurementDocumentForSignal(
                    signal, resultData ,chFile, compoundIndex, folderPath.resolve(acqTxtFilename));

            measurementDocuments.add(measurement);
        }

        MeasurementAggregateDocument measurementAggregate = new MeasurementAggregateDocument();
        measurementAggregate.setMeasurementDocument(measurementDocuments);
        gc.setMeasurementAggregateDocument(measurementAggregate);

        GasChromatographyAggregateDocument aggregate = new GasChromatographyAggregateDocument();
        aggregate.setDeviceSystemDocument(deviceSystem);
        aggregate.setGasChromatographyDocument(List.of(gc));

        GasChromatographySimpleModel model = new GasChromatographySimpleModel();
        model.setGasChromatographyAggregateDocument(aggregate);
        return model;
    }

    public GasChromatographySimpleModel fromChFile(Path chFilePath) throws IOException {
        ChFile chFile = chFileResolver.resolve(chFilePath);

        GasChromatographyDocument gasChromatographyDocument = new GasChromatographyDocument();
        gasChromatographyDocument.setAnalyst(chFile.getOperator());
        gasChromatographyDocument.setSubmitter(chFile.getOperator());
        gasChromatographyDocument.setDeviceMethodIdentifier(chFile.getMethod());

        MeasurementDocument measurementDocument = measurementDocumentMapper.createMeasurementDocument(chFile);

        MeasurementAggregateDocument measurementAggregateDocument = new MeasurementAggregateDocument();
        measurementAggregateDocument.setMeasurementDocument(List.of(measurementDocument));

        gasChromatographyDocument.setMeasurementAggregateDocument(measurementAggregateDocument);

        DeviceSystemDocument deviceSystemDocument =
                deviceSystemDocumentMapper.getDefaultDeviceSystemDocument();

        GasChromatographyAggregateDocument gasChromatographyAggregateDocument = new GasChromatographyAggregateDocument();
        gasChromatographyAggregateDocument.setGasChromatographyDocument(List.of(gasChromatographyDocument));
        gasChromatographyAggregateDocument.setDeviceSystemDocument(deviceSystemDocument);

        GasChromatographySimpleModel gasChromatographySimpleModel = new GasChromatographySimpleModel();
        gasChromatographySimpleModel.setGasChromatographyAggregateDocument(gasChromatographyAggregateDocument);

        return  gasChromatographySimpleModel;
    }

    private static String nonBlank(String s) { return (s == null || s.isBlank()) ? "" : s; }
}
