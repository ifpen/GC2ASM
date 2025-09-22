package fr.ifpen.allotropeconverters.gc.chemstation.mapping;

import fr.ifpen.allotropeconverters.allotrope_models.DeviceControlAggregateDocument;
import fr.ifpen.allotropeconverters.allotrope_models.DeviceControlDocument;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.DetectorKind;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ResultXmlReader;

import java.util.List;

/** Mappe les métadonnées de contrôle device pour un signal. */
public class DeviceControlAggregateDocumentMapper {

    public DeviceControlAggregateDocument toDeviceControlAggregateDocument(ResultXmlReader.SignalRecord signal) {
        DeviceControlDocument control = new DeviceControlDocument();
        control.setDeviceType(DetectorKind.fromRaw(signal.detectorRaw()).toAllotropeName());
        DeviceControlAggregateDocument aggregate = new DeviceControlAggregateDocument();
        aggregate.setDeviceControlDocument(List.of(control));
        return aggregate;
    }
}
