package fr.ifpen.allotropeconverters.gc.chemstation.mapping;

import fr.ifpen.allotropeconverters.allotrope_models.ChromatographyColumnDocument;
import fr.ifpen.allotropeconverters.allotrope_models.ChromatographyColumnDocumentChromatographyColumnLength;
import fr.ifpen.allotropeconverters.allotrope_models.ChromatographyColumnDocumentColumnInnerDiameter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_16;

final class ColumnInformationMapper {

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

    ChromatographyColumnDocument readColumnDocumentFromFile(Path folderPath, String acqTxtFilename) throws IOException {
        ChromatographyColumnDocument columnDocument = new ChromatographyColumnDocument();

        Path acqTxtPath = folderPath.resolve(acqTxtFilename);

        if (!Files.exists(acqTxtPath)) {
            return getDefaultColumnInformation();
        }

        try (InputStream acqInputStream = Files.newInputStream(acqTxtPath);
             InputStreamReader inputStreamReader = new InputStreamReader(acqInputStream, UTF_16);
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

            ChromatographyColumnDocumentColumnInnerDiameter columnInnerDiameter =
                    new ChromatographyColumnDocumentColumnInnerDiameter();
            double value = Double.parseDouble(columnInformation.group(groupIndex++));
            String rawUnit = columnInformation.group(groupIndex++);
            ChromatographyColumnDocumentColumnInnerDiameter.UnitEnum unit =
                    ChromatographyColumnDocumentColumnInnerDiameter.UnitEnum.MM;

            if (rawUnit.equals("µm")) { //Allotrope format forces mm.
                value = value / 1000;
            }

            columnInnerDiameter.setValue(value);
            columnInnerDiameter.setUnit(unit);
            columnDocument.setColumnInnerDiameter(columnInnerDiameter);

            ChromatographyColumnDocumentChromatographyColumnLength chromatographyColumnLength =
                    new ChromatographyColumnDocumentChromatographyColumnLength();
            chromatographyColumnLength.setValue(Double.parseDouble(columnInformation.group(groupIndex++)));

            rawUnit = columnInformation.group(groupIndex++);
            if (!rawUnit.equals("m")){
                throw new IOException("Unexpected unit for column length: " + rawUnit);
            }

            chromatographyColumnLength.setUnit(ChromatographyColumnDocumentChromatographyColumnLength.UnitEnum.M);
            columnDocument.setChromatographyColumnLength(chromatographyColumnLength);

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

    ChromatographyColumnDocument getDefaultColumnInformation() {
        ChromatographyColumnDocument columnDocument = new ChromatographyColumnDocument();
        columnDocument.setChromatographyColumnSerialNumber("N/A");
        return columnDocument;
    }
}
