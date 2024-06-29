package org.sola.cs.services.ejbs.claim.map;

import org.geotools.tile.Tile;
import org.geotools.tile.TileIdentifier;
import org.geotools.tile.TileService;
import org.geotools.tile.impl.WebMercatorTileFactory;
import org.geotools.tile.impl.ZoomLevel;

public class GoogleTileFactory extends WebMercatorTileFactory {

    @Override
    public Tile create(TileIdentifier identifier, TileService service) {
        return new GoogleTile(identifier, service);
    }

    @Override
    public Tile findTileAtCoordinate(double lon, double lat, ZoomLevel zoomLevel, TileService service) {
        return create(service.identifyTileAtCoordinate(lon, lat, zoomLevel), service);
    }

    /**
     * This method ensures that value is between min and max. If value < min, min is returned. If
     * value > max, max is returned. Otherwise value.
     */
    public static double moveInRange(double value, double min, double max) {
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        return value;
    }

    @Override
    public Tile findRightNeighbour(Tile tile, TileService service) {
        return create(tile.getTileIdentifier().getRightNeighbour(), service);
    }

    @Override
    public Tile findLowerNeighbour(Tile tile, TileService service) {
        return create(tile.getTileIdentifier().getLowerNeighbour(), service);
    }
}
