package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import javax.measure.converter.UnitConverter;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

class ChFile181 extends ChFile {

    // https://github.com/chemplexity/chromatography/blob/master/Development/File%20Conversion/ImportAgilentFID.m
    private static final int DATA_START = 6144;
    private static final int START_TIME_POSITION = 282;
    private static final int END_TIME_POSITION = 286;
    private static final int UNITS_POSITION = 4172;
    private static final int Y_OFFSET_POSITION = 4724;
    private static final int Y_SCALING_POSITION = 4732;
    private static final int DETECTOR_POSITION = 4213;
    private static final int OPERATOR_POSITION = 1880;
    private static final int METHOD_POSITION = 2574;
    private static final int SAMPLE_NAME_POSITION = 858;
    private static final int INJECTION_DATE_TIME_POSITION = 2391;

    ChFile181(RandomAccessFile input) throws IOException {
        super(input, DATA_START, START_TIME_POSITION, END_TIME_POSITION, UNITS_POSITION, Y_OFFSET_POSITION, Y_SCALING_POSITION,
              DETECTOR_POSITION, OPERATOR_POSITION, METHOD_POSITION, SAMPLE_NAME_POSITION, INJECTION_DATE_TIME_POSITION);
    }

    @Override
    protected void parseData(RandomAccessFile input) throws IOException {
        input.seek(DATA_START);

        values = new ArrayList<>();
        long[] buffer = new long[] {0, 0, 0};

        UnitConverter unitConverter = unit.getConverterTo(PICO_AMPERE_UNIT);

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

                values.add(unitConverter.convert(buffer[0] * yScaling + yOffset));
            } catch (EOFException e) {
                endOfFile = true;
            }
        }
    }
}
