package fr.ifpen.allotropeconverters.gc.chemstation.domain;

/** Quantité numérique + unité (optionnelle). Immuable. */
public record Quantity(Double value, String unit) {
    public boolean hasValue() { return value != null; }
    public static Quantity of(Double value, String unit) { return new Quantity(value, unit); }
}
