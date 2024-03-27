package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.*;

import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readDoubleDeltaStream;

class ChFile81 extends ChFile {
    private static final int DATA_START = 1024;
    private static final int START_TIME_POSITION = 282;
    private static final int END_TIME_POSITION = 286;
    private static final int Y_OFFSET_POSITION = 636;
    private static final int Y_SCALING_POSITION = 644;
    private static final int DETECTOR_POSITION = 208;
    private static final int ANALYST_POSITION = 148;
    private static final int INJECTION_TIME_POSITION = 178;

    ChFile81(RandomAccessFile input) throws IOException {
        super(
                input,
                DATA_START,
                START_TIME_POSITION,
                END_TIME_POSITION,
                Y_OFFSET_POSITION,
                Y_SCALING_POSITION,
                DETECTOR_POSITION,
                ANALYST_POSITION,
                0,
                0,
                INJECTION_TIME_POSITION,
                false,
                "dd/MM/yyyy HH:mm:ss");
    }


    @Override
    protected void parseData(RandomAccessFile input) throws IOException {
        values = readDoubleDeltaStream(input, DATA_START, yScaling, yOffset);
    }
}
