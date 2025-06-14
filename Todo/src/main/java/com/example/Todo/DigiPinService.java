package com.example.Todo;

import org.springframework.stereotype.Service;

@Service
public class DigiPinService {
    private static final char[][] DIGIPIN_GRID = {
            { 'F', 'C', '9', '8' },
            { 'J', '3', '2', '7' },
            { 'K', '4', '5', '6' },
            { 'L', 'M', 'P', 'T' }
    };

    private static final double MIN_LAT = 2.5;
    private static final double MAX_LAT = 38.5;
    private static final double MIN_LON = 63.5;
    private static final double MAX_LON = 99.5;

    public String encode(double lat, double lon) {
        if (lat < MIN_LAT || lat > MAX_LAT)
            throw new IllegalArgumentException("Latitude out of range");
        if (lon < MIN_LON || lon > MAX_LON)
            throw new IllegalArgumentException("Longitude out of range");

        double minLat = MIN_LAT, maxLat = MAX_LAT, minLon = MIN_LON, maxLon = MAX_LON;
        StringBuilder digiPin = new StringBuilder();

        for (int level = 1; level <= 10; level++) {
            double latDiv = (maxLat - minLat) / 4;
            double lonDiv = (maxLon - minLon) / 4;

            int row = 3 - (int) ((lat - minLat) / latDiv);
            int col = (int) ((lon - minLon) / lonDiv);

            row = Math.max(0, Math.min(row, 3));
            col = Math.max(0, Math.min(col, 3));

            digiPin.append(DIGIPIN_GRID[row][col]);

            if (level == 3 || level == 6)
                digiPin.append('-');

            maxLat = minLat + latDiv * (4 - row);
            minLat = minLat + latDiv * (3 - row);
            minLon = minLon + lonDiv * col;
            maxLon = minLon + lonDiv;
        }

        return digiPin.toString();
    }

    public double[] decode(String digiPin) {
        String pin = digiPin.replace("-", "");
        if (pin.length() != 10)
            throw new IllegalArgumentException("Invalid DIGIPIN");

        double minLat = MIN_LAT, maxLat = MAX_LAT, minLon = MIN_LON, maxLon = MAX_LON;

        for (int i = 0; i < 10; i++) {
            char ch = pin.charAt(i);
            int ri = -1, ci = -1;
            boolean found = false;

            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    if (DIGIPIN_GRID[r][c] == ch) {
                        ri = r;
                        ci = c;
                        found = true;
                        break;
                    }
                }
                if (found)
                    break;
            }

            if (!found)
                throw new IllegalArgumentException("Invalid character in DIGIPIN");

            double latDiv = (maxLat - minLat) / 4;
            double lonDiv = (maxLon - minLon) / 4;

            double lat1 = maxLat - latDiv * (ri + 1);
            double lat2 = maxLat - latDiv * ri;
            double lon1 = minLon + lonDiv * ci;
            double lon2 = minLon + lonDiv * (ci + 1);

            minLat = lat1;
            maxLat = lat2;
            minLon = lon1;
            maxLon = lon2;
        }

        return new double[] {
                Math.round(((minLat + maxLat) / 2) * 1e6) / 1e6,
                Math.round(((minLon + maxLon) / 2) * 1e6) / 1e6
        };
    }
}
