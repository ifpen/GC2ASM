package fr.ifpen.allotropeconverters.gc;

import java.nio.file.Path;
import java.time.ZoneId;

public class TestConstants {

    public static final ZoneId TIME_ZONE_PARIS = ZoneId.of("Europe/Paris");

    public static final Path RESOURCE_MAIN_SCHEMA_FILE = Path.of("src/main/resources/gas-chromatography.tabular.embed.schema.json");

    public static final Path RESOURCE_V_179_D_FOLDER = Path.of("src/test/resources/V179.D");
    public static final Path RESOURCE_V_179_D_CH_FILE = RESOURCE_V_179_D_FOLDER.resolve("FID1A.ch");

    public static final Path RESOURCE_V_181_D_FOLDER = Path.of("src/test/resources/V181.D");
    public static final Path RESOURCE_V_181_D_CH_FILE = RESOURCE_V_181_D_FOLDER.resolve("V181.ch");

    private TestConstants() {}
}
