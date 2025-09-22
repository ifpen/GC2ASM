package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.allotrope_models.*;
import fr.ifpen.allotropeconverters.gc.TestConstants;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

class ChemStationToAllotropeMapperTests {

    private static void assertV179Schema(GasChromatographySimpleModel embedSchema, boolean additionalAssertions) {
        Assertions.assertThat(embedSchema).isNotNull();

        GasChromatographyAggregateDocument gasChromatographyAggregateDocument = embedSchema.getGasChromatographyAggregateDocument();

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
        Assertions.assertThat(data).isInstanceOf(DatacubeData.class);


        List<List<Double>> dimensions = data.getDimensions();
        Assertions.assertThat(dimensions).hasSize(1);
        Assertions.assertThat(dimensions.get(0)).hasSize(71840);

        List<List<Double>> measures = data.getMeasures();
        Assertions.assertThat(measures).hasSize(1);
        Assertions.assertThat(measures.get(0)).hasSize(71840);

        PeakList peakList =
                measurementDocument.getProcessedDataAggregateDocument().getProcessedDataDocument().get(0).getPeakList();
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
    void returnsCorrectInfoForThreeChannelsFile() throws IOException {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();

        GasChromatographySimpleModel embedSchema = mapper.fromFolder(TestConstants.RESOURCE_THREE_CHANNELS_D_FOLDER);

        Assertions.assertThat(embedSchema).isNotNull();
        Assertions.assertThat(
                embedSchema
                        .getGasChromatographyAggregateDocument()
                        .getGasChromatographyDocument().get(0)
                        .getMeasurementAggregateDocument()
                        .getMeasurementDocument()).hasSize(3);
    }
}
