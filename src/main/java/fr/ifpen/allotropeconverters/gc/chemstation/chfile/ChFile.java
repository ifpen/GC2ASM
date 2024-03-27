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
    protected String analyst;
    protected String method;
    protected String sampleName;
    protected TimeWithFormat injectionTime;

    int dataStart;
    int startTimePosition;
    int endTimePosition;
    int yOffsetPosition;
    int yScalingPosition;
    int detectorPosition;
    int analystPosition;
    int methodPosition;
    int sampleNamePosition;
    int injectionTimePosition;
    Boolean isUtf16;
    String timeFormat;

    protected ChFile(
            RandomAccessFile input,
            int dataStart,
            int startTimePosition,
            int endTimePosition,
            int yOffsetPosition,
            int yScalingPosition,
            int detectorPosition,
            int analystPosition,
            int methodPosition,
            int sampleNamePosition,
            int injectionTimePosition,
            Boolean isUtf16,
            String timeFormat) throws IOException {
        this.dataStart = dataStart;
        this.startTimePosition = startTimePosition;
        this.endTimePosition = endTimePosition;
        this.yOffsetPosition = yOffsetPosition;
        this.yScalingPosition = yScalingPosition;
        this.detectorPosition = detectorPosition;
        this.analystPosition = analystPosition;
        this.methodPosition = methodPosition;
        this.sampleNamePosition = sampleNamePosition;
        this.injectionTimePosition = injectionTimePosition;
        this.isUtf16 = isUtf16;
        this.timeFormat = timeFormat;

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

    public String getAnalyst(){return analyst;}

    private void setAnalyst(String analyst){
        this.analyst = analyst;
    }

    public String getMethod() {return method;}

    private void setMethod(String method) {
        this.method = method;
    }

    public String getSampleName() {
        return sampleName;
    }

    private void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public TimeWithFormat getInjectionTime() {
        return injectionTime;
    }

    private void setInjectionTime(TimeWithFormat injectionTime) {
        this.injectionTime = injectionTime;
    }

    protected void readMetadata(RandomAccessFile input) throws IOException {
        setStartTime(readMetadataTime(input, startTimePosition));
        setEndTime(readMetadataTime(input, endTimePosition));

        input.seek(yOffsetPosition);
        yOffset = input.readDouble();

        input.seek(yScalingPosition);
        yScaling = input.readDouble();

        setDetector(readStringAtPosition(input, detectorPosition, isUtf16));

        setAnalyst(readStringAtPosition(input, analystPosition, isUtf16));

        setMethod(readStringAtPosition(input, methodPosition, isUtf16));

        setSampleName(readStringAtPosition(input, sampleNamePosition, isUtf16));

        setInjectionTime(new TimeWithFormat(
                readStringAtPosition(input, injectionTimePosition, isUtf16),
                timeFormat));
    }
}
