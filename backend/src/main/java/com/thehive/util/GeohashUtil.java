package com.thehive.util;

public class GeohashUtil {

    public static double calculateDistance(String geohash1, String geohash2) {
        if (geohash1 == null || geohash2 == null) {
            return Double.MAX_VALUE;
        }
        
        // find common prefix length
        int commonPrefixLength = 0;
        int minLength = Math.min(geohash1.length(), geohash2.length());
        
        for (int i = 0; i < minLength; i++) {
            if (geohash1.charAt(i) == geohash2.charAt(i)) {
                commonPrefixLength++;
            } else {
                break;
            }
        }
        
        // Rough distance estimation based on prefix length
        // Each character of precision roughly halves the area
        // geohash precision to approximate distance:
        // 1 char: ±2500 km
        // 2 chars: ±630 km
        // 3 chars: ±78 km
        // 4 chars: ±20 km
        // 5 chars: ±2.4 km
        // 6 chars: ±0.61 km
        // 7 chars: ±0.076 km
        
        switch (commonPrefixLength) {
            case 0: return 5000.0;
            case 1: return 1250.0;
            case 2: return 300.0;
            case 3: return 40.0;
            case 4: return 10.0;
            case 5: return 1.2;
            case 6: return 0.3;
            default: return 0.05;
        }
    }
    
    public static boolean isWithinDistance(String geohash1, String geohash2, double maxDistanceKm) {
        return calculateDistance(geohash1, geohash2) <= maxDistanceKm;
    }
    
    public static String getDistanceLabel(double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%.0f m", distanceKm * 1000);
        } else if (distanceKm < 10) {
            return String.format("%.1f km", distanceKm);
        } else {
            return String.format("%.0f km", distanceKm);
        }
    }
}

