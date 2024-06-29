package org.sola.cs.services.ejbs.claim.map;

import org.geotools.tile.TileIdentifier;
import org.geotools.tile.impl.ZoomLevel;

public class GoogleTileIdentifier extends TileIdentifier {
    public GoogleTileIdentifier(int x, int y, ZoomLevel zoomLevel, String serviceName) {
        super(x, y, zoomLevel, serviceName);
    }

    @Override
    public String getId() {
        final String separator = "_";
        StringBuilder sb = createGenericCodeBuilder(separator);
        sb.insert(0, separator).insert(0, getServiceName());
        return sb.toString();
    }

    @Override
    public String getCode() {
        return createGenericCodeBuilder("&").toString();
    }

    private StringBuilder createGenericCodeBuilder(String separator) {
        StringBuilder sb = new StringBuilder(150);
        sb.append(separator).append("x=").append(getX()).append(separator).append("y=").append(getY()).append(separator).append("z=").append(getZ());
        return sb;
    }

    @Override
    public TileIdentifier getRightNeighbour() {
        return new GoogleTileIdentifier(
                TileIdentifier.arithmeticMod((getX() + 1), getZoomLevel().getMaxTilePerRowNumber()),
                getY(),
                getZoomLevel(),
                getServiceName());
    }

    @Override
    public TileIdentifier getLowerNeighbour() {
        return new GoogleTileIdentifier(
                getX(),
                TileIdentifier.arithmeticMod((getY() + 1), getZoomLevel().getMaxTilePerRowNumber()),
                getZoomLevel(),
                getServiceName());
    }
}
