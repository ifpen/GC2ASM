package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import org.assertj.core.data.Offset;

import javax.measure.quantity.ElectricCurrent;
import javax.measure.unit.Unit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ChFileTestUtil {

    private static final Offset<Float> FLOAT_OFFSET = Offset.offset(0.000_01f);
    private static final Offset<Double> DOUBLE_OFFSET = Offset.offset(0.000_01);

    private ChFileTestUtil() {}

    static void makeAssertions(ChFile chFile, float startTime, float endTime, Unit<ElectricCurrent> unit, double yScaling, double yOffset,
                               String detector, String operator, String method, String sampleName, String injectionDateTime, int valuesSize,
                               double firstValue) {
        assertThat(chFile.getStartTime()).describedAs("start time").isEqualTo(startTime, FLOAT_OFFSET);
        assertThat(chFile.getEndTime()).describedAs("end time").isEqualTo(endTime, FLOAT_OFFSET);
        assertThat(chFile.getUnit()).describedAs("unit").isEqualTo(unit);
        assertThat(chFile.yScaling).describedAs("y scaling").isEqualTo(yScaling, DOUBLE_OFFSET);
        assertThat(chFile.yOffset).describedAs("y offset").isEqualTo(yOffset, DOUBLE_OFFSET);
        assertThat(chFile.getDetector()).describedAs("detector").isEqualTo(detector);
        assertThat(chFile.getOperator()).describedAs("operator").isEqualTo(operator);
        assertThat(chFile.getMethod()).describedAs("method").isEqualTo(method);
        assertThat(chFile.getSampleName()).describedAs("sample name").isEqualTo(sampleName);
        assertThat(chFile.getInjectionDateTime()).describedAs("injection date-time").isEqualTo(injectionDateTime);

        List<Double> values = chFile.getValues();

        assertThat(values).describedAs("values").hasSize(valuesSize);
        assertThat(values.get(0)).describedAs("first value").isEqualTo(firstValue, DOUBLE_OFFSET);
    }
}
