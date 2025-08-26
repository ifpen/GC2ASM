package fr.ifpen.allotropeconverters.gc;

import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapper;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapperBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MathUtilsTests {

    @Test
    void returnsCorrectInterpolationForIntegerValues() throws Exception {
        var result = MathUtils.interpolate(0, 10, 11);

        Assertions.assertEquals(11, result.length);

        Assertions.assertArrayEquals(new Double[]{0d, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d}, result);
    }

    void returnsCorrectInterpolationForDecimalValues() throws Exception {
        var result = MathUtils.interpolate(0, 2, 5);

        Assertions.assertEquals(5, result.length);

        Assertions.assertArrayEquals(new Double[]{0d, 0.5d, 1d, 1.5d, 2d,}, result);
    }

    @Test
    void throwsExceptionForIllegalCount() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MathUtils.interpolate(0, 10, 0));
    }
}
