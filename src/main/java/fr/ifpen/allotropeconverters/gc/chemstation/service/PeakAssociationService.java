package fr.ifpen.allotropeconverters.gc.chemstation.service;

import fr.ifpen.allotropeconverters.gc.chemstation.domain.*;
import java.util.*;

public class PeakAssociationService {


    public Map<String, Map<String, CompoundPeak>> indexCompoundsBySignalAndRt(List<CompoundPeak> compounds) {
        Map<String, Map<String, CompoundPeak>> index = new HashMap<>();
        for (CompoundPeak cp : compounds) {
            index.computeIfAbsent(cp.signalDescriptionUpper(), k -> new HashMap<>())
                    .put(cp.rtCanonical(), cp);
        }
        return index;
    }


    public Optional<CompoundPeak> findCompoundFor(Map<String, Map<String, CompoundPeak>> index,
                                                  String signalDescriptionUpper,
                                                  String rtCanonical) {
        Map<String, CompoundPeak> byRt = index.get(signalDescriptionUpper);
        if (byRt == null) return Optional.empty();
        return Optional.ofNullable(byRt.get(rtCanonical));
    }
}
