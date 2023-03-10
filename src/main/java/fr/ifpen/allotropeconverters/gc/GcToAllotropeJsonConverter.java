package fr.ifpen.allotropeconverters.gc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapper;
import fr.ifpen.allotropeconverters.gc.schema.GasChromatographyTabularEmbedSchema;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.text.ParseException;

public class GcToAllotropeJsonConverter {
    public ObjectNode convertFile(String filePath) throws JAXBException, IOException, ParseException {
            GasChromatographyTabularEmbedSchema embedSchema =
                    ChemStationToAllotropeMapper.mapToGasChromatographySchema(filePath);

            ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            return objectMapper.valueToTree(embedSchema);
    }
}
