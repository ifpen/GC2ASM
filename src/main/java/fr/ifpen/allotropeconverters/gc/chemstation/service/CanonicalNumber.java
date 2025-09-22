package fr.ifpen.allotropeconverters.gc.chemstation.service;

import java.math.BigDecimal;

public final class CanonicalNumber {
    private CanonicalNumber() {}

    public static String from(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            var bd = new BigDecimal(raw.trim().replace('\u00A0',' ').replace(",", "."));
            return bd.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String from(Double value) {
        if (value == null) return null;
        var bd = BigDecimal.valueOf(value);
        return bd.stripTrailingZeros().toPlainString();
    }
}
