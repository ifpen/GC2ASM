package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
}
