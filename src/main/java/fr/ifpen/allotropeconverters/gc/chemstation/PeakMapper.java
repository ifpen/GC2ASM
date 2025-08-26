package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.allotrope_models.*;

public class PeakMapper {

    PeakMapper() {}

    public Peak mapPeakFromCompound(CompoundType compoundType) {
        Peak peak = new Peak();
        peak.setIdentifier(compoundType.getCompoundID().toString());
        peak.setWrittenName(compoundType.getName());

        PeakPeakHeight peakHeight = new PeakPeakHeight();
        peakHeight.setValue(Double.parseDouble(compoundType.getHeight().getContent()));
        peakHeight.setUnit(compoundType.getHeight().getUnit());
        peak.setPeakHeight(peakHeight);

        if (compoundType.getPlatesHalfWidth() != null) {
            PeakNumberOfTheoreticalPlatesByPeakWidthAtHalfHeight theoreticalPlatesChromatographyHalfWidth =
                    new PeakNumberOfTheoreticalPlatesByPeakWidthAtHalfHeight();
            theoreticalPlatesChromatographyHalfWidth.setValue(Double.parseDouble(compoundType.getPlatesHalfWidth().getContent()));
            theoreticalPlatesChromatographyHalfWidth.setUnit(PeakNumberOfTheoreticalPlatesByPeakWidthAtHalfHeight.UnitEnum._UNITLESS_);
            peak.setNumberOfTheoreticalPlatesByPeakWidthAtHalfHeight(theoreticalPlatesChromatographyHalfWidth);
        }

        if (compoundType.getMeasRetTime() != null) {
            PeakRetentionTime retentionTime = getPeakRetentionTime(compoundType);
            peak.setRetentionTime(retentionTime);
        }

        if (compoundType.getArea() != null) {
            PeakPeakArea peakArea = new PeakPeakArea();
            peakArea.setValue(Double.parseDouble(compoundType.getArea().getContent()));
            peakArea.setUnit(formatUnitAsSI(compoundType.getArea().getUnit()));
            peak.setPeakArea(peakArea);
        }

        return peak;
    }

    private static PeakRetentionTime getPeakRetentionTime(CompoundType compoundType) {
        PeakRetentionTime retentionTime = new PeakRetentionTime();
        double fileValue = Double.parseDouble(compoundType.getMeasRetTime().getContent());
        String unit = compoundType.getMeasRetTime().getUnit();

        PeakRetentionTime.UnitEnum unitEnum;

        if (unit.equals("min")) {
            fileValue = fileValue * 60;
        }

        unitEnum = PeakRetentionTime.UnitEnum.S;

        retentionTime.setValue(fileValue);
        retentionTime.setUnit(unitEnum);
        return retentionTime;
    }

    private String formatUnitAsSI(String unit) {
        return unit.replace("*", ".");
    }
}
