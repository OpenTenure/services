package org.sola.cs.services.ejbs.claim.businesslogic;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.Response;
import org.sola.common.ConfigConstants;
import org.sola.common.SOLAException;
import org.sola.cs.common.messaging.ServiceMessage;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejbs.claim.entities.ClaimSpatial;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.logging.LogUtility;
import org.sola.services.common.repository.CommonSqlProvider;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.measure.Unit;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.tile.TileService;
import org.geotools.tile.util.TileLayer;
import org.geotools.xml.styling.SLDParser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.TransformException;
import org.sola.common.StringUtility;
import org.sola.cs.services.ejbs.claim.entities.BoundingBox;
import org.sola.cs.services.ejbs.claim.entities.MapImageParams;
import org.sola.cs.services.ejbs.claim.entities.MapImageResponse;
import org.sola.cs.services.ejbs.claim.map.GoogleService;

/**
 * Implements methods to generate parcel map image
 */
@Stateless
@EJB(name = "java:app/MapImageEJBLocal", beanInterface = MapImageEJBLocal.class)
public class MapImageEJB extends AbstractEJB implements MapImageEJBLocal {

    @EJB
    SystemCSEJBLocal systemEjb;

    private static final int DPI = 85;
    private static final String RESOURCES_PATH = "/styles/";
    private static final int roundNumber = 5;
    private int initialMapMargin;
    private int coordWidth;
    private int scaleLabelHeight;
    private static final int pdfCoof = 4;

    /**
     * Returns spatial claims by claim id
     *
     * @param claimId Claim ID
     * @param srid SRID
     * @return
     */
    private List<ClaimSpatial> getSpatialClaimsByClaim(String claimId, String projectId, int srid) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, ClaimSpatial.QUERY_CLAIM_WITH_NEIGHBOUR);
        params.put(ClaimSpatial.PARAM_CLAIM_ID, claimId);
        params.put(ClaimSpatial.PARAM_PROJECT_ID, projectId);
        params.put(ClaimSpatial.PARAM_CUSTOM_SRID, srid);
        return getRepository().getEntityList(ClaimSpatial.class, params);
    }

    private MapContent getUtmMap(MapContent wgs84map, String projectId) throws FactoryException {
        MapContent map = new MapContent();

        try {
            String crsWkt = "GEOGCS[\"WGS 84\", \n"
                    + "  DATUM[\"World Geodetic System 1984\", \n"
                    + "    SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \n"
                    + "    AUTHORITY[\"EPSG\",\"6326\"]], \n"
                    + "  PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n"
                    + "  UNIT[\"degree\", 0.017453292519943295], \n"
                    + "  AXIS[\"Geodetic latitude\", NORTH], \n"
                    + "  AXIS[\"Geodetic longitude\", EAST], \n"
                    + "  AUTHORITY[\"EPSG\",\"4326\"]]";

            String customCrsWkt = systemEjb.getSetting(ConfigConstants.OT_TITLE_PLAN_CRS_WKT, projectId, "");
            int customCrsInt = 0;
            CoordinateReferenceSystem crs;

            if (customCrsWkt != null && customCrsWkt.length() > 0) {
                crsWkt = customCrsWkt;
                crs = CRS.parseWKT(crsWkt);
                customCrsInt = Integer.parseInt(crs.getIdentifiers().iterator().next().getCode());
            } else {
                crs = CRS.parseWKT(crsWkt);
            }

            int srid = Integer.parseInt(crs.getIdentifiers().iterator().next().getCode());
            if (srid == 4326) {
                return wgs84map;
            }

            map.getViewport().setCoordinateReferenceSystem(crs);
            map.getViewport().setScreenArea(wgs84map.getViewport().getScreenArea());

            // Get utm extent
            ReferencedEnvelope extent = wgs84map.getViewport().getBounds();

            String wgs84Box = String.format("MULTIPOINT(%s %s, %s %s)", extent.getMinX(), extent.getMinY(), extent.getMaxX(), extent.getMaxY());

            HashMap params = new HashMap();
            params.put(CommonSqlProvider.PARAM_QUERY, BoundingBox.QUERY);
            params.put(BoundingBox.PARAM_POINTS, wgs84Box);
            params.put(BoundingBox.PARAM_SRID, srid);

            BoundingBox bBox = (BoundingBox) getRepository().getEntity(BoundingBox.class, params);

            ReferencedEnvelope utmExtent = new ReferencedEnvelope(bBox.getMinx(), bBox.getMaxx(), bBox.getMiny(), bBox.getMaxy(), crs);

            map.getViewport().getBounds().expandToInclude(utmExtent);
            map.getViewport().setBounds(utmExtent);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return map;
    }

    // Returns map filled with parcels
    private MapContent getParcelMap(String claimId, String projectId, MapImageParams params) {
        int mapMargin = initialMapMargin;
        if (!params.isShowGrid()) {
            mapMargin = 0;
        }

        int width = params.getWidth() - mapMargin;
        int height = params.getHeight() - mapMargin;

        try {
            CoordinateReferenceSystem wgs84crs = DefaultGeographicCRS.WGS84;

            SimpleFeatureTypeBuilder builderParcel = new SimpleFeatureTypeBuilder();
            SimpleFeatureTypeBuilder builderDimension = new SimpleFeatureTypeBuilder();
            SimpleFeatureTypeBuilder builderCoords = new SimpleFeatureTypeBuilder();

            builderParcel.setName("parcel");
            builderParcel.setCRS(wgs84crs);

            builderDimension.setName("dimension");
            builderDimension.setCRS(wgs84crs);

            builderCoords.setName("coords");
            builderCoords.setCRS(wgs84crs);

            // add attributes in order
            builderParcel.add("geom", Polygon.class);
            builderParcel.length(25).add("label", String.class);
            builderParcel.add("target", Boolean.class);

            builderCoords.add("geom", LineString.class);
            builderCoords.length(25).add("label", String.class);

            builderDimension.add("geom", LineString.class);
            builderDimension.length(25).add("label", String.class);

            // build the type
            final SimpleFeatureType POLY_TYPE = builderParcel.buildFeatureType();
            final SimpleFeatureType TYPE_DIMENTION = builderDimension.buildFeatureType();
            final SimpleFeatureType TYPE_COORDS = builderCoords.buildFeatureType();

            DefaultFeatureCollection parcelFeatures = new DefaultFeatureCollection("parcels", POLY_TYPE);
            DefaultFeatureCollection claimDimentions = new DefaultFeatureCollection("dimensions", TYPE_DIMENTION);
            DefaultFeatureCollection claimCoords = new DefaultFeatureCollection("coords", TYPE_COORDS);

            WKTReader2 wkt = new WKTReader2();

            // Get target parcel
            List<ClaimSpatial> claims = getSpatialClaimsByClaim(claimId, projectId, 4326);

            if (claims == null || claims.size() < 1) {
                return null;
            }

            ClaimSpatial targetClaim = null;
            List<ClaimSpatial> neighborClaims = new ArrayList<ClaimSpatial>();

            for (ClaimSpatial claim : claims) {
                if (claim.isTarget()) {
                    targetClaim = claim;
                } else {
                    neighborClaims.add(claim);
                }
            }

            if (targetClaim == null) {
                return null;
            }

            String parcelLabel = "";
            if (params.isShowLabels()) {
                parcelLabel = targetClaim.getNr();
            }

            SimpleFeature parcelFeature = SimpleFeatureBuilder.build(POLY_TYPE, new Object[]{
                wkt.read(targetClaim.getGeom()), parcelLabel, targetClaim.isTarget()}, targetClaim.getId());
            parcelFeatures.add(parcelFeature);

            Coordinate[] points = ((Polygon) parcelFeature.getDefaultGeometry()).getCoordinates();

            for (int i = 0; i < points.length - 1; i++) {
                if (params.isShowPoints()) {
                    claimCoords.add(SimpleFeatureBuilder.build(
                            TYPE_COORDS,
                            new Object[]{
                                wkt.read(String.format("POINT(%s %s)", points[i].x, points[i].y)),
                                i + 1
                            },
                            Integer.toString(i)
                    ));
                }
            }

            // Create a map content and add our shapefile to it
            MapContent map = new MapContent();
            map.setTitle("Parcel plan");
            map.getViewport().setCoordinateReferenceSystem(wgs84crs);

            String pdfStyle = params.isForPdf() ? "_pdf" : "";
            StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
            SLDParser stylereader;
            URL sldURL;

            // Add parcels layer
            sldURL = MapImageEJB.class.getResource(RESOURCES_PATH + "cert_parcel" + pdfStyle + ".xml");
            stylereader = new SLDParser(styleFactory, sldURL);
            Style parcelStyle = stylereader.readXML()[0];

            Layer layer = new FeatureLayer(DataUtilities.source(parcelFeatures), parcelStyle);
            map.addLayer(layer);

            if (params.isShowPoints()) {
                // Add coordinates layer
                sldURL = MapImageEJB.class.getResource(RESOURCES_PATH + "target_parcel_node" + pdfStyle + ".xml");
                stylereader = new SLDParser(styleFactory, sldURL);
                Style sldCoordsStyle = stylereader.readXML()[0];

                Layer coordsLayer = new FeatureLayer(DataUtilities.source(claimCoords), sldCoordsStyle);
                map.addLayer(coordsLayer);
            }
            
            double percent = 0.1;

            if (params.isShowSmaller()) {
                percent = 2;
            }

            // Make map extent with the same ratio as requested image 
            double imageRatio = (double) height / (double) width;
            double mapRatio = map.getMaxBounds().getHeight() / map.getMaxBounds().getWidth();
            double minX = map.getMaxBounds().getMinX();
            double maxX = map.getMaxBounds().getMaxX();
            double minY = map.getMaxBounds().getMinY();
            double maxY = map.getMaxBounds().getMaxY();

            if (imageRatio != mapRatio) {
                if (imageRatio < mapRatio) {
                    double newMapWidth = map.getMaxBounds().getHeight() / imageRatio;
                    minX = map.getMaxBounds().getMedian(0) - newMapWidth / 2;
                    maxX = map.getMaxBounds().getMedian(0) + newMapWidth / 2;
                } else {
                    double newMapHeight = map.getMaxBounds().getWidth() * imageRatio;
                    minY = map.getMaxBounds().getMedian(1) - newMapHeight / 2;
                    maxY = map.getMaxBounds().getMedian(1) + newMapHeight / 2;
                }
            }

            double deltaX = (maxX - minX) * percent;
            double deltaY = (maxY - minY) * percent;

            minX = minX - deltaX;
            maxX = maxX + deltaX;
            minY = minY - deltaY;
            maxY = maxY + deltaY;

            ReferencedEnvelope extent = new ReferencedEnvelope(minX, maxX, minY, maxY,
                    map.getViewport().getCoordinateReferenceSystem());

            map.getViewport().setScreenArea(new Rectangle(width, height));
            map.getViewport().getBounds().expandToInclude(extent);
            map.getViewport().setBounds(extent);

            // Add neighboring parcels
            DefaultFeatureCollection neighborParcelFeatures = new DefaultFeatureCollection("neighbors", POLY_TYPE);
            for (ClaimSpatial neighborParcel : neighborClaims) {
                String upi = StringUtility.empty(neighborParcel.getNr());
                neighborParcelFeatures.add(SimpleFeatureBuilder.build(POLY_TYPE,
                        new Object[]{wkt.read(neighborParcel.getGeom()), upi, neighborParcel.isTarget()}, neighborParcel.getId()));
            }

            if (!neighborParcelFeatures.isEmpty()) {
                SimpleFeatureSource neighborParcelSource = DataUtilities.source(neighborParcelFeatures);
                Layer neighborClaimsLayer = new FeatureLayer(neighborParcelSource, parcelStyle);
                map.addLayer(neighborClaimsLayer);
                map.moveLayer(map.layers().size() - 1, 0);
            }

            // Add google layer
            if (params.isUseGoogleBackground()) {
                String baseURL = "https://mt1.google.com/vt/lyrs=s";
                TileService service = new GoogleService("Google", baseURL);
                map.layers().add(0, new TileLayer(service));
            }

            return map;
        } catch (Exception e) {
            LogUtility.log("Failed to create map", e);
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_FAILED_TO_CREATE_MAP);
        }
    }

    /**
     * Returns map image for provided claim id
     *
     * @param claimId Claim id
     * @param projectId Project id
     * @param params Map generation parameters
     * @return
     */
    @Override
    public MapImageResponse getMapImage(String claimId, String projectId, MapImageParams params) {
        try {
            initialMapMargin = 30;
            coordWidth = 60;
            scaleLabelHeight = 20;

            if (params.isForPdf()) {
                initialMapMargin = initialMapMargin * pdfCoof;
                coordWidth = coordWidth * pdfCoof;
                scaleLabelHeight = scaleLabelHeight * pdfCoof;
                params.setWidth(params.getWidth() * pdfCoof);
                params.setHeight(params.getHeight() * pdfCoof);
            }

            int mapMargin = initialMapMargin;
            if (!params.isShowGrid()) {
                mapMargin = 0;
            }

            if (params.isShowScale()) {
                params.setHeight(params.getHeight() - scaleLabelHeight);
            }

            MapContent map;

            
            map = getParcelMap(claimId, projectId, params);

            int width = params.getWidth() - mapMargin;

            if (map == null) {
                return null;
            }

            ReferencedEnvelope extent = map.getViewport().getBounds();

            // Set map ratio
            double mapRatio = extent.getHeight() / extent.getWidth();
            double distance = calcDistance(extent, true);

            // Meters per pixel
            double mpp = distance / width;
            // Degrees per pixel
            double degPp = map.getViewport().getBounds().getWidth() / width;
            double realScale = mpp * (DPI / 2.54) * 100;
            double scale = realScale;

            if (params.isShowScale()) {
                scale = getBestScaleForMapImage(map, width, params.isForPdf(), params.isUseGoogleBackground());
            }

            if (scale != realScale) {
                double newMpp = scale / 100 / (DPI / 2.54);
                double newDistance = newMpp * width;

                //double mDiff = Math.abs(newDistance - distance);
                double mDiff = newDistance - distance;
                mDiff = mDiff / 2; // 50% required to extend each side

                // Differenr in pixels
                double pDiff = mDiff / mpp;

                // Difference in degrees
                double degDiff = degPp * pDiff;

                extent = new ReferencedEnvelope(
                        map.getViewport().getBounds().getMinX() - degDiff,
                        map.getViewport().getBounds().getMaxX() + degDiff,
                        map.getViewport().getBounds().getMinY() - (degDiff * mapRatio),
                        map.getViewport().getBounds().getMaxY() + (degDiff * mapRatio),
                        map.getViewport().getCoordinateReferenceSystem());

                map.getViewport().getBounds().expandToInclude(extent);
                map.getViewport().setBounds(extent);
            }

            MapImageResponse response = getMapImage(map, projectId, scale, params);
            map.dispose();
            return response;

        } catch (Exception e) {
            LogUtility.log("Failed to create map", e);
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_FAILED_TO_CREATE_MAP);
        }
    }

    private MapImageResponse getMapImage(MapContent map, String projectId, double scale, MapImageParams params)
            throws SchemaException, FactoryException, IOException,
            TransformException, NoninvertibleTransformException {

        MapImageResponse mapResponse = new MapImageResponse();
        if (params.isForPdf()) {
            scale = scale * pdfCoof;
        }
        mapResponse.setScale((int) scale);

        if (map == null) {
            return mapResponse;
        }

        int mapMargin = initialMapMargin;
        if (!params.isShowGrid()) {
            mapMargin = 0;
        }

        int width = (int) map.getViewport().getScreenArea().getWidth();
        int height = (int) map.getViewport().getScreenArea().getHeight();

        ReferencedEnvelope extent = map.getViewport().getBounds();
        MapContent utmMap = getUtmMap(map, projectId);

        boolean isWgs84 = map == utmMap;
        boolean isDegrees = !isUtmCrs(utmMap.getCoordinateReferenceSystem().getCoordinateSystem());

        mapResponse.setIsDegrees(isDegrees);

        if (isWgs84) {
            mapResponse.setCrsName("WGS84");
            mapResponse.setMinX(map.getViewport().getBounds().getMinX());
            mapResponse.setMaxX(map.getViewport().getBounds().getMaxX());
            mapResponse.setMinY(map.getViewport().getBounds().getMinY());
            mapResponse.setMaxY(map.getViewport().getBounds().getMaxY());
        } else {
            mapResponse.setCrsName(utmMap.getCoordinateReferenceSystem().getName().getCode());
            mapResponse.setMinX(utmMap.getViewport().getBounds().getMinX());
            mapResponse.setMaxX(utmMap.getViewport().getBounds().getMaxX());
            mapResponse.setMinY(utmMap.getViewport().getBounds().getMinY());
            mapResponse.setMaxY(utmMap.getViewport().getBounds().getMaxY());
        }

        double distance = calcDistance(utmMap.getViewport().getBounds(), isDegrees);

        // Meters per pixel
        double mpp = distance / width;

        // Draw image
        org.geotools.renderer.GTRenderer renderer = new org.geotools.renderer.lite.StreamingRenderer();
        renderer.setMapContent(map);

        Rectangle fullBounds = new Rectangle(0, 0, width + mapMargin, height + mapMargin);
        Rectangle mapBounds = new Rectangle(0, 0, width, height);

        BufferedImage mapImage = new BufferedImage(mapBounds.width, mapBounds.height, BufferedImage.TYPE_INT_RGB);
        BufferedImage fullImage = new BufferedImage(fullBounds.width, fullBounds.height, BufferedImage.TYPE_INT_RGB);

        Graphics2D grMapImage = mapImage.createGraphics();
        grMapImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        grMapImage.setPaint(Color.WHITE);
        grMapImage.fill(mapBounds);

        if (params.isUseWmsBackground()) {
            // WMS background
            String wmsServerUrl = systemEjb.getSetting(ConfigConstants.OT_WMS_SERVER_URL, projectId, "");
            String layerName = systemEjb.getSetting(ConfigConstants.OT_WMS_BG_LAYER_NAME, projectId, "");

            if (!StringUtility.isEmpty(wmsServerUrl) && !StringUtility.isEmpty(layerName)) {
                ClientBuilder builder = ClientBuilder.newBuilder();
                builder = builder.connectTimeout(2, TimeUnit.MINUTES);
                builder = builder.readTimeout(2, TimeUnit.MINUTES);
                Client client = builder.build();
                
                WebTarget target = client.target(
                        String.format("%s/wms?LAYERS=%s&TRANSPARENT=false&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&FORMAT=image/jpeg&SRS=EPSG:4326&BBOX=%s,%s,%s,%s&WIDTH=%s&HEIGHT=%s",
                                wmsServerUrl, layerName, extent.getMinX(), extent.getMinY(), extent.getMaxX(), extent.getMaxY(), width, height));
                Response response = target.request("image/jpeg").get();
                InputStream is = null;

                try {
                    is = response.readEntity(InputStream.class);
                    BufferedImage bg = ImageIO.read(is);
                    grMapImage.drawImage(bg, 0, 0, null);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e) {

                        }
                    }
                }
                client.close();
            }
        }

        // Draw map
        renderer.paint(grMapImage, mapBounds, extent, map.getViewport().getWorldToScreen());

        // Draw north arrow
        if (params.isShowNorth()) {
            String pdfNorth = params.isForPdf() ? "_pdf" : "";
            int shiftSize = params.isForPdf() ? 10 * pdfCoof : 10;
            BufferedImage arrow = ImageIO.read(MapImageEJB.class.getResourceAsStream(RESOURCES_PATH + "north_arrow" + pdfNorth + ".png"));
            grMapImage.drawImage(arrow, width - arrow.getWidth() - shiftSize, shiftSize, null);
        }

        // Draw full map
        Graphics2D grFullImage = fullImage.createGraphics();
        grFullImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        grFullImage.setPaint(Color.WHITE);
        grFullImage.fill(fullBounds);

        grFullImage.drawImage(mapImage, mapMargin / 2, mapMargin / 2, null);

        if (params.isShowGrid()) {
            // Draw grid cut
            float basicStrokeSize = params.isForPdf() ? pdfCoof : 1;

            grFullImage.setColor(Color.BLACK);
            grFullImage.setStroke(new BasicStroke(basicStrokeSize));
            grFullImage.drawRect((mapMargin / 2) - 1, (mapMargin / 2) - 1, mapBounds.width + 1, mapBounds.height + 1);

            int stepSizeH = (int) width / Math.round(width / (coordWidth * 2));
            int stepSizeV = (int) height / Math.round(height / (coordWidth * 2));

            int cutLen = params.isForPdf() ? 8 * pdfCoof : 8;

            grFullImage.setColor(new Color(200, 200, 200));
            grFullImage.setStroke(new BasicStroke(basicStrokeSize));
            AffineTransform tr = utmMap.getViewport().getScreenToWorld();
            utmMap.dispose();

            if (stepSizeH > 0 && stepSizeV > 0) {
                // Draw grid
                ArrayList<Integer> xPoints = new ArrayList<Integer>();
                ArrayList<Integer> yPoints = new ArrayList<Integer>();

                int nextX = coordWidth + mapMargin / 2;
                int nextY = height + mapMargin / 2 - coordWidth;

                int fontSize = params.isForPdf() ? 12 * pdfCoof : 12;
                grFullImage.setFont(new Font("SansSerif", Font.PLAIN, fontSize));

                while (true) {
                    // Draw horizontal
                    if ((nextX > coordWidth / 2 + mapMargin / 2) && (width + mapMargin / 2 - coordWidth / 2 >= nextX)) {
                        grFullImage.drawLine(nextX, height + mapMargin / 2, nextX, height + mapMargin / 2 - cutLen);
                        grFullImage.drawLine(nextX, mapMargin / 2, nextX, mapMargin / 2 + cutLen);

                        Point2D pointHrz = tr.transform(new Point2D.Double(nextX - (mapMargin / 2), height + mapMargin / 2), null);
                        String pointLabel;

                        if (isDegrees) {
                            pointLabel = Double.toString(round(pointHrz.getX(), roundNumber));
                        } else {
                            pointLabel = Long.toString(Math.round(pointHrz.getX() / 10) * 10);
                        }

                        drawText(grFullImage, pointLabel, nextX, fullBounds.height - 2, true);
                        drawText(grFullImage, pointLabel, nextX, (mapMargin / 2) - 3, true);

                        xPoints.add(nextX);
                    }

                    // Draw vertical
                    if ((mapMargin / 2 + coordWidth / 2 <= nextY) && (nextY < height + mapMargin / 2 - coordWidth / 2)) {
                        grFullImage.drawLine(mapMargin / 2, nextY, mapMargin / 2 + cutLen, nextY);
                        grFullImage.drawLine(width + (mapMargin / 2), nextY, width + (mapMargin / 2) - cutLen, nextY);

                        AffineTransform originalTransform = grFullImage.getTransform();
                        Point2D pointVrt = tr.transform(new Point2D.Double((mapMargin / 2) - 2, nextY - (mapMargin / 2)), null);
                        String pointLabel;

                        if (isDegrees) {
                            pointLabel = Double.toString(round(pointVrt.getY(), roundNumber));
                        } else {
                            pointLabel = Long.toString(Math.round(pointVrt.getY() / 10) * 10);
                        }

                        grFullImage.rotate(-Math.PI / 2, (mapMargin / 2) - 3, nextY);
                        drawText(grFullImage, pointLabel, (mapMargin / 2) - 3, nextY, true);

                        grFullImage.setTransform(originalTransform);

                        grFullImage.rotate(-Math.PI / 2, fullBounds.width - 3, nextY);
                        drawText(grFullImage, pointLabel, fullBounds.width - 3, nextY, true);

                        grFullImage.setTransform(originalTransform);

                        yPoints.add(nextY);
                    }

                    if ((nextX > width + mapMargin / 2 - coordWidth / 2) && (nextY < mapMargin / 2 + coordWidth / 2)) {
                        break;
                    }

                    nextX = nextX + stepSizeH;
                    nextY = nextY - stepSizeV;
                }

                // Draw intersection of xy
                if (xPoints.size() > 0 && yPoints.size() > 0) {
                    for (int x : xPoints) {
                        for (int y : yPoints) {
                            grFullImage.drawLine(x, y + cutLen / 2, x, y - cutLen / 2);
                            grFullImage.drawLine(x - cutLen / 2, y, x + cutLen / 2, y);
                        }
                    }
                }

            } else {
                // Draw coordinates in the middle only
                grFullImage.drawLine(fullBounds.width / 2, height + mapMargin / 2, fullBounds.width / 2, height + mapMargin / 2 - cutLen);
                grFullImage.drawLine(fullBounds.width / 2, mapMargin / 2, fullBounds.width / 2, mapMargin / 2 + cutLen);

                Point2D pointHrz = tr.transform(new Point2D.Double(fullBounds.width / 2 - (mapMargin / 2), height + mapMargin / 2), null);
                String pointLabel;

                if (isDegrees) {
                    pointLabel = Double.toString(round(pointHrz.getX(), roundNumber));
                } else {
                    pointLabel = Long.toString(Math.round(pointHrz.getX()));
                }

                drawText(grFullImage, pointLabel, fullBounds.width / 2, fullBounds.height - 2, true);
                drawText(grFullImage, pointLabel, fullBounds.width / 2, (mapMargin / 2) - 3, true);

                // Vertical
                grFullImage.drawLine(mapMargin / 2, fullBounds.height / 2, mapMargin / 2 + cutLen, fullBounds.height / 2);
                grFullImage.drawLine(width + (mapMargin / 2), fullBounds.height / 2, width + (mapMargin / 2) - cutLen, fullBounds.height / 2);

                AffineTransform originalTransform = grFullImage.getTransform();
                Point2D pointVrt = tr.transform(new Point2D.Double((mapMargin / 2) - 2, fullBounds.height / 2 - (mapMargin / 2)), null);

                if (isDegrees) {
                    pointLabel = Double.toString(round(pointVrt.getY(), roundNumber));
                } else {
                    pointLabel = Long.toString(Math.round(pointVrt.getY()));
                }

                grFullImage.rotate(-Math.PI / 2, (mapMargin / 2) - 3, fullBounds.height / 2);
                drawText(grFullImage, pointLabel, (mapMargin / 2) - 3, fullBounds.height / 2, true);

                grFullImage.setTransform(originalTransform);

                grFullImage.rotate(-Math.PI / 2, fullBounds.width - 3, fullBounds.height / 2);
                drawText(grFullImage, pointLabel, fullBounds.width - 3, fullBounds.height / 2, true);

                grFullImage.setTransform(originalTransform);

                // Cross 
                grFullImage.drawLine(fullBounds.width / 2, fullBounds.height / 2 + cutLen / 2,
                        fullBounds.width / 2, fullBounds.height / 2 - cutLen / 2);
                grFullImage.drawLine(fullBounds.width / 2 - cutLen / 2, fullBounds.height / 2,
                        fullBounds.width / 2 + cutLen / 2, fullBounds.height / 2);
            }
        }

        // Id drawing scale is requested
        if (params.isShowScale()) {
            BufferedImage fullImageWithScale = new BufferedImage(fullImage.getWidth(), fullImage.getHeight() + scaleLabelHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D grFullImageWithScale = fullImageWithScale.createGraphics();
            grFullImageWithScale.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            grFullImageWithScale.setPaint(Color.WHITE);
            grFullImageWithScale.fill(new Rectangle(fullImageWithScale.getWidth(), fullImageWithScale.getHeight()));
            grFullImageWithScale.drawImage(fullImage, 0, 0, null);

            grFullImageWithScale.setPaint(Color.BLACK);
            int fontSize = params.isForPdf() ? 12 * pdfCoof : 11;

            grFullImageWithScale.setFont(new Font("SansSerif", Font.BOLD, fontSize));

            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
            DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            formatter.setDecimalFormatSymbols(symbols);
            drawText(grFullImageWithScale,
                    String.format("%s 1 : %s", params.getScaleLabel(), formatter.format(scale)),
                    fullImageWithScale.getWidth() / 2,
                    fullImageWithScale.getHeight() - 1,
                    true);

            mapResponse.setMap(fullImageWithScale);
            return mapResponse;
        }

        mapResponse.setMap(fullImage);
        return mapResponse;
    }

    private int getBestScaleForMapImage(MapContent map, int width, boolean isForPdf, boolean useGoogleLayer) {
        try {
            ReferencedEnvelope extent = map.getViewport().getBounds();

            double distance = calcDistance(extent, true);

            // Meters per pixel
            int factor = isForPdf ? pdfCoof : 1;
            double mpp = distance / width;
            double realScale = mpp * (DPI / 2.54) * 100;

            int[] scales = {100, 500, 1000, 1500, 2000, 2500, 3000, 5000, 6000, 7000, 8000, 9000, 10000, 12000, 15000, 20000, 25000,
                50000, 75000, 100000, 150000, 200000, 250000, 500000, 750000, 1000000};
            for (int scale : scales) {
                if (scale < 1000 && useGoogleLayer) {
                    continue;
                }
                if (realScale < scale / factor) {
                    return scale / factor;
                }
            }
            return 1000000;
        } catch (Exception e) {
            LogUtility.log("Failed to generate map image", e);
            return 1000;
        }
    }

    /**
     * Calculates best scale for the map image
     *
     * @param claimId = Claim ID
     * @param projectId Project ID
     * @param params Map image parameters
     * @return
     */
    @Override
    public int getBestScaleForMapImage(String claimId, String projectId, MapImageParams params) {
        return getBestScaleForMapImage(getParcelMap(claimId, projectId, params), params.getWidth(), true, params.isUseGoogleBackground());
    }

    private double round(double number, int precision) {
        return Math.round(number * Math.pow(10, precision)) / Math.pow(10, precision);
    }

    private double calcDistance(ReferencedEnvelope extent, boolean isWgs84) {
// double distance = JTS.orthodromicDistance(
//                new Coordinate(extent.getMaxY(),extent.getMinX()),
//                new Coordinate(extent.getMaxY(), extent.getMaxX()),
//                map.getViewport().getCoordinateReferenceSystem());
        if (isWgs84) {
            double earthRadius = 6371000; //meters
            double dLat = Math.toRadians(extent.getMaxX() - extent.getMinX());
            double dLng = Math.toRadians(extent.getMaxY() - extent.getMaxY());
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(extent.getMinX())) * Math.cos(Math.toRadians(extent.getMaxX()))
                    * Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            float dist = (float) (earthRadius * c);
            return dist;
        } else {
            return extent.getMaxX() - extent.getMinX();
        }
    }

    private boolean isUtmCrs(CoordinateSystem crs) {
        boolean isUtm = true;
        try {
            Unit<?> unit = crs.getAxis(0).getUnit();
            String symbol = unit.getSymbol();
            if (StringUtility.isEmpty(symbol) && unit.getSystemUnit() != null) {
                symbol = unit.getSystemUnit().getSymbol();
            }
            if (!symbol.equalsIgnoreCase("m")) {
                isUtm = false;
            }
        } catch (Exception e) {
        }
        return isUtm;
    }

    private void drawText(Graphics2D graphics, String txt, int x, int y, boolean center) {
        if (center) {
            FontMetrics fontMetrics = graphics.getFontMetrics();
            int txtWidth = fontMetrics.stringWidth(txt);
            x = x - txtWidth / 2;
        }
        Color originalColor = graphics.getColor();
        graphics.setColor(Color.BLACK);
        graphics.drawString(txt, x, y);
        graphics.setColor(originalColor);
    }
}
