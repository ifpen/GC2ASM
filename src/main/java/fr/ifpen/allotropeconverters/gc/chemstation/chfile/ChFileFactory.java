package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readString;

/**
 * ChFileFactory is responsible for parsing .ch files and creating the appropriate subclass instance of ChFile.
 * This is determined based on the version information contained within the file.
 * Supported versions are handled through specific subclasses such as ChFile179 and ChFile181.
 * <p>
 * This class facilitates:
 * - Reading the .ch file to determine its version.
 * - Returning an instance of the subclass that corresponds to the identified version.
 * - Ensuring an IOException is thrown if an unsupported or invalid version is encountered.
 */
public class ChFileFactory {

    /**
     * Parses a .ch file at the specified file path and returns an instance of the appropriate subclass of ChFile.
     *
     * @param filePath
     *         the path to the .ch file to be parsed
     *
     * @return a ChFile instance representing the parsed file, either ChFile179 or ChFile181 depending on the version
     *
     * @throws IOException
     *         if an I/O error occurs while reading the file or if the version is not supported
     */
    public ChFile getChFile(String filePath) throws IOException {
        File file = new File(filePath);
        try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
            String version = readString(input, false);

            return switch (version) {
                case "179" -> new ChFile179(input);
                case "181" -> new ChFile181(input);
                default -> throw new IOException("version not supported");
            };
        }
    }
}
