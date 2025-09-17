package fr.ifpen.allotropeconverters.gc.chemstation.domain;

public enum DetectorKind {
    FLAME_IONIZATION, UNKNOWN;

    public static DetectorKind fromRaw(String detectorRaw) {
        if (detectorRaw == null) return UNKNOWN;
        String d = detectorRaw.toLowerCase();
        if (d.contains("fid")) return FLAME_IONIZATION;
        return UNKNOWN;
    }

    public String toAllotropeName() {
        return switch (this) {
            case FLAME_IONIZATION -> "Flame Ionization";
            default -> "Unknown";
        };
    }
}
