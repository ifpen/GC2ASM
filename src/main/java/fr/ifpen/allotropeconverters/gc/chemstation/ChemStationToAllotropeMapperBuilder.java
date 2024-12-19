package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapper.MergeStrategy;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builder for {@link ChemStationToAllotropeMapper}.
 * <p>
 * Default {@link ChemStationToAllotropeMapper} has for values:
 * <ul>
 *     <li>Time-zone: <strong>UTC</strong></li>
 *     <li>Date-time formatter patterns:</li>
 *     <ul>
 *         <li><strong>dd-MMM-yy, HH:mm:ss</strong></li>
 *         <li><strong>dd MMM yy  hh:mm a</strong></li>
 *     </ul>
 *     <li>.ch file name: <strong>FID1A.ch</strong></li>
 *     <li>.xml file name: <strong>Result.xml</strong></li>
 *     <li>.txt file name: <strong>acq.txt</strong></li>
 *     <li>{@link MergeStrategy MergeStrategy}: {@link MergeStrategy#ERROR MergeStrategy.ERROR}</li>
 * </ul>
 */
public class ChemStationToAllotropeMapperBuilder {

    private static final List<DateTimeFormatter> DEFAULT_DATE_TIME_FORMATTERS =
            List.of(DateTimeFormatter.ofPattern("dd-MMM-yy, HH:mm:ss", Locale.US),
                    new DateTimeFormatterBuilder().appendPattern("dd MMM yy  hh:mm ")
                                                  .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "am", 1L, "pm"))
                                                  .toFormatter(Locale.US));

    private final List<DateTimeFormatter> dateTimeFormatters = new ArrayList<>(DEFAULT_DATE_TIME_FORMATTERS);

    private ZoneId zoneId = ZoneOffset.UTC;
    private String chFileName = "FID1A.ch";
    private String xmlFileName = "Result.xml";
    private String txtFileName = "acq.txt";
    private MergeStrategy mergeStrategy = MergeStrategy.ERROR;

    /**
     * Sets the time zone to use for the ChemStation to Allotrope mapping operation.
     *
     * @param zoneId
     *         the {@code ZoneId} representing the desired time zone
     *
     * @return the current instance of {@code ChemStationToAllotropeMapperBuilder} for method chaining
     */
    public ChemStationToAllotropeMapperBuilder withZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    /**
     * Adds additional date-time formatters to be used in the ChemStation to Allotrope mapping operation.
     *
     * @param formatter
     *         one or more {@code DateTimeFormatter} instances to add
     *
     * @return the current instance of {@code ChemStationToAllotropeMapperBuilder} for method chaining
     */
    public ChemStationToAllotropeMapperBuilder withAdditionalDateTimeFormatters(DateTimeFormatter... formatter) {
        dateTimeFormatters.addAll(List.of(formatter));
        return this;
    }

    /**
     * Sets the file name of the ChemStation file to be used for the mapping operation.
     *
     * @param chFileName
     *         the name of the ChemStation file
     *
     * @return the current instance of {@code ChemStationToAllotropeMapperBuilder} for method chaining
     */
    public ChemStationToAllotropeMapperBuilder withChFileName(String chFileName) {
        this.chFileName = chFileName;
        return this;
    }

    /**
     * Sets the file name of the .xml file to be used for the mapping operation.
     *
     * @param xmlFileName
     *         the name of the .xml file
     *
     * @return the current instance of {@code ChemStationToAllotropeMapperBuilder} for method chaining
     */
    public ChemStationToAllotropeMapperBuilder withXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
        return this;
    }

    /**
     * Sets the file name of the .txt file to be used for the mapping operation.
     *
     * @param txtFileName
     *         the name of the .txt file
     *
     * @return the current instance of {@code ChemStationToAllotropeMapperBuilder} for method chaining
     */
    public ChemStationToAllotropeMapperBuilder withTxtFileName(String txtFileName) {
        this.txtFileName = txtFileName;
        return this;
    }

    /**
     * Sets the merge strategy to be used for handling conflicts when different values
     * are read for the same field during the ChemStation to Allotrope mapping process.
     *
     * @param mergeStrategy
     *         the {@code MergeStrategy} to apply for resolving conflicting values
     *
     * @return the current instance of {@code ChemStationToAllotropeMapperBuilder} for method chaining
     */
    public ChemStationToAllotropeMapperBuilder withMergeStrategy(MergeStrategy mergeStrategy) {
        this.mergeStrategy = mergeStrategy;
        return this;
    }

    /**
     * Builds and returns a configured instance of {@code ChemStationToAllotropeMapper}.
     *
     * @return a new instance of {@code ChemStationToAllotropeMapper} configured
     */
    public ChemStationToAllotropeMapper build() {
        return new ChemStationToAllotropeMapper(zoneId, dateTimeFormatters, chFileName, xmlFileName, txtFileName, mergeStrategy);
    }
}
