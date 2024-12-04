package fr.ifpen.allotropeconverters.gc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapper;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapperBuilder;
import jakarta.xml.bind.JAXBException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

class GcToAllotropeJsonConverterTests {

    private static void convertAndAssertJson(String folderPath, ChemStationToAllotropeMapper mapper) throws JAXBException, IOException {
        GcToAllotropeJsonConverter gcToAllotropeJsonConverter = new GcToAllotropeJsonConverter(mapper);

        ObjectNode node = gcToAllotropeJsonConverter.convertFolderToAllotrope(folderPath);
        Assertions.assertThat(node).isNotNull();

        JsonSchema referenceSchema = getJsonSchemaFromClasspath();
        Set<ValidationMessage> errors = referenceSchema.validate(node);
        Assertions.assertThat(errors).isEmpty();
    }

    private static JsonSchema getJsonSchemaFromClasspath() throws IOException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        InputStream schemaStream = Files.newInputStream(Path.of(TestConstants.RESOURCE_MAIN_SCHEMA_FILE));
        return factory.getSchema(schemaStream);
    }

    @Test
    void returnsValidJsonForV179() throws Exception {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();
        convertAndAssertJson(TestConstants.RESOURCE_V_179_D_FOLDER, mapper);
    }

    @Test
    void returnsValidJsonForV181() throws Exception {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS)
                                                                                       .withChFileName("V181.ch")
                                                                                       .withTxtFileName("acq_columInfoFixed.txt")
                                                                                       .build();
        convertAndAssertJson(TestConstants.RESOURCE_V_181_D_FOLDER, mapper);
    }
}
