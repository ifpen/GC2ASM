package fr.ifpen.allotropeconverters.gc.chemstation.domain;

import java.util.Objects;

public final class ChannelKey {
    private final String value; // e.g. "FID1A"

    private ChannelKey(String value) { this.value = Objects.requireNonNull(value); }

    public static ChannelKey of(String detector, String signalId) {
        String d = detector == null ? "" : detector.trim().toUpperCase();
        String s = signalId == null ? "" : signalId.trim().toUpperCase();
        return new ChannelKey(d + s);
    }

    public String value() { return value; }

    @Override public String toString() { return value; }
    @Override public boolean equals(Object o){ return o instanceof ChannelKey ck && ck.value.equals(value); }
    @Override public int hashCode(){ return value.hashCode(); }
}
