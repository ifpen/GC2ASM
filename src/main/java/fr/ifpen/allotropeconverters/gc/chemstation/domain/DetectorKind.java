package fr.ifpen.allotropeconverters.gc.chemstation.domain;

public enum DetectorKind {
    FLAME_IONIZATION, UNKNOWN;

    public static DetectorKind fromRaw(String detectorRaw) {
        return (detectorRaw != null && detectorRaw.toLowerCase().contains("fid")) ? FLAME_IONIZATION : UNKNOWN;
    }

    public String toAllotropeName() {
        return switch (this) {
            case FLAME_IONIZATION -> "Flame Ionization";
            default -> "Unknown";
        };
    }
}
