package fr.ifpen.allotropeconverters.gc.chemstation.domain;

public record Volume(double microliters) {
    public static Volume fromText(String raw) {
        if (raw == null || raw.isBlank()) return new Volume(Double.NaN);
        String s = raw.trim().replace('\u00A0',' ').replace(",", ".").toLowerCase();
        var m = java.util.regex.Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)").matcher(s);
        if (!m.find()) return new Volume(Double.NaN);
        double v = Double.parseDouble(m.group(1));
        if (s.contains("ml")) return new Volume(v * 1000.0);
        if (s.contains("nl")) return new Volume(v / 1000.0);
        return new Volume(v); // µL par défaut
    }
}
