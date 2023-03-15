package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.*;
import java.util.ArrayList;

import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readLittleEndianDouble;

class ChFile179 extends ChFile {
    private static final int DATA_START = 6144; // https://github.com/CINF/PyExpLabSys/blob/master/PyExpLabSys/file_parsers/chemstation.py
    private static final int START_TIME_POSITION = 282;
    private static final int END_TIME_POSITION = 286;
    private static final int Y_OFFSET_POSITION = 4724;
    private static final int Y_SCALING_POSITION = 4732;
    private static final int DETECTOR_POSITION = 4213;

    ChFile179(RandomAccessFile input) throws IOException {
        super(input, DATA_START, START_TIME_POSITION, END_TIME_POSITION, Y_OFFSET_POSITION, Y_SCALING_POSITION, DETECTOR_POSITION);
    }


    @Override
    protected void parseData(RandomAccessFile input) throws IOException {

        long numberOfPoints = (input.length() - DATA_START) / 8;

        input.seek(DATA_START);

        setValues(new ArrayList<>());
        for(int i=0; i<numberOfPoints;i++){
            getValues().add(readLittleEndianDouble(input) * yScaling);
        }
    }
}
