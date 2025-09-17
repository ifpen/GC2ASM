package fr.ifpen.allotropeconverters.gc.chemstation.mapping;

import fr.ifpen.allotropeconverters.allotrope_models.DeviceDocument;
import fr.ifpen.allotropeconverters.allotrope_models.DeviceSystemDocument;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.DetectorKind;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ResultXmlReader;

import java.util.ArrayList;
import java.util.List;

/** Mappe le système de devices (tous les détecteurs présents). */
public class DeviceSystemDocumentMapper {

    public DeviceSystemDocument toDeviceSystemDocument(ResultXmlReader.ResultData resultData) {
        DeviceSystemDocument system = new DeviceSystemDocument();
        system.setAssetManagementIdentifier(resultData.instrumentName());

        List<DeviceDocument> devices = new ArrayList<>();
        for (var signal : resultData.signals()) {
            DeviceDocument device = new DeviceDocument();
            device.setDeviceType(DetectorKind.fromRaw(signal.detectorRaw()).toAllotropeName());
            devices.add(device);
        }
        system.setDeviceDocument(devices);
        return system;
    }
}
