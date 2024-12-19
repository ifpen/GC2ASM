package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import javax.measure.converter.UnitConverter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readLittleEndianDouble;

class ChFile179 extends ChFile {

    // https://github.com/CINF/PyExpLabSys/blob/master/PyExpLabSys/file_parsers/chemstation.py
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

    ChFile179(RandomAccessFile input) throws IOException {
        super(input, DATA_START, START_TIME_POSITION, END_TIME_POSITION, UNITS_POSITION, Y_OFFSET_POSITION, Y_SCALING_POSITION,
              DETECTOR_POSITION, OPERATOR_POSITION, METHOD_POSITION, SAMPLE_NAME_POSITION, INJECTION_DATE_TIME_POSITION);
    }

    @Override
    protected void parseData(RandomAccessFile input) throws IOException {
        long numberOfPoints = (input.length() - DATA_START) / 8;
        if (numberOfPoints > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Input too large to parse");
        }

        values = new ArrayList<>((int) numberOfPoints);
        UnitConverter unitConverter = unit.getConverterTo(PICO_AMPERE_UNIT);

        input.seek(DATA_START);

        for (int i = 0; i < numberOfPoints; i++) {
            values.add(unitConverter.convert(readLittleEndianDouble(input) * yScaling + yOffset));
        }
    }
}
