package org.sola.cs.services.ejbs.claim.map;

import java.net.URL;
import org.geotools.tile.Tile;
import org.geotools.tile.TileIdentifier;
import org.geotools.tile.TileService;
import org.geotools.tile.impl.WebMercatorTileFactory;
import org.geotools.tile.impl.ZoomLevel;

public class GoogleTile extends Tile {
    public static final int DEFAULT_TILE_SIZE = 256;

    public GoogleTile(int x, int y, ZoomLevel zoomLevel, TileService service) {
        this(new GoogleTileIdentifier(x, y, zoomLevel, service.getName()), service);
    }

    public GoogleTile(TileIdentifier tileName, TileService service) {
        super(
                tileName,
                WebMercatorTileFactory.getExtentFromTileName(tileName),
                DEFAULT_TILE_SIZE,
                service);
    }

    @Override
    public URL getUrl() {
        String url = this.service.getBaseUrl() + getTileIdentifier().getCode();
        try {
            return new URL(url);
        } catch (Exception e) {
            final String mesg = "Cannot create URL from " + url;
            throw new RuntimeException(mesg, e);
        }
    }
}
