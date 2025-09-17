package fr.ifpen.allotropeconverters.gc.chemstation.mapping;

import fr.ifpen.allotropeconverters.allotrope_models.InjectionDocument;
import fr.ifpen.allotropeconverters.allotrope_models.InjectionDocumentInjectionVolumeSetting;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.Volume;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ResultXmlReader;

public class InjectionDocumentMapper {

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
}
