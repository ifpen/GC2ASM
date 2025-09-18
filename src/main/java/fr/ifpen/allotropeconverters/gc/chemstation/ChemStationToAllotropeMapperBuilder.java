package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.chemstation.domain.MergeStrategy;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ChFileResolver;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.DomResultXmlReader;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ResultXmlReader;
import fr.ifpen.allotropeconverters.gc.chemstation.mapping.*;
import fr.ifpen.allotropeconverters.gc.chemstation.service.PeakAssociationService;

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
 *     <li><ul>
 *         <li><strong>dd-MMM-yy, HH:mm:ss</strong></li>
 *         <li><strong>dd MMM yy  hh:mm a</strong></li>
 *     </ul></li>
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
    private String resultXmlFileName = "Result.xml";
    private String acqTxtFileName = "acq.txt";

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
     * Sets the file name of the .xml file to be used for the mapping operation.
     *
     * @param resultXmlFilename
     *         the name of the .xml file
     *
     * @return the current instance of {@code ChemStationToAllotropeMapperBuilder} for method chaining
     */
    public ChemStationToAllotropeMapperBuilder withResultXmlFilename(String resultXmlFilename) {
        this.resultXmlFileName = resultXmlFilename;
        return this;
    }

    /**
     * Sets the file name of the .txt file to be used for the mapping operation.
     *
     * @param acqTxtFileName
     *         the name of the .txt file
     *
     * @return the current instance of {@code ChemStationToAllotropeMapperBuilder} for method chaining
     */
    public ChemStationToAllotropeMapperBuilder withAcqTxtFilename(String acqTxtFileName) {
        this.acqTxtFileName = acqTxtFileName;
        return this;
    }

    /**
     * Builds and returns a configured instance of {@code ChemStationToAllotropeMapper}.
     *
     * @return a new instance of {@code ChemStationToAllotropeMapper} configured
     */
    public ChemStationToAllotropeMapper build() {

        ResultXmlReader resultXmlReader = new DomResultXmlReader(zoneId, dateTimeFormatters);
        ChFileResolver chFileResolver = new ChFileResolver();
        DeviceSystemDocumentMapper deviceSystemDocumentMapper = new DeviceSystemDocumentMapper();
        DeviceControlAggregateDocumentMapper deviceControlAggregateDocumentMapper = new DeviceControlAggregateDocumentMapper();
        SampleDocumentMapper sampleDocumentMapper = new SampleDocumentMapper();
        InjectionDocumentMapper injectionDocumentMapper = new InjectionDocumentMapper(zoneId, dateTimeFormatters);
        PeakAssociationService peakAssociationService = new PeakAssociationService();
        MeasurementDocumentMapper measurementDocumentMapper =
                new MeasurementDocumentMapper(
                        sampleDocumentMapper,
                        injectionDocumentMapper,
                        deviceControlAggregateDocumentMapper,
                        peakAssociationService);


        return new ChemStationToAllotropeMapper(
                resultXmlReader,
                chFileResolver,
                deviceSystemDocumentMapper,
                measurementDocumentMapper,
                peakAssociationService,
                acqTxtFileName,
                resultXmlFileName);
    }
}
