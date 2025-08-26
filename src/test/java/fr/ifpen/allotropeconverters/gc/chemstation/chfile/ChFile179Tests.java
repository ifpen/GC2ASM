package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import fr.ifpen.allotropeconverters.gc.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.measure.unit.SI;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

class ChFile179Tests {

    @Test
    void getVersionReturnsExpected() throws IOException {
        URI uri = TestConstants.RESOURCE_V_179_D_CH_FILE.toUri();
        RandomAccessFile file = new RandomAccessFile(uri.getPath(), "r");
        ChFile chFile = new ChFile179(file);

        Assertions.assertNotNull(chFile);

        ChFileTestUtil.makeAssertions(chFile, 0.0f, 14367.8f, SI.PICO(SI.AMPERE), 1.302_083_33E-4, 0.0, "FID1A, Front Signal", "SYSTEM",
                                      "DET401.M", "22-00465-1", "12-May-22, 11:24:28", 71_840, 2.165_234);
    }
}
