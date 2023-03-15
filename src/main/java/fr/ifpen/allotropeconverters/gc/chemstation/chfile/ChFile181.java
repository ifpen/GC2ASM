package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

class ChFile181 extends ChFile {
    private static final int DATA_START = 6144; // https://github.com/chemplexity/chromatography/blob/master/Development/File%20Conversion/ImportAgilentFID.m
    private static final int START_TIME_POSITION = 282;
    private static final int END_TIME_POSITION = 286;
    private static final int Y_OFFSET_POSITION = 4724;
    private static final int Y_SCALING_POSITION = 4732;
    private static final int DETECTOR_POSITION = 4213;

    ChFile181(RandomAccessFile input) throws IOException {
        super(input, DATA_START, START_TIME_POSITION, END_TIME_POSITION, Y_OFFSET_POSITION, Y_SCALING_POSITION, DETECTOR_POSITION);
    }

    @Override
    protected void parseData(RandomAccessFile input) throws IOException {
        input.seek(DATA_START);

        values = new ArrayList<>();
        long[] buffer = new long[]{0,0,0};

        boolean endOfFile = false;

        while(!endOfFile){
            try{
                buffer[2]= input.readShort();

                if(buffer[2] != 32767){
                    buffer[1]=buffer[2]+buffer[1];
                    buffer[0]=buffer[1]+buffer[0];
                } else {
                    buffer[0] = (long) input.readShort() << 32;
                    buffer[0] = input.readInt() + buffer[0];
                    buffer[1] = 0;
                }

                values.add(buffer[0] * yScaling + yOffset);

            }
            catch (EOFException e){
                endOfFile = true;
            }
        }
    }
}
