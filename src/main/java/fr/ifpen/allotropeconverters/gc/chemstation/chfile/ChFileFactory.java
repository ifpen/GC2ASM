package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readString;

public class ChFileFactory {

    public ChFile getChFile(String filePath) throws IOException {
        File file = new File(filePath);
        try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
            String version = readString(input, false);

            switch (version) {
                case "179":
                    return new ChFile179(input);
                case "181":
                    return new ChFile181(input);
                default:
                    throw new IOException("version not supported");
            }
        }
    }
}
