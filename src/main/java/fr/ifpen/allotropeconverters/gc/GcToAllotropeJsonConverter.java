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
import java.time.ZoneOffset;

public class GcToAllotropeJsonConverter {


    private final ZoneOffset defaultTimeZone;
    private ChemStationToAllotropeMapper chemstationMapper;

    public GcToAllotropeJsonConverter(){
        defaultTimeZone = ZoneOffset.UTC;
        this.createMapper();
    }

    public GcToAllotropeJsonConverter(ZoneOffset defaultTimeZone){
        this.defaultTimeZone = defaultTimeZone;
        this.createMapper();
    }

    private void createMapper(){
        chemstationMapper = new ChemStationToAllotropeMapper(defaultTimeZone);
    }

    public ObjectNode convertFile(String filePath) throws JAXBException, IOException {
            GasChromatographyTabularEmbedSchema embedSchema =
                    chemstationMapper.mapToGasChromatographySchema(filePath);

            ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            return objectMapper.valueToTree(embedSchema);
    }
}
