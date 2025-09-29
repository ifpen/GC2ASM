package fr.ifpen.allotropeconverters.gc.chemstation.domain;

public record CompoundPeak(String signalDescriptionUpper, Double rtMinutes,
                           Object jaxbCompound) { }
