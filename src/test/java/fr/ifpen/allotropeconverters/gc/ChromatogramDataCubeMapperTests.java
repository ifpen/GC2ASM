package fr.ifpen.allotropeconverters.gc;

import fr.ifpen.allotropeconverters.gc.schema.ChromatogramDataCube;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static fr.ifpen.allotropeconverters.gc.chemstation.ChromatogramDataCubeMapper.readChromatogramDataCube;

class ChromatogramDataCubeMapperTests {
    @Test
    void ReturnsChromatogramDataCube() throws IOException {
        ChromatogramDataCube chromatogramDataCube =
                readChromatogramDataCube("src/test/resources/V179.D/FID1A.ch");
        Assertions.assertNotNull(chromatogramDataCube);
    }

    @Test
    void ReturnsChromatogramDataCubeDataCorrect() throws IOException {
        ChromatogramDataCube chromatogramDataCube =
                readChromatogramDataCube("src/test/resources/V179.D/FID1A.ch");
        Assertions.assertEquals("FID1A, Front Signal", chromatogramDataCube.getLabel());
    }
}
