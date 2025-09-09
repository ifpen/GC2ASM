package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.allotrope_models.ChromatographyColumnDocument;
import fr.ifpen.allotropeconverters.allotrope_models.ChromatographyColumnDocumentChromatographyColumnLength;
import fr.ifpen.allotropeconverters.allotrope_models.ChromatographyColumnDocumentColumnInnerDiameter;
import fr.ifpen.allotropeconverters.gc.TestConstants;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ColumnInformationMapperTests {

    private static void readAndAssertColumnInformation(Path folderPath, String acqTxtFilename) throws IOException {
        ColumnInformationMapper columnInformationMapper = new ColumnInformationMapper();

        ChromatographyColumnDocument chromatographyColumnDocument =
                columnInformationMapper.readColumnDocumentFromFile(folderPath, acqTxtFilename);

        assertThat(chromatographyColumnDocument.getChromatographyColumnPartNumber()).isEqualTo("19091S-001");
        assertThat(chromatographyColumnDocument.getProductManufacturer()).isEqualTo("Agilent");

        ChromatographyColumnDocumentColumnInnerDiameter columnInnerDiameter =
                chromatographyColumnDocument.getColumnInnerDiameter();
        assertThat(columnInnerDiameter.getValue()).isEqualTo(0.2);
        assertThat(columnInnerDiameter.getUnit().toString()).hasToString("mm");

        ChromatographyColumnDocumentChromatographyColumnLength chromatographyColumnLength = chromatographyColumnDocument.getChromatographyColumnLength();
        assertThat(chromatographyColumnLength.getValue()).isEqualTo(50);
        assertThat(chromatographyColumnLength.getUnit().toString()).hasToString("m");
    }

    private static void read179(String acqTxtFilename) throws IOException {
        readAndAssertColumnInformation(TestConstants.RESOURCE_V_179_D_FOLDER, acqTxtFilename);
    }

    @Test
    void mapperCI() throws IOException {
        read179("acq.txt");
    }

    @Test
    void mapperCI_withMultipleColumnInformationPerLine() throws IOException {
        read179("acq_multipleColumnInformation.txt");
    }
}
