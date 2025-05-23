package fr.ifpen.allotropeconverters.gc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapper;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapperBuilder;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyTabularEmbedSchema;
import jakarta.xml.bind.JAXBException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

class AllotropeConversionToolsTests {

    private static void convertAndAssertJson(Path folderPath, ChemStationToAllotropeMapper mapper) throws JAXBException, IOException {
        AllotropeConversionTools allotropeConversionTools = new AllotropeConversionTools(mapper);

        ObjectNode node = allotropeConversionTools.convertFolderToAllotropeTree(folderPath);
        Assertions.assertThat(node).isNotNull();

        JsonSchema referenceSchema = getJsonSchemaFromClasspath();
        Set<ValidationMessage> errors = referenceSchema.validate(node);
        Assertions.assertThat(errors).isEmpty();
    }

    private static JsonSchema getJsonSchemaFromClasspath() throws IOException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        InputStream schemaStream = Files.newInputStream(TestConstants.RESOURCE_MAIN_SCHEMA_FILE);
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
                                                                                       .withChFilename("V181.ch")
                                                                                       .withAcqTxtFilename("acq_columInfoFixed.txt")
                                                                                       .build();
        convertAndAssertJson(TestConstants.RESOURCE_V_181_D_FOLDER, mapper);
    }

    @Test
    void readAllotropeFromInputStream() throws Exception {
        ChemStationToAllotropeMapper mapper = new ChemStationToAllotropeMapperBuilder().withZoneId(TestConstants.TIME_ZONE_PARIS).build();
        AllotropeConversionTools allotropeConversionTools = new AllotropeConversionTools(mapper);

        ObjectNode allotropeTree = allotropeConversionTools.convertFolderToAllotropeTree(TestConstants.RESOURCE_V_179_D_FOLDER);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(allotropeTree.toPrettyString().getBytes());
        GasChromatographyTabularEmbedSchema result = AllotropeConversionTools.readAllotropeFromInputStream(byteArrayInputStream);

        Assertions.assertThat(result).isNotNull();
    }
}
