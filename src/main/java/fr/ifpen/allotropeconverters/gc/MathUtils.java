package fr.ifpen.allotropeconverters.gc;

public class MathUtils {

    private MathUtils() {
        throw new IllegalStateException("Utility class");
    }


    /***
     * Interpolating method
     * @param start start of the interval
     * @param end end of the interval
     * @param count count of output interpolated numbers
     * @return array of interpolated number with specified count
     */
    public static Double[] interpolate(double start, double end, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("interpolate: illegal count!");
        }
        Double[] array = new Double[count];
        for (int i = 0; i < count; ++i) {
            array[i] = start + i * (end - start) / (count-1);
        }
        return array;
    }
}
