package fr.ifpen.allotropeconverters.gc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyTabularEmbedSchema;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapper;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GcToAllotropeJsonConverterTests {

    @Test
    void returnsValidJson() throws Exception {
            URI uri;
            uri = new File("src/test/resources/V179.D").toURI();

            GasChromatographyTabularEmbedSchema embedSchema;

                ZoneId zoneId = ZoneId.of("Europe/Paris");
                ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapper(zoneId);

                embedSchema = mapper.mapToGasChromatographySchema(
                        Paths.get(uri).toString());

                JsonSchema referenceSchema = getJsonSchemaFromClasspath();

                ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                JsonNode node = objectMapper.valueToTree(embedSchema);

                Set<ValidationMessage> errors = referenceSchema.validate(node);
                Assertions.assertEquals(0, errors.size());
    }

    @Test
    void returnsCorrectDeviceInfo_179() throws Exception {
            URI uri;
            uri = new File("src/test/resources/V179.D").toURI();

        ZoneId zoneId = ZoneId.of("Europe/Paris");
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapper(zoneId);

            GasChromatographyTabularEmbedSchema embedSchema = mapper.mapToGasChromatographySchema(
                    Paths.get(uri).toString());

            Assertions.assertEquals("GC65",
                    embedSchema.getGasChromatographyAggregateDocument()
                            .getDeviceSystemDocument()
                            .getAssetManagementIdentifier());

            Instant expectedInjectionDate = Instant.parse("2022-05-12T09:24:28Z"); // 12-May-22, 11:24:28

            Assertions.assertEquals(expectedInjectionDate,embedSchema.getGasChromatographyAggregateDocument().getGasChromatographyDocument().get(0).getInjectionDocument().getInjectionTime());
    }

    @Test
    void returnsCorrectDeviceInfo_81() throws Exception {
        URI uri;
        uri = new File("src/test/resources/V81.D").toURI();

        ZoneId zoneId = ZoneId.of("Europe/Paris");
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapper(zoneId);

        GasChromatographyTabularEmbedSchema embedSchema = mapper.mapToGasChromatographySchema(
                Paths.get(uri).toString());

        Assertions.assertEquals("HP G1530A",
                embedSchema.getGasChromatographyAggregateDocument()
                        .getDeviceSystemDocument()
                        .getAssetManagementIdentifier());

        Instant expectedInjectionDate = Instant.parse("2019-10-20T19:28:54Z");

        Assertions.assertEquals(expectedInjectionDate,embedSchema.getGasChromatographyAggregateDocument().getGasChromatographyDocument().get(0).getInjectionDocument().getInjectionTime());
    }

    private JsonSchema getJsonSchemaFromClasspath() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("gas-chromatography.tabular.embed.schema.json");
        return factory.getSchema(is);
    }

    @Test
    void IntegrationTest_179() throws JAXBException, IOException {
        String filePath = Paths.get(new File("src/test/resources/V179.D").toURI()).toString();
        GcToAllotropeJsonConverter converter = new GcToAllotropeJsonConverter();
        ObjectNode result = converter.convertFile(filePath);
        Assertions.assertFalse(result.isNull());
    }

    @Test
    void IntegrationTest_81() throws JAXBException, IOException {
        String filePath = Paths.get(new File("src/test/resources/V81.D").toURI()).toString();
        GcToAllotropeJsonConverter converter = new GcToAllotropeJsonConverter();
        ObjectNode result = converter.convertFile(filePath);
        Assertions.assertFalse(result.isNull());
    }

    @AfterAll()
        void CleanUp(){
        File resultFile = new File("src/test/resources/V179.json");
        if(resultFile.exists()){
            Boolean delete = resultFile.delete();
        }
    }
}
