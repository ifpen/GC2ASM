package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.TestConstants;
import fr.ifpen.allotropeconverters.gc.schema.ChromatogramDataCube;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ChromatogramDataCubeMapperTests {

    @Test
    void returnsChromatogramDataCube() throws IOException {
        ChromatogramDataCubeMapper mapper = new ChromatogramDataCubeMapper();

        ChromatogramDataCube chromatogramDataCube = mapper.readChromatogramDataCube(TestConstants.RESOURCE_V_179_D_CH_FILE);
        Assertions.assertNotNull(chromatogramDataCube);
    }

    @Test
    void returnsChromatogramDataCubeDataCorrect() throws IOException {
        ChromatogramDataCubeMapper mapper = new ChromatogramDataCubeMapper();

        ChromatogramDataCube chromatogramDataCube = mapper.readChromatogramDataCube(TestConstants.RESOURCE_V_179_D_CH_FILE);
        Assertions.assertEquals("FID1A, Front Signal", chromatogramDataCube.getLabel());
    }
}
