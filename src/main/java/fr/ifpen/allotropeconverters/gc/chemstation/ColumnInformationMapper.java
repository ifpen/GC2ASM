package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.schema.ChromatographyColumnDocument;
import fr.ifpen.allotropeconverters.gc.schema.ChromatographyColumnFilmThickness;
import fr.ifpen.allotropeconverters.gc.schema.ChromatographyColumnLength;
import fr.ifpen.allotropeconverters.gc.schema.ColumnInnerDiameter;

import java.io.*;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_16;

public final class ColumnInformationMapper {

    private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile(".*:");

    public ChromatographyColumnDocument readColumnDocumentFromFile(String folderPath) throws IOException {
        ChromatographyColumnDocument columnDocument = new ChromatographyColumnDocument();

        File file = new File(folderPath,"acq.txt");
        try (FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, UTF_16);
        Scanner acquisitionScanner = new Scanner(inputStreamReader)){

            acquisitionScanner.useLocale(Locale.US); //Agilent files are US formatted.

            skipToColumnInformation(acquisitionScanner);

            acquisitionScanner.skip(COLUMN_NAME_PATTERN);
            columnDocument.setChromatographyColumnPartNumber(acquisitionScanner.next()); //Model
            acquisitionScanner.nextLine();

            acquisitionScanner.skip(COLUMN_NAME_PATTERN);
            columnDocument.setProductManufacturer(acquisitionScanner.next()); //Manufacturer
            acquisitionScanner.nextLine();

            ColumnInnerDiameter columnInnerDiameter = new ColumnInnerDiameter();
            acquisitionScanner.skip(COLUMN_NAME_PATTERN);
            double value = acquisitionScanner.nextDouble();
            String unit = acquisitionScanner.next();

            if(unit.equals("µm")){ //Allotrope format forces mm.
                unit = "mm";
                value = value / 1000;
            }

            columnInnerDiameter.setValue(value);
            columnInnerDiameter.setUnit(unit);
            columnDocument.setColumnInnerDiameter(columnInnerDiameter);
            acquisitionScanner.nextLine();

            ChromatographyColumnLength chromatographyColumnLength = new ChromatographyColumnLength();
            acquisitionScanner.skip(COLUMN_NAME_PATTERN);
            chromatographyColumnLength.setValue(acquisitionScanner.nextDouble());
            chromatographyColumnLength.setUnit(acquisitionScanner.next());
            columnDocument.setChromatographyColumnLength(chromatographyColumnLength);
            acquisitionScanner.nextLine();

            ChromatographyColumnFilmThickness columnFilmThickness = new ChromatographyColumnFilmThickness();
            acquisitionScanner.skip(COLUMN_NAME_PATTERN);
            columnFilmThickness.setValue(acquisitionScanner.nextDouble());
            columnFilmThickness.setUnit(acquisitionScanner.next());
            columnDocument.setChromatographyColumnFilmThickness(columnFilmThickness);

            columnDocument.setChromatographyColumnSerialNumber("N/A");

            return columnDocument;
        } catch (InputMismatchException e){
            return new ChromatographyColumnDocument();
        }
    }

    private void skipToColumnInformation(Scanner acquisitionScanner){
                /* Looking for pattern
        =====================================================================
                          Column(s)
        =====================================================================

        Column Description :  HP-PONA
         */
        String line;
        while ((line = acquisitionScanner.nextLine()) != null) {
            if (line.contains("======")) {
                line = acquisitionScanner.nextLine();
                if (line.contains("Column(s)")) {
                    acquisitionScanner.nextLine();// === line
                    acquisitionScanner.nextLine();// empty line
                    break;
                }
            }
        }
        acquisitionScanner.nextLine(); //Column Description - Not in model
        acquisitionScanner.nextLine(); //Inventory # - Not in model
    }
}
