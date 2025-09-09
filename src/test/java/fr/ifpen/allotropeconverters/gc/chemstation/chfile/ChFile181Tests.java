package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import fr.ifpen.allotropeconverters.gc.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.measure.unit.SI;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

class ChFile181Tests {

    @Test
    void getVersionReturnsExpected() throws IOException {
        URI uri = TestConstants.RESOURCE_V_181_D_CH_FILE.toUri();
        RandomAccessFile file = new RandomAccessFile(uri.getPath(), "r");
        ChFile chFile = new ChFile181(file);

        Assertions.assertNotNull(chFile);

        ChFileTestUtil.makeAssertions(chFile, 0, 1182.3239f, SI.PICO(SI.AMPERE), 1.302_083_33E-4, 0.0, "", "SYSTEM", "DET3300.M",
                                      "140+H", "23-Aug-22, 12:48:20", 5914, 2.101_041);
    }
}
