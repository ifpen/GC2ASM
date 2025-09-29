package fr.ifpen.allotropeconverters.gc.chemstation.service;

import fr.ifpen.allotropeconverters.gc.chemstation.domain.*;
import java.util.*;

public class PeakAssociationService {


    public Map<String, Map<Double, CompoundPeak>> indexCompoundsBySignalAndRt(List<CompoundPeak> compounds) {
        Map<String, Map<Double, CompoundPeak>> index = new HashMap<>();
        for (CompoundPeak cp : compounds) {
            index.computeIfAbsent(cp.signalDescriptionUpper(), k -> new HashMap<>())
                    .put(cp.rtMinutes(), cp);
        }
        return index;
    }


    public Optional<CompoundPeak> findCompoundFor(Map<String, Map<Double, CompoundPeak>> index,
                                                  String signalDescriptionUpper,
                                                  Double rtMinutes) {
        Map<Double, CompoundPeak> byRt = index.get(signalDescriptionUpper);
        return Optional.ofNullable(byRt).map(mapByRt -> mapByRt.get(rtMinutes));
    }
}