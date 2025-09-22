package fr.ifpen.allotropeconverters.gc.chemstation.mapping;

import fr.ifpen.allotropeconverters.allotrope_models.InjectionDocument;
import fr.ifpen.allotropeconverters.allotrope_models.InjectionDocumentInjectionVolumeSetting;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.Volume;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ResultXmlReader;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class InjectionDocumentMapper {

    private final ZoneId timeZone;
    private final List<DateTimeFormatter> dateTimeFormatters;

    public InjectionDocumentMapper(ZoneId timeZone, List<DateTimeFormatter> defaultDateTimeFormatters) {
        this.timeZone = timeZone;
        this.dateTimeFormatters = defaultDateTimeFormatters;
    }

    public InjectionDocument toInjectionDocument(ResultXmlReader.ResultData resultData) {
        InjectionDocument injection = new InjectionDocument();

        injection.setInjectionIdentifier(resultData.injectionIdentifier());
        injection.setInjectionTime(resultData.injectionDateTime());

        InjectionDocumentInjectionVolumeSetting volumeSetting = new InjectionDocumentInjectionVolumeSetting();
        volumeSetting.setUnit(InjectionDocumentInjectionVolumeSetting.UnitEnum.Micro_L);
        volumeSetting.setValue(Volume.fromText(resultData.injectionVolumeText()).microliters());

        injection.setInjectionVolumeSetting(volumeSetting);
        return injection;
    }

    public InjectionDocument toInjectionDocument(ChFile chFile){
        InjectionDocument injectionDocument = new InjectionDocument();
        InjectionDocumentInjectionVolumeSetting injectionVolumeSetting = new InjectionDocumentInjectionVolumeSetting();
        injectionVolumeSetting.setValue(Double.NaN);
        injectionVolumeSetting.setUnit(InjectionDocumentInjectionVolumeSetting.UnitEnum.Micro_L);
        injectionDocument.setInjectionVolumeSetting(injectionVolumeSetting);
        injectionDocument.setInjectionIdentifier("");

        injectionDocument.setInjectionTime(getInjectionDateInstant(chFile.getInjectionDateTime()));

        return injectionDocument;
    }

    private OffsetDateTime getInjectionDateInstant(String injectionDateString) {
        LocalDateTime injectionDate = getLocalDateTime(injectionDateString);
        if (injectionDate == null) {
            throw new IllegalArgumentException("Injection date has an unknown format. Original string is: '" + injectionDateString + "'");
        }
        return injectionDate.atZone(timeZone).toOffsetDateTime();
    }

    private LocalDateTime getLocalDateTime(String dateTimeString) {
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
