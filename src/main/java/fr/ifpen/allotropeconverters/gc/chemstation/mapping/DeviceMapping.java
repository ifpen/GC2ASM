package fr.ifpen.allotropeconverters.gc.chemstation.mapping;

import fr.ifpen.allotropeconverters.allotrope_models.*;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.DetectorKind;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ResultXmlReader;

import java.util.ArrayList;
import java.util.List;

public class DeviceMapping {

    public DeviceSystemDocument toDeviceSystem(ResultXmlReader.ResultData data) {
        DeviceSystemDocument system = new DeviceSystemDocument();
        system.setAssetManagementIdentifier(data.instrumentName());

        List<DeviceDocument> devices = new ArrayList<>();
        for (var s : data.signals()) {
            DeviceDocument device = new DeviceDocument();
            device.setDeviceType(DetectorKind.fromRaw(s.detectorRaw()).toAllotropeName());
            devices.add(device);
        }
        system.setDeviceDocument(devices);
        return system;
    }

    public DeviceControlAggregateDocument toDeviceControlForSignal(ResultXmlReader.SignalRecord signal) {
        DeviceControlDocument ctrl = new DeviceControlDocument();
        ctrl.setDeviceType(DetectorKind.fromRaw(signal.detectorRaw()).toAllotropeName());
        DeviceControlAggregateDocument agg = new DeviceControlAggregateDocument();
        agg.setDeviceControlDocument(List.of(ctrl));
        return agg;
    }
}
