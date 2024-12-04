package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.allotropeutils.AllotropeData;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFileFactory;
import fr.ifpen.allotropeconverters.gc.schema.ChromatogramDataCube;
import fr.ifpen.allotropeconverters.gc.schema.CubeStructure;
import fr.ifpen.allotropeconverters.gc.schema.Dimension;
import fr.ifpen.allotropeconverters.gc.schema.Measure;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class ChromatogramDataCubeMapper {

    ChromatogramDataCube readChromatogramDataCube(String chFilePath) throws IOException {
        ChFileFactory chFileFactory = new ChFileFactory();
        ChFile chFile = chFileFactory.getChFile(chFilePath);

        return readChromatogramDataCube(chFile);
    }

    ChromatogramDataCube readChromatogramDataCube(ChFile chFile) {
        ChromatogramDataCube chromatogramDataCube = new ChromatogramDataCube();
        chromatogramDataCube.setLabel(chFile.getDetector());
        chromatogramDataCube.setCubeStructure(getCubeStructure());
        chromatogramDataCube.setData(createAllotropeDataFromChFile(chFile));

        return chromatogramDataCube;
    }

    private CubeStructure getCubeStructure() {
        CubeStructure cubeStructure = new CubeStructure();

        Dimension firstDimension = new Dimension();
        firstDimension.setConcept("time");
        firstDimension.setUnit("min");

        Measure firstMeasure = new Measure();
        firstMeasure.setConcept("electric current");
        firstMeasure.setUnit("pA");

        List<Dimension> dimensionList = List.of(firstDimension);
        List<Measure> measureList = List.of(firstMeasure);

        cubeStructure.setDimensions(dimensionList);
        cubeStructure.setMeasures(measureList);
        return cubeStructure;
    }

    private AllotropeData createAllotropeDataFromChFile(ChFile chFile) {
        Double[] xValues = this.interpolate(chFile.getStartTime(), chFile.getEndTime(), chFile.getValues().size());
        return new AllotropeData(List.of(Arrays.asList(xValues)), List.of(chFile.getValues()));
    }

    /***
     * Interpolating method - https://stackoverflow.com/questions/30182467/how-to-implement-linear-interpolation-method-in-java-array
     * @param start start of the interval
     * @param end end of the interval
     * @param count count of output interpolated numbers
     * @return array of interpolated number with specified count
     */
    private Double[] interpolate(double start, double end, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("interpolate: illegal count!");
        }
        Double[] array = new Double[count + 1];
        for (int i = 0; i <= count; ++i) {
            array[i] = start + i * (end - start) / count;
        }
        return array;
    }
}
