// fr/ifpen/allotropeconverters/gc/chemstation/infra/ResultXmlReader.java
package fr.ifpen.allotropeconverters.gc.chemstation.infra;

import fr.ifpen.allotropeconverters.gc.chemstation.domain.*;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

public interface ResultXmlReader {

    record SignalRecord(ChannelKey channelKey,
                        String detectorRaw,               // "FID1"
                        String signalIdRaw,               // "A"
                        String signalDescription,         // "FID1 A, Front Signal"
                        List<IntegrationRow> integrationRows) {}


    record ResultData(
            String instrumentName,
            String analystFromXml,
            String methodFromXml,
            String sampleNameFromXml,
            String sampleDescription,
            OffsetDateTime injectionDateTime,
            String injectionIdentifier,
            String injectionVolumeText,
            List<SignalRecord> signals,
            List<CompoundPeak> compounds
    ) {}

    ResultData read(Path resultXmlFile);
}
