package fr.ifpen.allotropeconverters.gc.chemstation.domain;

public record IntegrationRow (
        String signalDescription,
        String retentionTimeCanonical,
        Double retentionTimeMinutes,
        Double area,
        Double height,
        Double width,
        Double peakStart,
        Double peakEnd,
        Double relativeHeight,
        Double relativeArea,
        Double symmetryFactor) {}
