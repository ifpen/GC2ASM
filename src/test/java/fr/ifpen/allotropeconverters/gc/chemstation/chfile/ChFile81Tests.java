package fr.ifpen.allotropeconverters.gc.chemstation.chfile;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.List;

class ChFile81Tests {
    @Test
    void GetVersionReturnsExpected() throws IOException {
        URI uri = new File("src/test/resources/V81.D/FID1B.ch").toURI();
        RandomAccessFile file = new RandomAccessFile(uri.getPath(), "r");
        ChFile chFile = new ChFile81(file);

        List<Double> values = chFile.getValues();

        Assertions.assertEquals(71841, values.size());
        Assertions.assertEquals(1.575, values.get(0), 0.001);
    }
}
