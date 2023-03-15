package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.List;

class ChFile179Tests {
    @Test
    void GetVersionReturnsExpected() throws IOException {
        URI uri = new File("src/test/resources/V179.D/FID1A.ch").toURI();
        RandomAccessFile file = new RandomAccessFile(uri.getPath(), "r");
        ChFile chFile = new ChFile179(file);

        List<Double> values = chFile.getValues();

        Assertions.assertEquals(71840, values.size());
        Assertions.assertEquals(2.165234, values.get(0), 0.001);
    }
}
