package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.chemstation.ColumnInformationMapper;
import fr.ifpen.allotropeconverters.gc.schema.ChromatographyColumnDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

class ColumnInformationMapperTests {

    @Test
    void MapperCI() throws Exception {
            URI uri;
            uri = new File("src/test/resources/V179.D").toURI();

            ColumnInformationMapper columnInformationMapper = new ColumnInformationMapper();

            ChromatographyColumnDocument chromatographyColumnDocument = columnInformationMapper.readColumnDocumentFromFile(
                    Paths.get(uri).toString());

            Assertions.assertEquals("19091S-001", chromatographyColumnDocument.getChromatographyColumnPartNumber());
            Assertions.assertEquals("Agilent",chromatographyColumnDocument.getProductManufacturer());
            Assertions.assertEquals(0.2,chromatographyColumnDocument.getColumnInnerDiameter().getValue());
            Assertions.assertEquals("mm",chromatographyColumnDocument.getColumnInnerDiameter().getUnit());
            Assertions.assertEquals(50,chromatographyColumnDocument.getChromatographyColumnLength().getValue());
            Assertions.assertEquals("m",chromatographyColumnDocument.getChromatographyColumnLength().getUnit());
            Assertions.assertEquals(0.50,chromatographyColumnDocument.getChromatographyColumnFilmThickness().getValue());
            Assertions.assertEquals("µm",chromatographyColumnDocument.getChromatographyColumnFilmThickness().getUnit());

    }
}
