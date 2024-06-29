package org.sola.cs.services.ejbs.claim.map;

import org.geotools.tile.TileFactory;
import org.geotools.tile.TileIdentifier;
import org.geotools.tile.impl.WebMercatorTileService;
import org.geotools.tile.impl.ZoomLevel;

public class GoogleService extends WebMercatorTileService {
    private static final TileFactory tileFactory = new GoogleTileFactory();

    private static double[] scaleList = {
        Double.NaN,
        Double.NaN,
        295829355.45,
        147914677.73,
        73957338.86,
        36978669.43,
        18489334.72,
        9244667.36,
        4622333.68,
        2311166.84,
        1155583.42,
        577791.71,
        288895.85,
        144447.93,
        72223.96,
        36111.98,
        18055.99,
        9028.0,
        4514.0,
        2257.0,
        1128.50,
        564.25,
        282.12,
        141.06,
        70.53
    };

    public GoogleService (String name, String baseUrl) {
        super(name, baseUrl);
    }

    @Override
    public double[] getScaleList() {
        return scaleList;
    }

    @Override
    public TileFactory getTileFactory() {
        return tileFactory;
    }

    @Override
    public TileIdentifier identifyTileAtCoordinate(double lon, double lat, ZoomLevel zoomLevel) {
        lat = TileFactory.normalizeDegreeValue(lat, 90);
        lon = TileFactory.normalizeDegreeValue(lon, 180);

        lat = GoogleTileFactory.moveInRange(
                        lat,
                        WebMercatorTileService.MIN_LATITUDE,
                        WebMercatorTileService.MAX_LATITUDE);

        int zoomPower = 1 << zoomLevel.getZoomLevel();
        int xTile = (int) Math.floor((lon + 180) / 360 * zoomPower);
        double latr = lat * Math.PI / 180;
        double yd = (1 - Math.log(Math.tan(latr) + 1 / Math.cos(latr)) / Math.PI) / 2 * zoomPower;
        int yTile = (int) Math.floor(yd);
        if (yTile < 0) yTile = 0;
        return new GoogleTileIdentifier(xTile, yTile, zoomLevel, getName());
    }
}
