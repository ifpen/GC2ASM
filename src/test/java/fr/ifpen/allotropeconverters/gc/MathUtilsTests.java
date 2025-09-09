package fr.ifpen.allotropeconverters.gc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MathUtilsTests {

    @Test
    void returnsCorrectInterpolationForIntegerValues() {
        var expectedLength = 11;
        var result = MathUtils.interpolate(0, 10, expectedLength);

        Assertions.assertEquals(expectedLength, result.length);

        Assertions.assertArrayEquals(new Double[]{0d, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d}, result);
    }

    @Test
    void returnsCorrectInterpolationForDecimalValues() {
        var expectedLength = 5;

        var result = MathUtils.interpolate(0, 2, expectedLength);

        Assertions.assertEquals(expectedLength, result.length);

        Assertions.assertArrayEquals(new Double[]{0d, 0.5d, 1d, 1.5d, 2d,}, result);
    }

    @Test
    void throwsExceptionForIllegalCount() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MathUtils.interpolate(0, 10, 0));
    }
}
