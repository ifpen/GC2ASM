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
    private final DeviceControlAggregateDocumentMapper deviceControlAggregateDocumentMapper;
    private final SampleDocumentMapper sampleDocumentMapper;
    private final InjectionDocumentMapper injectionDocumentMapper;
    private final MeasurementDocumentMapper measurementDocumentMapper;
    private final PeakAssociationService peakAssociationService;
    private final String acqTxtFilename;
    private final String resultXmlFilename;

    public ChemStationToAllotropeMapper(ResultXmlReader resultXmlReader,
                                                ChFileResolver chFileResolver,
                                                DeviceSystemDocumentMapper deviceSystemDocumentMapper,
                                                DeviceControlAggregateDocumentMapper deviceControlAggregateDocumentMapper,
                                                SampleDocumentMapper sampleDocumentMapper,
                                                InjectionDocumentMapper injectionDocumentMapper,
                                                MeasurementDocumentMapper measurementDocumentMapper,
                                                PeakAssociationService peakAssociationService,
                                                String acqTxtFilename,
                                                String resultXmlFilename) {
        this.resultXmlReader = resultXmlReader;
        this.chFileResolver = chFileResolver;
        this.deviceSystemDocumentMapper = deviceSystemDocumentMapper;
        this.deviceControlAggregateDocumentMapper = deviceControlAggregateDocumentMapper;
        this.sampleDocumentMapper = sampleDocumentMapper;
        this.injectionDocumentMapper = injectionDocumentMapper;
        this.measurementDocumentMapper = measurementDocumentMapper;
        this.peakAssociationService = peakAssociationService;
        this.acqTxtFilename = acqTxtFilename;
        this.resultXmlFilename = resultXmlFilename;
    }


    public GasChromatographySimpleModel fromFolder(Path folderPath) throws IOException {

        ResultXmlReader.ResultData resultData = resultXmlReader.read(folderPath, resultXmlFilename);


        Map<ChannelKey, ChFile> chFilesByChannel = chFileResolver.resolve(folderPath, resultData);


        Map<String, Map<String, CompoundPeak>> compoundIndex =
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
                    signal, resultData, chFile, compoundIndex, acqTxtFilename, folderPath);


            SampleDocument sample = sampleDocumentMapper.toSampleDocument(resultData, chFile.getSampleName());
            measurement.setSampleDocument(sample);


            InjectionDocument injection = injectionDocumentMapper.toInjectionDocument(resultData);
            measurement.setInjectionDocument(injection);


            measurement.setDeviceControlAggregateDocument(
                    deviceControlAggregateDocumentMapper.toDeviceControlAggregateDocument(signal));

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

    private static String nonBlank(String s) { return (s == null || s.isBlank()) ? "" : s; }
}
