package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

class ReadHelpers {

    private ReadHelpers(){}

    static String readString(DataInput input, boolean isUTF16) throws IOException {
        int stringLength = input.readByte();

        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < stringLength; i++){
            if(isUTF16){
                ByteBuffer buffer = ByteBuffer.allocate(2);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.put(input.readByte());
                buffer.put(input.readByte());
                stringBuilder.append(buffer.getChar(0));
            } else {
                stringBuilder.append((char)input.readByte());
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
        for(int i=0; i<8;i++){
            byteBuffer.put(input.readByte());
        }
        return byteBuffer.getDouble(0);
    }

    static Float readMetadataTime(RandomAccessFile input, long position) throws IOException {
        input.seek(position);
        float rawMetadataTime = input.readFloat();
        return rawMetadataTime / 60000;
    }

    static List<Double> readDoubleDeltaStream(RandomAccessFile input, int dataStartPosition, Double yScaling, Double yOffset) throws IOException {
        input.seek(dataStartPosition);

        ArrayList<Double> values = new ArrayList<>();
        long[] buffer = new long[]{0, 0, 0};

        boolean endOfFile = false;

        while (!endOfFile) {
            try {
                buffer[2] = input.readShort();

                if (buffer[2] != 32767) {
                    buffer[1] = buffer[2] + buffer[1];
                    buffer[0] = buffer[1] + buffer[0];
                } else {
                    buffer[0] = (long) input.readShort() << 32;
                    buffer[0] = input.readInt() + buffer[0];
                    buffer[1] = 0;
                }

                values.add(buffer[0] * yScaling + yOffset);

            } catch (EOFException e) {
                endOfFile = true;
            }
        }
        return values;
    }
}
