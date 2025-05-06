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
import java.io.InputStream;

/**
 * AllotropeConversionTools is responsible for converting gas chromatography data
 * into an Allotrope-compatible JSON structure.
 * <p>
 * It supports conversion from .ch files
 * as well as from folders containing multiple related files (.ch, .xml, and .txt).
 */
public class AllotropeConversionTools {

    private final ChemStationToAllotropeMapper chemstationMapper;

    /**
     * Creates a AllotropeConversionTools with a default {@link ChemStationToAllotropeMapper} from {@link ChemStationToAllotropeMapperBuilder}.
     */
    public AllotropeConversionTools() {
        this(new ChemStationToAllotropeMapperBuilder().build());
    }

    /**
     * Creates a AllotropeConversionTools with a custom {@link ChemStationToAllotropeMapper}.
     *
     * @param chemstationMapper
     *         mapper created from {@link ChemStationToAllotropeMapperBuilder}
     */
    public AllotropeConversionTools(ChemStationToAllotropeMapper chemstationMapper) {
        this.chemstationMapper = chemstationMapper;
    }

    public static ObjectNode schema(GasChromatographyTabularEmbedSchema embedSchema) {
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
    public ObjectNode convertFolderToAllotropeTree(String folderPath) throws JAXBException, IOException {
        return schema(convertFolderToAllotrope(folderPath));
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
    public ObjectNode convertChFileToAllotropeTree(String chFilePath) throws IOException {
        return schema(convertChFileToAllotrope(chFilePath));
    }

    /**
     * Converts the contents of a specified folder into an Allotrope object.
     *
     * @param folderPath
     *         the path to the folder containing the .ch, result.xml and acq.txt files to be converted
     *
     * @return a GasChromatographyTabularEmbedSchema representing the Allotrope content
     *
     * @throws JAXBException
     *         if there is an error while processing XML files during the conversion
     * @throws IOException
     *         if there is an I/O error while reading the folder or its contents
     */
    public GasChromatographyTabularEmbedSchema convertFolderToAllotrope(String folderPath) throws JAXBException, IOException {
        return chemstationMapper.fromFolder(folderPath);
    }

    /**
     * Converts the contents of a specified .ch file into an Allotrope object.
     *
     * @param chFilePath
     *         the file path to the .ch file to be converted
     *
     * @return a GasChromatographyTabularEmbedSchema representing the Allotrope content
     *
     * @throws IOException
     *         if there is an I/O error while reading the folder or its contents
     */
    public GasChromatographyTabularEmbedSchema convertChFileToAllotrope(String chFilePath) throws IOException {
        return chemstationMapper.fromChFile(chFilePath);
    }

    /**
     * Converts the contents of an imput stream into an Allotrope object.
     *
     * @param inputStream
     *         the inputstream to read from
     *
     * @return a GasChromatographyTabularEmbedSchema representing the Allotrope content
     *
     * @throws IOException
     *         if there is an I/O error while reading the folder or its contents
     */
    public static GasChromatographyTabularEmbedSchema readAllotropeFromInputStream(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.readValue(
                inputStream,
                GasChromatographyTabularEmbedSchema.class
        );
    }
}
