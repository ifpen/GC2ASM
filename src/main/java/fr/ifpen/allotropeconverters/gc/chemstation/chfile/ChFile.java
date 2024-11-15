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

public abstract class ChFile {

    protected static final Unit<ElectricCurrent> PICO_AMPERE_UNIT = SI.PICO(SI.AMPERE);

    protected List<Double> values;
    protected Float startTime;
    protected Float endTime;
    protected Unit<ElectricCurrent> unit;
    protected Double yScaling;
    protected Double yOffset;
    protected String detector;

    int dataStart;
    int startTimePosition;
    int endTimePosition;
    int unitsPosition;
    int yOffsetPosition;
    int yScalingPosition;
    int detectorPosition;

    protected ChFile(RandomAccessFile input, int dataStart, int startTimePosition, int endTimePosition, int unitsPosition, int yOffsetPosition, int yScalingPosition, int detectorPosition) throws IOException {
        this.dataStart = dataStart;
        this.startTimePosition = startTimePosition;
        this.endTimePosition = endTimePosition;
        this.unitsPosition = unitsPosition;
        this.yOffsetPosition = yOffsetPosition;
        this.yScalingPosition = yScalingPosition;
        this.detectorPosition = detectorPosition;

        readMetadata(input);
        parseData(input);
    }

    protected abstract void parseData(RandomAccessFile input) throws IOException;

    public List<Double> getValues() {
        return values;
    }

    protected void setValues(List<Double> values) {
        this.values = values;
    }

    public Float getStartTime() {
        return startTime;
    }

    private void setStartTime(Float startTime) {
        this.startTime = startTime;
    }

    public Float getEndTime() {
        return endTime;
    }

    private void setEndTime(Float endTime) {
        this.endTime = endTime;
    }

    protected Unit<ElectricCurrent> getUnit() {
        return unit;
    }

    public String getUnitSymbol() {
        return unit.toString();
    }

    private void setUnit(String unit) {
        Unit<? extends Quantity> localUnit = Unit.valueOf(unit);

        if (!PICO_AMPERE_UNIT.isCompatible(localUnit)) {
            throw new IllegalArgumentException("Unsupported unit: " + localUnit);
        }

        this.unit = localUnit.asType(ElectricCurrent.class);
    }

    public String getDetector() {
        return detector;
    }

    private void setDetector(String detector) {
        this.detector = detector;
    }

    protected void readMetadata(RandomAccessFile input) throws IOException {
        setStartTime(readMetadataTime(input, startTimePosition));
        setEndTime(readMetadataTime(input, endTimePosition));
        setUnit(readStringAtPosition(input, unitsPosition, true));

        input.seek(yOffsetPosition);
        yOffset = input.readDouble();

        input.seek(yScalingPosition);
        yScaling = input.readDouble();

        setDetector(readStringAtPosition(input, detectorPosition, true));
    }
}
