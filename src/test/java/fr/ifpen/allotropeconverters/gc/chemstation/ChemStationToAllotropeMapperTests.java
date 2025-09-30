package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.allotrope_models.*;
import fr.ifpen.allotropeconverters.gc.TestConstants;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

class ChemStationToAllotropeMapperTests {

    private static void assertV179Schema(GasChromatographySimpleModel embedSchema, boolean additionalAssertions) {
        Assertions.assertThat(embedSchema).isNotNull();

        GasChromatographyAggregateDocument gasChromatographyAggregateDocument = embedSchema.getGasChromatographyAggregateDocument();
        Assertions.assertThat(gasChromatographyAggregateDocument).isNotNull();

        List<GasChromatographyDocument> gasChromatographyDocumentList = gasChromatographyAggregateDocument.getGasChromatographyDocument();
        Assertions.assertThat(gasChromatographyDocumentList).hasSize(1);

        GasChromatographyDocument gasChromatographyDocument = gasChromatographyDocumentList.get(0);
        Assertions.assertThat(gasChromatographyDocument.getSubmitter()).isEqualTo("SYSTEM");

        MeasurementDocument measurementDocument =
                gasChromatographyDocument.getMeasurementAggregateDocument().getMeasurementDocument().get(0);

        if (additionalAssertions) {
            Assertions.assertThat(measurementDocument.getChromatographyColumnDocument().getProductManufacturer())
                    .isEqualTo("Agilent");
        }

        SampleDocument sampleDocument = measurementDocument.getSampleDocument();
        Assertions.assertThat(sampleDocument.getSampleIdentifier()).isEqualTo("22-00465-1");

        if (additionalAssertions) {
            Assertions.assertThat(sampleDocument.getDescription())
                    .isEqualTo(
                            "22-00465-1 - E2046501 - DET : 401 - -FORD (TAE 9891)-HUILE -  - delai Lims : 11/02/2022 -Jean Fritz FORTUNE - VQC28002");
        }

        InjectionDocument injectionDocument = measurementDocument.getInjectionDocument();
        Assertions.assertThat(injectionDocument.getInjectionTime().toInstant()).isEqualTo(Instant.parse("2022-05-12T09:24:28Z")); // 12-May-22, 11:24:28

        if (additionalAssertions) {
            Assertions.assertThat(injectionDocument.getInjectionIdentifier()).isEqualTo("1");
            Assertions.assertThat(injectionDocument.getInjectionVolumeSetting().getValue()).isEqualTo(0.5);
        } else {
            Assertions.assertThat(injectionDocument.getInjectionVolumeSetting().getValue()).isNaN();
        }

        Assertions.assertThat(gasChromatographyDocument.getDeviceMethodIdentifier()).isEqualTo("DET401.M");

        List<MeasurementDocument> measurementDocumentList =
                gasChromatographyDocument.getMeasurementAggregateDocument().getMeasurementDocument();
        Assertions.assertThat(measurementDocumentList).hasSize(1);

        DatacubeData data = measurementDocument.getChromatogramDataCube().getDatacubeData();
        Assertions.assertThat(data).isNotNull();

        List<List<Double>> dimensions = data.getDimensions();
        Assertions.assertThat(dimensions).hasSize(1);
        Assertions.assertThat(dimensions.get(0)).hasSize(71840);

        List<List<Double>> measures = data.getMeasures();
        Assertions.assertThat(measures).hasSize(1);
        Assertions.assertThat(measures.get(0)).hasSize(71840);

        ProcessedDataAggregateDocument processedDataAggregateDocument = measurementDocument.getProcessedDataAggregateDocument();
        Assertions.assertThat(processedDataAggregateDocument).isNotNull();
        List<ProcessedDataDocument> processedDataDocumentList = processedDataAggregateDocument.getProcessedDataDocument();
        Assertions.assertThat(processedDataDocumentList).isNotEmpty();
        PeakList peakList = processedDataDocumentList.get(0).getPeakList();

        if (additionalAssertions) {
            Assertions.assertThat(peakList.getPeak()).hasSize(24);
            Assertions.assertThat(peakList.getPeak().get(0).getRetentionTime().getValue()).isEqualTo(2388.01278);
        } else {
            Assertions.assertThat(peakList.getPeak()).isEmpty();
        }

        if (additionalAssertions) {
            Assertions.assertThat(gasChromatographyAggregateDocument.getDeviceSystemDocument().getAssetManagementIdentifier())
                      .isEqualTo("GC65");
        }
    }

    private static void assertV181Schema(GasChromatographySimpleModel embedSchema, boolean additionalAssertions) {
        Assertions.assertThat(embedSchema).isNotNull();

        GasChromatographyAggregateDocument gasChromatographyAggregateDocument = embedSchema.getGasChromatographyAggregateDocument();
        Assertions.assertThat(gasChromatographyAggregateDocument).isNotNull();

        List<GasChromatographyDocument> gasChromatographyDocumentList = gasChromatographyAggregateDocument.getGasChromatographyDocument();
        Assertions.assertThat(gasChromatographyDocumentList).hasSize(1);

        GasChromatographyDocument gasChromatographyDocument = gasChromatographyDocumentList.get(0);
        Assertions.assertThat(gasChromatographyDocument.getSubmitter()).isEqualTo("SYSTEM");

        MeasurementDocument measurementDocument =
                gasChromatographyDocument.getMeasurementAggregateDocument().getMeasurementDocument().get(0);

        if (additionalAssertions) {
            Assertions.assertThat(measurementDocument.getChromatographyColumnDocument().getProductManufacturer())
                    .isEqualTo("");
        }

        SampleDocument sampleDocument = measurementDocument.getSampleDocument();
        Assertions.assertThat(sampleDocument.getSampleIdentifier()).isEqualTo("140+H");

        if (additionalAssertions) {
            Assertions.assertThat(sampleDocument.getDescription())
                    .isEqualTo(
                            "140+ hydrogene");
        }

        InjectionDocument injectionDocument = measurementDocument.getInjectionDocument();
        Assertions.assertThat(injectionDocument.getInjectionTime().toInstant()).isEqualTo(Instant.parse("2022-08-23T10:48:20Z")); // 12-May-22, 11:24:28

        if (additionalAssertions) {
            Assertions.assertThat(injectionDocument.getInjectionIdentifier()).isEqualTo("1");
            Assertions.assertThat(injectionDocument.getInjectionVolumeSetting().getValue()).isEqualTo(1);
        } else {
            Assertions.assertThat(injectionDocument.getInjectionVolumeSetting().getValue()).isNaN();
        }

        Assertions.assertThat(gasChromatographyDocument.getDeviceMethodIdentifier()).isEqualTo("DET3300.M");

        List<MeasurementDocument> measurementDocumentList =
                gasChromatographyDocument.getMeasurementAggregateDocument().getMeasurementDocument();
        Assertions.assertThat(measurementDocumentList).hasSize(1);

        DatacubeData data = measurementDocument.getChromatogramDataCube().getDatacubeData();
        Assertions.assertThat(data).isNotNull();

        List<List<Double>> dimensions = data.getDimensions();
        Assertions.assertThat(dimensions).hasSize(1);
        List<Double> dimensionValues = dimensions.get(0);
        Assertions.assertThat(dimensionValues).hasSize(5914);
        Assertions.assertThat(dimensionValues.get(2)).isCloseTo(0.3999065964955395, Percentage.withPercentage(0.001));

        List<List<Double>> measures = data.getMeasures();
        Assertions.assertThat(measures).hasSize(1);
        List<Double> measureValues = measures.get(0);
        Assertions.assertThat(measureValues).hasSize(5914);
        Assertions.assertThat(measureValues.get(0)).isCloseTo(2.1010416666666667, Percentage.withPercentage(0.001));

        ProcessedDataAggregateDocument processedDataAggregateDocument = measurementDocument.getProcessedDataAggregateDocument();
        Assertions.assertThat(processedDataAggregateDocument).isNotNull();
        List<ProcessedDataDocument> processedDataDocumentList = processedDataAggregateDocument.getProcessedDataDocument();
        Assertions.assertThat(processedDataDocumentList).isNotEmpty();
        PeakList peakList = processedDataDocumentList.get(0).getPeakList();

        if (additionalAssertions) {
            Assertions.assertThat(peakList.getPeak()).hasSize(36);
            Peak firstPeak = peakList.getPeak().get(0);
            Assertions.assertThat(firstPeak.getRetentionTime().getValue()).isEqualTo(10.05222);
            Assertions.assertThat(firstPeak.getWrittenName()).isEqualTo("Compound 0");
        } else {
            Assertions.assertThat(peakList.getPeak()).isEmpty();
        }

        if (additionalAssertions) {
            Assertions.assertThat(gasChromatographyAggregateDocument.getDeviceSystemDocument().getAssetManagementIdentifier())
                    .isEqualTo("GC52");
        }
    }

    @Test
    void returnsCorrectInfoForV179Folder() throws IOException {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();

        GasChromatographySimpleModel embedSchema = mapper.fromFolder(TestConstants.RESOURCE_V_179_D_FOLDER);
        assertV179Schema(embedSchema, true);
    }

    @Test
    void returnsCorrectInfoForV179File() throws IOException {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();

        GasChromatographySimpleModel embedSchema = mapper.fromChFile(TestConstants.RESOURCE_V_179_D_CH_FILE);
        assertV179Schema(embedSchema, false);
    }

    @Test
    void returnsCorrectInfoForV181Folder() throws IOException {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();

        GasChromatographySimpleModel embedSchema = mapper.fromFolder(TestConstants.RESOURCE_V_181_D_FOLDER);
        assertV181Schema(embedSchema, true);
    }

    @Test
    void returnsCorrectInfoForV181File() throws IOException {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();

        GasChromatographySimpleModel embedSchema = mapper.fromChFile(TestConstants.RESOURCE_V_181_D_CH_FILE);
        assertV181Schema(embedSchema, false);
    }

    @Test
    void returnsCorrectInfoForThreeChannelsFile() throws IOException {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();

        GasChromatographySimpleModel embedSchema = mapper.fromFolder(TestConstants.RESOURCE_THREE_CHANNELS_D_FOLDER);

        Assertions.assertThat(embedSchema).isNotNull();
        GasChromatographyAggregateDocument gasChromatographyAggregateDocument = embedSchema.getGasChromatographyAggregateDocument();
        Assertions.assertThat(gasChromatographyAggregateDocument).isNotNull();
        List<GasChromatographyDocument> gasChromatographyDocumentList = gasChromatographyAggregateDocument.getGasChromatographyDocument();
        Assertions.assertThat(gasChromatographyDocumentList).isNotEmpty();
        Assertions.assertThat(gasChromatographyDocumentList
                .get(0)
                .getMeasurementAggregateDocument()
                .getMeasurementDocument()).hasSize(3);
    }
}
