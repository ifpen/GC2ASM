package fr.ifpen.allotropeconverters.gc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapper;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapperBuilder;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyTabularEmbedSchema;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;

/**
 * GcToAllotropeJsonConverter is responsible for converting gas chromatography data
 * into an Allotrope-compatible JSON structure.
 * <p>
 * It supports conversion from .ch files
 * as well as from folders containing multiple related files (.ch, .xml, and .txt).
 */
public class GcToAllotropeJsonConverter {

    private final ChemStationToAllotropeMapper chemstationMapper;

    /**
     * Creates a GcToAllotropeJsonConverter with a default {@link ChemStationToAllotropeMapper} from {@link ChemStationToAllotropeMapperBuilder}.
     */
    public GcToAllotropeJsonConverter() {
        this(new ChemStationToAllotropeMapperBuilder().build());
    }

    /**
     * Creates a GcToAllotropeJsonConverter with a custom {@link ChemStationToAllotropeMapper}.
     *
     * @param chemstationMapper
     *         mapper created from {@link ChemStationToAllotropeMapperBuilder}
     */
    public GcToAllotropeJsonConverter(ChemStationToAllotropeMapper chemstationMapper) {
        this.chemstationMapper = chemstationMapper;
    }

    private static ObjectNode schema(GasChromatographyTabularEmbedSchema embedSchema) {
        ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper.valueToTree(embedSchema);
    }

    /**
     * Converts the contents of a specified folder into an Allotrope-compatible JSON representation.
     *
     * @param folderPath
     *         the path to the folder containing the .ch, .xml and .txt files to be converted
     *
     * @return an ObjectNode representing the Allotrope-compatible JSON structure
     *
     * @throws JAXBException
     *         if there is an error while processing XML files during the conversion
     * @throws IOException
     *         if there is an I/O error while reading the folder or its contents
     */
    public ObjectNode convertFolderToAllotrope(String folderPath) throws JAXBException, IOException {
        GasChromatographyTabularEmbedSchema embedSchema = chemstationMapper.fromFolder(folderPath);

        return schema(embedSchema);
    }

    /**
     * Converts the contents of a specified .ch file into an Allotrope-compatible JSON representation.
     *
     * @param chFilePath
     *         the file path to the .ch file to be converted
     *
     * @return an ObjectNode representing the Allotrope-compatible JSON structure
     *
     * @throws IOException
     *         if there is an error accessing or reading the required .ch file
     */
    public ObjectNode convertChFileToAllotrope(String chFilePath) throws IOException {
        GasChromatographyTabularEmbedSchema embedSchema = chemstationMapper.fromChFile(chFilePath);

        return schema(embedSchema);
    }
}
