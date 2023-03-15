package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.schema.ChromatogramDataCube;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ChromatogramDataCubeMapperTests {
    @Test
    void ReturnsChromatogramDataCube() throws IOException {

        ChromatogramDataCubeMapper mapper = new ChromatogramDataCubeMapper();

        ChromatogramDataCube chromatogramDataCube =
                mapper.readChromatogramDataCube("src/test/resources/V179.D/FID1A.ch");
        Assertions.assertNotNull(chromatogramDataCube);
    }

    @Test
    void ReturnsChromatogramDataCubeDataCorrect() throws IOException {

        ChromatogramDataCubeMapper mapper = new ChromatogramDataCubeMapper();

        ChromatogramDataCube chromatogramDataCube =
                mapper.readChromatogramDataCube("src/test/resources/V179.D/FID1A.ch");
        Assertions.assertEquals("FID1A, Front Signal", chromatogramDataCube.getLabel());
    }
}
