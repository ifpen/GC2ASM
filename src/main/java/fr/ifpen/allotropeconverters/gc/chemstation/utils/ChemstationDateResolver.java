package fr.ifpen.allotropeconverters.gc.chemstation.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ChemstationDateResolver {

    public static final List<DateTimeFormatter> DEFAULT_DATE_TIME_FORMATTERS =
            List.of(DateTimeFormatter.ofPattern("dd-MMM-yy, HH:mm:ss", Locale.US),
                    new DateTimeFormatterBuilder().appendPattern("dd MMM yy  hh:mm ")
                            .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "am", 1L, "pm"))
                            .toFormatter(Locale.US));

    private final List<DateTimeFormatter> dateTimeFormatters;

    public ChemstationDateResolver() {
        this(DEFAULT_DATE_TIME_FORMATTERS);
    }

    public ChemstationDateResolver(List<DateTimeFormatter> defaultDateTimeFormatters) {
        this.dateTimeFormatters = Objects.requireNonNullElseGet(defaultDateTimeFormatters, List::of);
    }

    public LocalDateTime getLocalDateTime(String dateTimeString) {
        LocalDateTime parse = null;
        for (DateTimeFormatter formatter : dateTimeFormatters) {
            try {
                parse = LocalDateTime.parse(dateTimeString, formatter);
            } catch (DateTimeParseException e) {
                // Do nothing
            }
        }
        return parse;
    }
}
