package fr.ifpen.allotropeconverters.gc.chemstation.infra;

import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFileFactory;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.ChannelKey;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChFileResolver {
    private final ChFileFactory factory = new ChFileFactory();

    /** Construit map channelKey -> ChFile. Règle nommage: detector + signalId + ".ch" (ex: FID1A.ch) */
    public Map<ChannelKey, ChFile> resolve(Path folder, ResultXmlReader.ResultData data) throws IOException {
        Map<ChannelKey, ChFile> out = new LinkedHashMap<>();
        for (var s : data.signals()) {
            String fileName = s.detectorRaw() + s.signalIdRaw() + ".ch";
            Path path = folder.resolve(fileName);
            out.put(s.channelKey(), factory.getChFile(path));
        }
        return out;
    }
}
