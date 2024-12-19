package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.allotropeutils.AllotropeData;
import fr.ifpen.allotropeconverters.gc.TestConstants;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyAggregateDocument;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyDocument;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyTabularEmbedSchema;
import fr.ifpen.allotropeconverters.gc.schema.InjectionDocument;
import fr.ifpen.allotropeconverters.gc.schema.MeasurementDocument;
import fr.ifpen.allotropeconverters.gc.schema.PeakList;
import fr.ifpen.allotropeconverters.gc.schema.SampleDocument;
import jakarta.xml.bind.JAXBException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

class ChemStationToAllotropeMapperTests {

    private static void assertV179Schema(GasChromatographyTabularEmbedSchema embedSchema, boolean additionalAssertions) {
        Assertions.assertThat(embedSchema).isNotNull();

        GasChromatographyAggregateDocument gasChromatographyAggregateDocument = embedSchema.getGasChromatographyAggregateDocument();

        List<GasChromatographyDocument> gasChromatographyDocumentList = gasChromatographyAggregateDocument.getGasChromatographyDocument();
        Assertions.assertThat(gasChromatographyDocumentList).hasSize(1);

        GasChromatographyDocument gasChromatographyDocument = gasChromatographyDocumentList.get(0);
        Assertions.assertThat(gasChromatographyDocument.getSubmitter()).isEqualTo("SYSTEM");

        if (additionalAssertions) {
            Assertions.assertThat(gasChromatographyDocument.getChromatographyColumnDocument().getProductManufacturer())
                      .isEqualTo("Agilent");
        }

        SampleDocument sampleDocument = gasChromatographyDocument.getSampleDocument();
        Assertions.assertThat(sampleDocument.getSampleIdentifier()).isEqualTo("22-00465-1");

        if (additionalAssertions) {
            Assertions.assertThat(sampleDocument.getDescription())
                      .isEqualTo(
                              "22-00465-1 - E2046501 - DET : 401 - -FORD (TAE 9891)-HUILE -  - delai Lims : 11/02/2022 -Jean Fritz FORTUNE - VQC28002");
        }

        InjectionDocument injectionDocument = gasChromatographyDocument.getInjectionDocument();
        Assertions.assertThat(injectionDocument.getInjectionTime()).isEqualTo(Instant.parse("2022-05-12T09:24:28Z")); // 12-May-22, 11:24:28

        if (additionalAssertions) {
            Assertions.assertThat(injectionDocument.getInjectionIdentifier()).isEqualTo("1");
            Assertions.assertThat(injectionDocument.getInjectionVolumeSetting().getValue()).isEqualTo(1.0);
        } else {
            Assertions.assertThat(injectionDocument.getInjectionVolumeSetting().getValue()).isNaN();
        }

        Assertions.assertThat(gasChromatographyDocument.getDeviceMethodIdentifier()).isEqualTo("DET401.M");

        List<MeasurementDocument> measurementDocumentList =
                gasChromatographyDocument.getMeasurementAggregateDocument().getMeasurementDocument();
        Assertions.assertThat(measurementDocumentList).hasSize(1);

        MeasurementDocument measurementDocument = measurementDocumentList.get(0);
        Object data = measurementDocument.getChromatogramDataCube().getData();
        Assertions.assertThat(data).isInstanceOf(AllotropeData.class);

        AllotropeData allotropeData = (AllotropeData) data;

        List<List<Double>> dimensions = allotropeData.dimensions();
        Assertions.assertThat(dimensions).hasSize(1);
        Assertions.assertThat(dimensions.get(0)).hasSize(71841);

        List<List<Double>> measures = allotropeData.measures();
        Assertions.assertThat(measures).hasSize(1);
        Assertions.assertThat(measures.get(0)).hasSize(71840);

        PeakList peakList = measurementDocument.getPeakList();
        if (additionalAssertions) {
            Assertions.assertThat(peakList.getPeak()).hasSize(24);
        } else {
            Assertions.assertThat(peakList.getPeak()).isEmpty();
        }

        if (additionalAssertions) {
            Assertions.assertThat(gasChromatographyAggregateDocument.getDeviceSystemDocument().getAssetManagementIdentifier())
                      .isEqualTo("GC65");
        }
    }

    @Test
    void returnsCorrectInfoForV179Folder() throws JAXBException, IOException {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();

        GasChromatographyTabularEmbedSchema embedSchema = mapper.fromFolder(TestConstants.RESOURCE_V_179_D_FOLDER);
        assertV179Schema(embedSchema, true);
    }

    @Test
    void returnsCorrectInfoForV179File() throws IOException {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();

        GasChromatographyTabularEmbedSchema embedSchema = mapper.fromChFile(TestConstants.RESOURCE_V_179_D_FOLDER);
        assertV179Schema(embedSchema, false);
    }
}
