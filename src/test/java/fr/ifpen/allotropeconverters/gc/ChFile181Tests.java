package fr.ifpen.allotropeconverters.gc;

import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile181;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.List;

class ChFile181Tests {
    @Test
    void GetVersionReturnsExpected() throws IOException {
        URI uri = new File("src/test/resources/V181.D/V181.ch").toURI();
        RandomAccessFile file = new RandomAccessFile(uri.getPath(), "r");
        ChFile chFile = new ChFile181(file);

        List<Double> values = chFile.getValues();

        Assertions.assertEquals(5914, values.size());
        Assertions.assertEquals(2.1010, values.get(0), 0.001);
    }
}