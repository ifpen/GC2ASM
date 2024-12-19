package fr.ifpen.allotropeconverters.gc;

import java.time.ZoneId;

public class TestConstants {

    public static final ZoneId TIME_ZONE_PARIS = ZoneId.of("Europe/Paris");

    public static final String RESOURCE_MAIN_SCHEMA_FILE = "src/main/resources/gas-chromatography.tabular.embed.schema.json";

    public static final String RESOURCE_V_179_D_FOLDER = "src/test/resources/V179.D";
    public static final String RESOURCE_V_179_D_CH_FILE = RESOURCE_V_179_D_FOLDER + "/FID1A.ch";

    public static final String RESOURCE_V_181_D_FOLDER = "src/test/resources/V181.D";
    public static final String RESOURCE_V_181_D_CH_FILE = RESOURCE_V_181_D_FOLDER + "/V181.ch";

    private TestConstants() {}
}
