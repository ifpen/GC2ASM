package fr.ifpen.allotropeconverters.gc.chemstation.mapping;

import fr.ifpen.allotropeconverters.allotrope_models.*;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.CompoundType;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.CompoundPeak;
import fr.ifpen.allotropeconverters.gc.chemstation.domain.IntegrationRow;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ResultXmlReader;
import fr.ifpen.allotropeconverters.gc.chemstation.service.PeakAssociationService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mappe un MeasurementDocument pour un Signal :
 * - type de détection (detector brut),
 * - chromatogramme (cube),
 * - colonne (depuis acq.txt),
 * - pics : liste venant des Integration Results, enrichie si un Compound peak match (SignalDesc + RetTime).
 */
public class MeasurementDocumentMapper {

    private final PeakMapper peakMapper = new PeakMapper();
    private final ColumnInformationMapper columnInformationMapper = new ColumnInformationMapper();
    private final ChromatogramDataCubeMapper dataCubeMapper = new ChromatogramDataCubeMapper();
    private final PeakAssociationService peakAssociationService;

    public MeasurementDocumentMapper(PeakAssociationService peakAssociationService) {
        this.peakAssociationService = peakAssociationService;
    }

    public MeasurementDocument toMeasurementDocumentForSignal(ResultXmlReader.SignalRecord signal,
                                                              ResultXmlReader.ResultData resultData,
                                                              ChFile chFile,
                                                              Map<String, Map<String, CompoundPeak>> compoundIndex,
                                                              String acqTxtFilename,
                                                              Path folderPath) throws IOException {
        MeasurementDocument measurement = new MeasurementDocument();

        measurement.setDetectionType(signal.detectorRaw());

        measurement.setChromatogramDataCube(dataCubeMapper.readChromatogramDataCube(chFile));
        measurement.setMeasurementIdentifier("");

        ChromatographyColumnDocument column = columnInformationMapper.readColumnDocumentFromFile(folderPath, acqTxtFilename);
        measurement.setChromatographyColumnDocument(column);

        List<Peak> peaks = new ArrayList<>();
        String signalDescUpper = signal.signalDescription().trim().toUpperCase();

        for (IntegrationRow row : signal.integrationRows()) {
            var matched = peakAssociationService.findCompoundFor(compoundIndex, signalDescUpper, row.retentionTimeCanonical());
            Peak peak;
            if (matched.isPresent()) {
                peak = peakMapper.mapPeakFromCompound((CompoundType) matched.get().jaxbCompound());
            } else {
                peak = new Peak();

                if (row.retentionTimeMinutes() != null) {
                    PeakRetentionTime peakRetentionTime = new PeakRetentionTime();
                    peakRetentionTime.setUnit(PeakRetentionTime.UnitEnum.S);
                    peakRetentionTime.setValue(row.retentionTimeMinutes() * 60);
                    peak.setRetentionTime(peakRetentionTime);
                }
                if (row.area() != null){
                    PeakPeakArea peakPeakArea = new PeakPeakArea();
                    peakPeakArea.setUnit("pA.S");
                    peakPeakArea.setValue(row.area()*60);
                    peak.setPeakArea(peakPeakArea);
                }
                if (row.height() != null) {
                    PeakPeakHeight peakPeakHeight = new PeakPeakHeight();
                    peakPeakHeight.setUnit("pA");
                    peakPeakHeight.setValue(row.height());
                    peak.setPeakHeight(peakPeakHeight);
                }

                if (row.width() != null){
                    PeakPeakWidthAtHalfHeight peakPeakWidthAtHalfHeight = new PeakPeakWidthAtHalfHeight();
                    peakPeakWidthAtHalfHeight.setUnit(PeakPeakWidthAtHalfHeight.UnitEnum.S);
                    peakPeakWidthAtHalfHeight.setValue(row.width() * 60);
                    peak.setPeakWidthAtHalfHeight(peakPeakWidthAtHalfHeight);
                }

                if (row.peakStart() != null){
                    PeakPeakStart peakPeakStart = new PeakPeakStart();
                    peakPeakStart.setUnit(PeakPeakStart.UnitEnum.S);
                    peakPeakStart.setValue(row.peakStart() * 60);
                    peak.setPeakStart(peakPeakStart);
                }

                if (row.peakEnd() != null){
                    PeakPeakEnd peakPeakEnd = new PeakPeakEnd();
                    peakPeakEnd.setUnit(PeakPeakEnd.UnitEnum.S);
                    peakPeakEnd.setValue(row.peakEnd() * 60);
                    peak.setPeakEnd(peakPeakEnd);
                }

                if (row.relativeHeight() != null){
                    PeakRelativePeakHeight relativePeakHeight = new PeakRelativePeakHeight();
                    relativePeakHeight.setValue(row.relativeHeight());
                    relativePeakHeight.setUnit(PeakRelativePeakHeight.UnitEnum.PERCENT);
                    peak.setRelativePeakHeight(relativePeakHeight);
                }

                if (row.relativeArea() != null){
                    PeakRelativePeakArea relativePeakArea = new PeakRelativePeakArea();
                    relativePeakArea.setValue(row.relativeArea());
                    relativePeakArea.setUnit(PeakRelativePeakArea.UnitEnum.PERCENT);
                    peak.setRelativePeakArea(relativePeakArea);
                }

                if (row.symmetryFactor() != null){
                    PeakAsymmetryFactorMeasuredAt5Height peakAsymmetryFactorMeasuredAt5Height =
                            new PeakAsymmetryFactorMeasuredAt5Height();
                    peakAsymmetryFactorMeasuredAt5Height.setValue(row.symmetryFactor());
                    peakAsymmetryFactorMeasuredAt5Height.setUnit(PeakAsymmetryFactorMeasuredAt5Height.UnitEnum._UNITLESS_);
                    peak.setAsymmetryFactorMeasuredAt5PercentHeight(peakAsymmetryFactorMeasuredAt5Height);

                }
            }
            peaks.add(peak);
        }

        ProcessedDataDocument processed = new ProcessedDataDocument();
        PeakList peakList = new PeakList();
        peakList.setPeak(peaks);
        processed.setPeakList(peakList);

        ProcessedDataAggregateDocument processedAggregate = new ProcessedDataAggregateDocument();
        processedAggregate.setProcessedDataDocument(List.of(processed));
        measurement.setProcessedDataAggregateDocument(processedAggregate);

        return measurement;
    }
}
