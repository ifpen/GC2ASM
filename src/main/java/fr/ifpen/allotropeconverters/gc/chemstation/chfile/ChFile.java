package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readMetadataTime;
import static fr.ifpen.allotropeconverters.gc.chemstation.chfile.ReadHelpers.readStringAtPosition;

public abstract class ChFile {

    protected List<Double> values;
    protected Float startTime;
    protected Float endTime;
    protected Double yScaling;
    protected Double yOffset;
    protected String detector;

    int dataStart;
    int startTimePosition;
    int endTimePosition;
    int yOffsetPosition;
    int yScalingPosition;
    int detectorPosition;

    protected ChFile(RandomAccessFile input, int dataStart, int startTimePosition, int endTimePosition, int yOffsetPosition, int yScalingPosition, int detectorPosition) throws IOException {
        this.dataStart = dataStart;
        this.startTimePosition = startTimePosition;
        this.endTimePosition = endTimePosition;
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

    public String getDetector() {
        return detector;
    }

    private void setDetector(String detector) {
        this.detector = detector;
    }

    protected void readMetadata(RandomAccessFile input) throws IOException {
        setStartTime(readMetadataTime(input, startTimePosition));
        setEndTime(readMetadataTime(input, endTimePosition));

        input.seek(yOffsetPosition);
        yOffset = input.readDouble();

        input.seek(yScalingPosition);
        yScaling = input.readDouble();

        setDetector(readStringAtPosition(input, detectorPosition, true));
    }
}
