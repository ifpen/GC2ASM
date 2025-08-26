package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class ReadHelpers {

    private ReadHelpers() {}

    static String readString(DataInput input, boolean isUTF16) throws IOException {
        int stringLength = input.readByte();

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < stringLength; i++) {
            if (isUTF16) {
                ByteBuffer buffer = ByteBuffer.allocate(2);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.put(input.readByte());
                buffer.put(input.readByte());
                stringBuilder.append(buffer.getChar(0));
            } else {
                stringBuilder.append((char) input.readByte());
            }
        }

        return stringBuilder.toString();
    }

    static String readStringAtPosition(RandomAccessFile input, long position, boolean isUtf16) throws IOException {
        input.seek(position);
        return readString(input, isUtf16);
    }

    static Double readLittleEndianDouble(DataInput input) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 8; i++) {
            byteBuffer.put(input.readByte());
        }
        return byteBuffer.getDouble(0);
    }

    /**
     * Reads the metadata time at a specific position in a binary file.
     * This method seeks to the given position in the file, reads a 4-byte floating-point value,
     * and converts it from milliseconds to seconds by dividing it by 1000.
     *
     * @param input    the RandomAccessFile from which the metadata time is read
     * @param position the position in the file at which the metadata time is stored
     * @return the metadata time in seconds as a Float
     * @throws IOException if an I/O error occurs while accessing the file
     */
    static Float readMetadataTime(RandomAccessFile input, long position) throws IOException {
        input.seek(position);
        float rawMetadataTime = input.readFloat();
        if (rawMetadataTime < 0 && rawMetadataTime > -1000) {rawMetadataTime = 0;} // Correct for barely negative times
        return rawMetadataTime / 1000;
    }
}
