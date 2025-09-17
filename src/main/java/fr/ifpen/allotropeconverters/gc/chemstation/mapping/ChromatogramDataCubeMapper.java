package fr.ifpen.allotropeconverters.gc.chemstation.mapping;

import fr.ifpen.allotropeconverters.allotrope_models.*;
import fr.ifpen.allotropeconverters.gc.MathUtils;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFileFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

class ChromatogramDataCubeMapper {

    ChromatogramDataCube readChromatogramDataCube(Path chFilePath) throws IOException {
        ChFileFactory chFileFactory = new ChFileFactory();
        ChFile chFile = chFileFactory.getChFile(chFilePath);

        return readChromatogramDataCube(chFile);
    }

    ChromatogramDataCube readChromatogramDataCube(ChFile chFile) {
        ChromatogramDataCube chromatogramDataCube = new ChromatogramDataCube();
        chromatogramDataCube.setLabel(chFile.getDetector());
        chromatogramDataCube.setDatacubeStructure(getCubeStructure(chFile.getDetector()));
        chromatogramDataCube.setDatacubeData(createAllotropeDataFromChFile(chFile));

        return chromatogramDataCube;
    }

    private ChromatogramDatacubeStructure getCubeStructure(String detector) {
        ChromatogramDatacubeStructure cubeStructure = new ChromatogramDatacubeStructure();

        ChromatogramDimension firstDimension = new ChromatogramDimension();
        firstDimension.setConcept(ChromatogramDimension.ConceptEnum.RETENTION_TIME);
        firstDimension.setUnit(ChromatogramDimension.UnitEnum.S);

        ChromatogramMeasure firstMeasure = new ChromatogramMeasure();
        if (detector.startsWith("FID")) {
            firstMeasure.setConcept("electric current");
            firstMeasure.setUnit("pA");

        } else if (detector.startsWith("TCD")) {
            firstMeasure.setConcept("electric potential");
            firstMeasure.setUnit("mV");
        }

        List<ChromatogramDimension> dimensionList = List.of(firstDimension);
        List<ChromatogramMeasure> measureList = List.of(firstMeasure);

        cubeStructure.setDimensions(dimensionList);
        cubeStructure.setMeasures(measureList);
        return cubeStructure;
    }

    private DatacubeData createAllotropeDataFromChFile(ChFile chFile) {
        Double[] xValues = MathUtils.interpolate(chFile.getStartTime(), chFile.getEndTime(), chFile.getValues().size());
        DatacubeData datacubeData = new DatacubeData();
        datacubeData.setMeasures(List.of(Arrays.asList(xValues)));
        datacubeData.setDimensions(List.of(chFile.getValues()));
        return datacubeData;
    }
}
