package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.schema.ChromatographyColumnDocument;
import fr.ifpen.allotropeconverters.gc.schema.ChromatographyColumnFilmThickness;
import fr.ifpen.allotropeconverters.gc.schema.ChromatographyColumnLength;
import fr.ifpen.allotropeconverters.gc.schema.ColumnInnerDiameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_16;

public final class ColumnInformationMapper {

    private static final String COLON_REGEX = "\\s*:\\s*";
    private static final String SEPARATOR_REGEX = "\\s*";
    private static final String TEXT_REGEX = "(\\S+)";
    private static final String NUMBER_REGEX = "([\\d.]+)";
    private static final String COLUMN_SEPARATOR_REGEX = "(?>\\s+|\\n+)";

    private static final Map<String, Boolean> COLUMN_NAMES_MAP = new LinkedHashMap<>();
    private static final Pattern COLUMN_PATTERN;

    static {
        COLUMN_NAMES_MAP.put("Model#", true);
        COLUMN_NAMES_MAP.put("Manufacturer", true);
        COLUMN_NAMES_MAP.put("Diameter", false);
        COLUMN_NAMES_MAP.put("Length", false);
        COLUMN_NAMES_MAP.put("Film thickness", false);
    }

    static {
        StringBuilder pattern = new StringBuilder();

        COLUMN_NAMES_MAP.forEach((columnName, isOnlyText) -> {
            pattern.append(columnName).append(COLON_REGEX);

            if (!isOnlyText) {
                pattern.append(NUMBER_REGEX).append(SEPARATOR_REGEX);
            }

            pattern.append(TEXT_REGEX).append(COLUMN_SEPARATOR_REGEX);
        });

        COLUMN_PATTERN = Pattern.compile(pattern.toString(), Pattern.MULTILINE);
    }

    public ChromatographyColumnDocument readColumnDocumentFromFile(String folderPath, String txtFileName) throws IOException {
        ChromatographyColumnDocument columnDocument = new ChromatographyColumnDocument();

        File file = new File(folderPath, txtFileName);
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, UTF_16);
             Scanner acquisitionScanner = new Scanner(inputStreamReader)) {

            acquisitionScanner.useLocale(Locale.US); //Agilent files are US formatted.

            skipToColumnInformation(acquisitionScanner);

            MatchResult columnInformation = acquisitionScanner.findAll(COLUMN_PATTERN)
                                                              .findFirst()
                                                              .orElseThrow(
                                                                      () -> new NoSuchElementException("Incorrect column information"));

            int groupIndex = 1;

            columnDocument.setChromatographyColumnPartNumber(columnInformation.group(groupIndex++)); //Model
            columnDocument.setProductManufacturer(columnInformation.group(groupIndex++)); //Manufacturer

            ColumnInnerDiameter columnInnerDiameter = new ColumnInnerDiameter();
            double value = Double.parseDouble(columnInformation.group(groupIndex++));
            String unit = columnInformation.group(groupIndex++);

            if (unit.equals("µm")) { //Allotrope format forces mm.
                unit = "mm";
                value = value / 1000;
            }
            columnInnerDiameter.setValue(value);
            columnInnerDiameter.setUnit(unit);
            columnDocument.setColumnInnerDiameter(columnInnerDiameter);

            ChromatographyColumnLength chromatographyColumnLength = new ChromatographyColumnLength();
            chromatographyColumnLength.setValue(Double.parseDouble(columnInformation.group(groupIndex++)));
            chromatographyColumnLength.setUnit(columnInformation.group(groupIndex++));
            columnDocument.setChromatographyColumnLength(chromatographyColumnLength);

            ChromatographyColumnFilmThickness columnFilmThickness = new ChromatographyColumnFilmThickness();
            columnFilmThickness.setValue(Double.parseDouble(columnInformation.group(groupIndex++)));
            columnFilmThickness.setUnit(columnInformation.group(groupIndex++));
            columnDocument.setChromatographyColumnFilmThickness(columnFilmThickness);

            columnDocument.setChromatographyColumnSerialNumber("N/A");

            return columnDocument;
        } catch (InputMismatchException e) {
            return new ChromatographyColumnDocument();
        }
    }

    private void skipToColumnInformation(Scanner acquisitionScanner) {
                /* Looking for pattern
        =====================================================================
                          Column(s)
        =====================================================================

        Column Description :  HP-PONA
         */
        boolean columnSectionFound = false;
        String line;
        while ((line = acquisitionScanner.nextLine()) != null) {
            if (line.contains("======")) {
                line = acquisitionScanner.nextLine();
                if (line.contains("Column(s)")) {
                    acquisitionScanner.nextLine();// === line
                    acquisitionScanner.nextLine();// empty line
                    columnSectionFound = true;
                    break;
                }
            }
        }

        if (!columnSectionFound) {
            throw new NoSuchElementException("No column information found");
        }

        acquisitionScanner.nextLine(); //Column Description - Not in model
        acquisitionScanner.nextLine(); //Inventory # - Not in model
    }
}
