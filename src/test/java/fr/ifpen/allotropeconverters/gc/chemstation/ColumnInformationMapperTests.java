package fr.ifpen.allotropeconverters.gc.chemstation;

import java.io.IOException;

import fr.ifpen.allotropeconverters.gc.schema.ChromatographyColumnDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ColumnInformationMapperTests {

    @Test
    void mapperCI() throws IOException {
        readAndAssertColumnInformation("src/test/resources/V179.D");
    }

    @Test
    void mapperCI_withMultipleColumnInformationPerLine() throws IOException {
        readAndAssertColumnInformation("src/test/resources/V179_2.D");
    }

    private static void readAndAssertColumnInformation(String folderPath) throws IOException {
        ColumnInformationMapper columnInformationMapper = new ColumnInformationMapper();

        ChromatographyColumnDocument chromatographyColumnDocument = columnInformationMapper.readColumnDocumentFromFile(
                folderPath);

        Assertions.assertEquals("19091S-001", chromatographyColumnDocument.getChromatographyColumnPartNumber());
        Assertions.assertEquals("Agilent", chromatographyColumnDocument.getProductManufacturer());
        Assertions.assertEquals(0.2, chromatographyColumnDocument.getColumnInnerDiameter().getValue());
        Assertions.assertEquals("mm", chromatographyColumnDocument.getColumnInnerDiameter().getUnit());
        Assertions.assertEquals(50, chromatographyColumnDocument.getChromatographyColumnLength().getValue());
        Assertions.assertEquals("m", chromatographyColumnDocument.getChromatographyColumnLength().getUnit());
        Assertions.assertEquals(0.50, chromatographyColumnDocument.getChromatographyColumnFilmThickness().getValue());
        Assertions.assertEquals("µm", chromatographyColumnDocument.getChromatographyColumnFilmThickness().getUnit());
    }
}
