package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readMetadataTime;
import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readStringAtPosition;

/**
 * ChFile is an abstract class that serves as the base class for extracting and processing
 * chromatographic data from binary .ch files. The .ch files typically store metadata and
 * data points related to chromatographic experiments, which this class facilitates reading,
 * parsing, and converting to a standard unit (picoampere).
 * <p>
 * This class manages:
 * - Metadata reading, including start time, end time, units, detector details, operator information, method,
 *   sample name, and injection date/time, among others.
 * - Proper unit conversion and scaling as per the Allotrope standards.
 * <p>
 * Subclasses of ChFile must implement specific parsing logic for different .ch file variants through the
 * `parseData` method.
 */
public abstract class ChFile {

    protected static final Unit<ElectricCurrent> PICO_AMPERE_UNIT = SI.PICO(SI.AMPERE);

    protected final int dataStart; // Has no use for now
    protected final int startTimePosition;
    protected final int endTimePosition;
    protected final int unitsPosition;
    protected final int yOffsetPosition;
    protected final int yScalingPosition;
    protected final int detectorPosition;
    protected final int operatorPosition;
    protected final int methodPosition;
    protected final int sampleNamePosition;
    protected final int injectionDateTimePosition;

    protected List<Double> values;
    protected Float startTime;
    protected Float endTime;
    protected Unit<ElectricCurrent> unit;
    protected Double yScaling;
    protected Double yOffset;
    protected String detector;
    protected String operator;
    protected String method;
    protected String sampleName;
    protected String injectionDateTime;

    protected ChFile(RandomAccessFile input, int dataStart, int startTimePosition, int endTimePosition, int unitsPosition,
                     int yOffsetPosition, int yScalingPosition, int detectorPosition, int operatorPosition, int methodPosition,
                     int sampleNamePosition, int injectionDateTimePosition) throws IOException {
        this.dataStart = dataStart;
        this.startTimePosition = startTimePosition;
        this.endTimePosition = endTimePosition;
        this.unitsPosition = unitsPosition;
        this.yOffsetPosition = yOffsetPosition;
        this.yScalingPosition = yScalingPosition;
        this.detectorPosition = detectorPosition;
        this.operatorPosition = operatorPosition;
        this.methodPosition = methodPosition;
        this.sampleNamePosition = sampleNamePosition;
        this.injectionDateTimePosition = injectionDateTimePosition;

        readMetadata(input);
        parseData(input);
    }

    protected abstract void parseData(RandomAccessFile input) throws IOException;

    /**
     * Returns the values found in the .ch file, converted to picoampere as the standard imposes.
     */
    public List<Double> getValues() {
        return values;
    }

    public Float getStartTime() {
        return startTime;
    }

    public Float getEndTime() {
        return endTime;
    }

    public String getDetector() {
        return detector;
    }

    public String getOperator() {
        return operator;
    }

    public String getMethod() {
        return method;
    }

    public String getSampleName() {
        return sampleName;
    }

    public String getInjectionDateTime() {
        return injectionDateTime;
    }

    /**
     * Returns the unit found in the .ch file.<br>
     * Warning: the values stored in this class are converted to picoampere, as the standard imposes.
     */
    protected Unit<ElectricCurrent> getUnit() {
        return unit;
    }

    private void setUnit(String unit) {
        Unit<? extends Quantity> localUnit = Unit.valueOf(unit);

        if (!PICO_AMPERE_UNIT.isCompatible(localUnit)) {
            throw new IllegalArgumentException("Unsupported unit: " + unit);
        }

        this.unit = localUnit.asType(ElectricCurrent.class);
    }

    private void readMetadata(RandomAccessFile input) throws IOException {
        startTime = readMetadataTime(input, startTimePosition);
        endTime = readMetadataTime(input, endTimePosition);
        setUnit(readStringAtPosition(input, unitsPosition, true));

        input.seek(yOffsetPosition);
        yOffset = input.readDouble();

        input.seek(yScalingPosition);
        yScaling = input.readDouble();

        detector = readStringAtPosition(input, detectorPosition, true);

        operator = readStringAtPosition(input, operatorPosition, true);
        method = readStringAtPosition(input, methodPosition, true);
        sampleName = readStringAtPosition(input, sampleNamePosition, true);
        injectionDateTime = readStringAtPosition(input, injectionDateTimePosition, true);
    }
}
