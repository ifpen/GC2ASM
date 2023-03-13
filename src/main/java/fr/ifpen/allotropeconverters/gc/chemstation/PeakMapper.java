package fr.ifpen.allotropeconverters.gc.chemstation;

import fr.ifpen.allotropeconverters.gc.schema.*;

public class PeakMapper {

    PeakMapper(){}

    public Peak mapPeakFromCompound(CompoundType compoundType) {
        Peak peak = new Peak();
        peak.setIdentifier(
                compoundType.getCompoundID().toString());
        peak.setWrittenName(
                compoundType.getName());

        PeakHeight peakHeight = new PeakHeight();
        peakHeight.setValue(Double.parseDouble(compoundType.getHeight().getContent()));
        peakHeight.setUnit(compoundType.getHeight().getUnit());
        peak.setPeakHeight(peakHeight);


        if (compoundType.getPlatesHalfWidth() != null) {
            NumberOfTheoreticalPlatesChromatography theoreticalPlatesChromatographyHalfWidth =
                    new NumberOfTheoreticalPlatesChromatography();
            theoreticalPlatesChromatographyHalfWidth.setValue(Double.parseDouble(compoundType.getPlatesHalfWidth().getContent()));
            theoreticalPlatesChromatographyHalfWidth.setUnit(compoundType.getPlatesHalfWidth().getUnit());
            peak.setNumberOfTheoreticalPlatesChromatography(theoreticalPlatesChromatographyHalfWidth);
        }

        if (compoundType.getMeasRetTime() != null) {
            RetentionTime retentionTime =
                    new RetentionTime();
            double fileValue = Double.parseDouble(compoundType.getMeasRetTime().getContent());
            String unit = compoundType.getMeasRetTime().getUnit();
            if(unit.equals("min")){
                fileValue = fileValue * 60;
                unit = "s";
            }
            retentionTime.setValue(fileValue);
            retentionTime.setUnit(unit);
            peak.setRetentionTime(retentionTime);
        }

        if (compoundType.getArea() != null) {
            PeakArea peakArea =
                    new PeakArea();
            peakArea.setValue(Double.parseDouble(compoundType.getArea().getContent()));
            peakArea.setUnit(formatUnitAsSI(compoundType.getArea().getUnit()));
            peak.setPeakArea(peakArea);
        }

        if (compoundType.getAmount() != null) {
            RelativePeakAnalyteAmount relativePeakAnalyteAmount =
                    new RelativePeakAnalyteAmount();
            relativePeakAnalyteAmount.setValue(Double.parseDouble(compoundType.getAmount().getContent()));
            relativePeakAnalyteAmount.setUnit(compoundType.getAmount().getUnit());
            peak.setRelativePeakAnalyteAmount(relativePeakAnalyteAmount);
        }

        return peak;
    }

        private String formatUnitAsSI(String unit){
            return unit.replace("*", ".");
        }
    }
