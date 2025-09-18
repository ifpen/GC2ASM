package fr.ifpen.allotropeconverters.gc;

import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TestConstants {

    public static final ZoneId TIME_ZONE_PARIS = ZoneId.of("Europe/Paris");

    public static final List<DateTimeFormatter> DEFAULT_DATE_TIME_FORMATTERS =
            List.of(DateTimeFormatter.ofPattern("dd-MMM-yy, HH:mm:ss", Locale.US),
                    new DateTimeFormatterBuilder().appendPattern("dd MMM yy  hh:mm ")
                            .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "am", 1L, "pm"))
                            .toFormatter(Locale.US));

    public static final Path RESOURCE_SCHEMA_FILE = Path.of("src/test/resources/gas-chromatography.tabular.embed.schema.json");

    public static final Path RESOURCE_V_179_D_FOLDER = Path.of("src/test/resources/V179.D");
    public static final Path RESOURCE_V_179_D_CH_FILE = RESOURCE_V_179_D_FOLDER.resolve("FID1A.ch");
    public static final Path RESOURCE_V_179_D_XML_RESULT = RESOURCE_V_179_D_FOLDER.resolve("Result.xml");

    public static final Path RESOURCE_V_181_D_FOLDER = Path.of("src/test/resources/V181.D");
    public static final Path RESOURCE_V_181_D_CH_FILE = RESOURCE_V_181_D_FOLDER.resolve("FID1A.ch");

    public static final Path RESOURCE_THREE_CHANNELS_D_FOLDER = Path.of("src/test/resources/THREE-CHANNELS.D");

    private TestConstants() {}
}
