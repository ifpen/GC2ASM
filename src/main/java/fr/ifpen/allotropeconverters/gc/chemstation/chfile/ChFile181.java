package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.IOException;
import java.io.RandomAccessFile;

import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readDoubleDeltaStream;

class ChFile181 extends ChFile {
    private static final int DATA_START = 6144; // https://github.com/chemplexity/chromatography/blob/master/Development/File%20Conversion/ImportAgilentFID.m
    private static final int START_TIME_POSITION = 282;
    private static final int END_TIME_POSITION = 286;
    private static final int Y_OFFSET_POSITION = 4724;
    private static final int Y_SCALING_POSITION = 4732;
    private static final int DETECTOR_POSITION = 4213;
    private static final int ANALYST_POSITION = 1880;

    ChFile181(RandomAccessFile input) throws IOException {
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
                0,
                true,
                "");
    }

    @Override
    protected void parseData(RandomAccessFile input) throws IOException {
        values = readDoubleDeltaStream(input, DATA_START, yScaling, yOffset);
    }
}
