package org.sola.cs.services.ejbs.claim.businesslogic;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.imageio.ImageIO;
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
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.sola.common.ConfigConstants;
import org.sola.common.SOLAException;
import org.sola.cs.common.messaging.ServiceMessage;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;
import org.sola.cs.services.ejbs.claim.entities.ClaimSpatial;
import org.sola.services.common.ejbs.AbstractEJB;
import org.sola.services.common.logging.LogUtility;
import org.sola.services.common.repository.CommonSqlProvider;

/**
 * Implements methods to generate parcel map image
 */
@Stateless
@EJB(name = "java:app/MapImageEJBLocal", beanInterface = MapImageEJBLocal.class)
public class MapImageEJB extends AbstractEJB implements MapImageEJBLocal {

    @EJB
    SystemCSEJBLocal systemEjb;

    @EJB
    AdminCSEJBLocal adminEjb;

    private static final int DPI = 96;
    private static final String resourcesPath = "/styles/";
    private final int mapMargin = 30;
    private final int minGridCuts = 1;
    private final int coordWidth = 67;
    private final int roundNumber = 5;

    /**
     * Returns spatial claims by claim id
     *
     * @param claimId Claim ID
     * @param srid SRID
     * @return
     */
    private List<ClaimSpatial> getSpatialClaimsByClaim(String claimId, int srid) {
        HashMap params = new HashMap();
        params.put(CommonSqlProvider.PARAM_QUERY, ClaimSpatial.QUERY_GET_BY_ID);
        params.put(ClaimSpatial.PARAM_CLAIM_ID, claimId);
        params.put(ClaimSpatial.PARAM_CUSTOM_SRID, srid);
        return getRepository().getEntityList(ClaimSpatial.class, params);
    }

    // Returns map filled with parcels
    private MapContent getMap(String claimId, int width, int height) {
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

            String customCrsWkt = systemEjb.getSetting(ConfigConstants.OT_TITLE_PLAN_CRS_WKT, "");
            int customCrsInt = 0;
            CoordinateReferenceSystem crs;

            if (customCrsWkt != null && customCrsWkt.length() > 0) {
                crsWkt = customCrsWkt;
                crs = CRS.parseWKT(crsWkt);
                customCrsInt = Integer.parseInt(crs.getIdentifiers().iterator().next().getCode());
            } else {
                crs = CRS.parseWKT(crsWkt);
            }

            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("parcel");
            builder.setCRS(crs);
            //builder.setCRS(CRS.decode("EPSG:" + crs));

            // add attributes in order
            builder.add("geom", Polygon.class);
            builder.length(25).add("label", String.class);
            builder.add("target", Boolean.class);

            // build the type
            final SimpleFeatureType TYPE = builder.buildFeatureType();

            DefaultFeatureCollection claimFeatures = new DefaultFeatureCollection("parcels", TYPE);
            WKTReader2 wkt = new WKTReader2();

            // Get parcels
            List<ClaimSpatial> claims = getSpatialClaimsByClaim(claimId, customCrsInt);

            if (claims == null || claims.size() < 1) {
                return null;
            }

            for (ClaimSpatial claim : claims) {
                claimFeatures.add(SimpleFeatureBuilder.build(TYPE, new Object[]{
                    wkt.read(claim.getGeom()), claim.getNr(), claim.isTarget()}, claim.getId()));
            }

            SimpleFeatureSource parcelsSource = DataUtilities.source(claimFeatures);

            // Create a map content and add our shapefile to it
            MapContent map = new MapContent();
            map.setTitle("Parcel plan");
            //map.getViewport().setCoordinateReferenceSystem(CRS.decode("EPSG:" + crs));
            map.getViewport().setCoordinateReferenceSystem(crs);

            StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
            URL sldURL = MapImageEJB.class.getResource(resourcesPath + "cert_parcel.xml");
            SLDParser stylereader;
            stylereader = new SLDParser(styleFactory, sldURL);
            Style sldStyle = stylereader.readXML()[0];

            Layer layer = new FeatureLayer(parcelsSource, sldStyle);
            map.addLayer(layer);

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

            ReferencedEnvelope extent = new ReferencedEnvelope(minX, maxX, minY, maxY,
                    map.getViewport().getCoordinateReferenceSystem());

            map.getViewport().setScreenArea(new Rectangle(width, height));
            map.getViewport().getBounds().expandToInclude(extent);
            map.getViewport().setBounds(extent);

            return map;
        } catch (Exception e) {
            LogUtility.log("Failed to create map", e);
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_FAILED_TO_CREATE_MAP);
        }
    }

    /**
     * Returns map image, containing claim parcel and its surrounding parcels.
     * Map scale is automatically calculated to best fit.
     *
     * @param claimId = Claim ID
     * @param width = Map width
     * @param height = Map height
     * @param drawScale Indicate whether to add scale label on the image
     * @param scaleLabel Text for "Scale" label
     * @return
     */
    @Override
    public BufferedImage getMapImage(String claimId, int width, int height, boolean drawScale, String scaleLabel) {
        MapContent map = getMap(claimId, width - mapMargin, height - mapMargin);

        if (map == null) {
            return null;
        }
        return getMapImage(map, width, getBestScaleForMapImage(map, width), drawScale, scaleLabel);
    }

    /**
     * Returns map image, containing claim parcel and its surrounding parcels
     *
     * @param claimId = Claim ID
     * @param width = Map width
     * @param height = Map height
     * @param scale = Map scale
     * @param drawScale Indicate whether to add scale label on the image
     * @param scaleLabel Text for "Scale" label
     * @return
     */
    @Override
    public BufferedImage getMapImage(String claimId, int width, int height,
            double scale, boolean drawScale, String scaleLabel) {
        try {
            MapContent map = getMap(claimId, width - mapMargin, height - mapMargin);

            if (map == null) {
                return null;
            }
            return getMapImage(map, width, scale, drawScale, scaleLabel);
        } catch (Exception e) {
            LogUtility.log("Failed to generate map image", e);
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_FAILED_TO_GENERATE_MAP);
        }
    }

    private BufferedImage getMapImage(MapContent map, int width, double scale,
            boolean drawScale, String scaleLabel) {
        width = width - mapMargin;
        try {
            if (map == null) {
                return null;
            }

            ReferencedEnvelope extent = map.getViewport().getBounds();

            // Set map ratio
            double mapRatio = extent.getHeight() / extent.getWidth();
            double distance = calcDistance(extent);

            // Meters per pixel
            double mpp = distance / width;
            // Degrees per pixel
            double degPp = map.getViewport().getBounds().getWidth() / width;
            double realScale = mpp * (DPI / 2.54) * 100;

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
            return getMapImage(map, scale, drawScale, scaleLabel);
        } catch (Exception e) {
            LogUtility.log("Failed to generate map image", e);
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_FAILED_TO_GENERATE_MAP);
        }
    }

    private BufferedImage getMapImage(MapContent map, double scale, boolean drawScale, String scaleLabel)
            throws SchemaException, ParseException, FactoryException, IOException,
            TransformException, NoninvertibleTransformException {

        if (map == null) {
            return null;
        }

        int width = (int) map.getViewport().getScreenArea().getWidth();
        int height = (int) map.getViewport().getScreenArea().getHeight();
        boolean isWgs84 = map.getViewport().getCoordinateReferenceSystem()
                .getIdentifiers().iterator().next().getCode().equals("4326");

        ReferencedEnvelope extent = map.getViewport().getBounds();

        double distance = calcDistance(extent);

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

        // Draw map
        renderer.paint(grMapImage, mapBounds, extent, map.getViewport().getWorldToScreen());

        // Draw north arrow
        BufferedImage arrow = ImageIO.read(MapImageEJB.class.getResourceAsStream(resourcesPath + "north_arrow.png"));
        grMapImage.drawImage(arrow, width - arrow.getWidth() - 10, 10, null);

        // Draw grid cut
        Graphics2D grFullImage = fullImage.createGraphics();
        grFullImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        grFullImage.setPaint(Color.WHITE);
        grFullImage.fill(fullBounds);

        grFullImage.drawImage(mapImage, mapMargin / 2, mapMargin / 2, null);

        grFullImage.setColor(Color.BLACK);
        grFullImage.drawRect((mapMargin / 2) - 1, (mapMargin / 2) - 1, mapBounds.width + 1, mapBounds.height + 1);

        //int gridSize = getBestGridSize(gridDistanceX, gridDistanceY, width);
        int gridSize = getBestGridSize(width, height, mpp);
        int stepSize = (int) Math.round(gridSize / mpp);
        int cutLen = 8;
        grFullImage.setColor(Color.RED);
        grFullImage.setStroke(new BasicStroke(1));
        AffineTransform tr = map.getViewport().getScreenToWorld();

        if (gridSize > 0) {
            // Draw grid
            ArrayList<Integer> xPoints = new ArrayList<Integer>();
            ArrayList<Integer> yPoints = new ArrayList<Integer>();

            int nextX = stepSize + mapMargin / 2;
            int nextY = height + mapMargin / 2 - stepSize;

            // If projected CRS, adjust first point to the round number
            if (!isWgs84) {
                Point2D pointHrz = tr.transform(new Point2D.Double(nextX - (mapMargin / 2), height + mapMargin / 2), null);
                Point2D pointVrt = tr.transform(new Point2D.Double((mapMargin / 2) - 2, nextY - (mapMargin / 2)), null);

                if (Math.round(pointHrz.getX()) % gridSize > 0) {
                    pointHrz.setLocation(Math.round(pointHrz.getX()) - (Math.round(pointHrz.getX()) % gridSize), pointHrz.getY());
                    nextX = (int) Math.round(tr.inverseTransform(pointHrz, null).getX()) + (mapMargin / 2);
                }

                if (Math.round(pointVrt.getY()) % gridSize > 0) {
                    pointVrt.setLocation(pointVrt.getX(), Math.round(pointVrt.getY()) - (Math.round(pointVrt.getY()) % gridSize));
                    nextY = (int) Math.round(tr.inverseTransform(pointVrt, null).getY()) + (mapMargin / 2);
                }
            }

            while (true) {
                // Draw horizontal
                if ((nextX > coordWidth / 2 + mapMargin / 2) && (width + mapMargin / 2 - coordWidth / 2 >= nextX)) {
                    grFullImage.drawLine(nextX, height + mapMargin / 2, nextX, height + mapMargin / 2 - cutLen);
                    grFullImage.drawLine(nextX, mapMargin / 2, nextX, mapMargin / 2 + cutLen);

                    Point2D pointHrz = tr.transform(new Point2D.Double(nextX - (mapMargin / 2), height + mapMargin / 2), null);
                    String pointLabel;

                    if (isWgs84) {
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

                    if (isWgs84) {
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

                nextX = nextX + stepSize;
                nextY = nextY - stepSize;
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

            if (isWgs84) {
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

            if (isWgs84) {
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

        // Id drawing scale is requested
        if (drawScale) {
            BufferedImage fullImageWithScale = new BufferedImage(fullImage.getWidth(), fullImage.getHeight() + 30, BufferedImage.TYPE_INT_RGB);
            Graphics2D grFullImageWithScale = fullImageWithScale.createGraphics();
            grFullImageWithScale.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            grFullImageWithScale.setPaint(Color.WHITE);
            grFullImageWithScale.fill(new Rectangle(fullImageWithScale.getWidth(), fullImageWithScale.getHeight()));
            grFullImageWithScale.drawImage(fullImage, 0, 0, null);

            grFullImageWithScale.setPaint(Color.BLACK);
            grFullImageWithScale.setFont(new Font("SansSerif", Font.BOLD, 16));

            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
            DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            formatter.setDecimalFormatSymbols(symbols);
            drawText(grFullImageWithScale,
                    String.format("%s 1:%s", scaleLabel, formatter.format(scale)),
                    fullImageWithScale.getWidth() / 2,
                    fullImageWithScale.getHeight() - 1,
                    true);

            return fullImageWithScale;
        } else {
            return fullImage;
        }
    }

    private int getBestScaleForMapImage(MapContent map, int width) {
        try {
            ReferencedEnvelope extent = map.getViewport().getBounds();

            double distance = calcDistance(extent);

            // Meters per pixel
            double mpp = distance / width;
            double realScale = mpp * (DPI / 2.54) * 100;

            int[] scales = {100, 500, 1000, 2000, 5000, 10000, 15000, 20000, 25000,
                50000, 75000, 100000, 150000, 200000, 250000, 500000, 750000, 1000000};
            for (int scale : scales) {
                if (realScale < scale) {
                    return scale;
                }
            }
            return 1000000;
        } catch (Exception e) {
            LogUtility.log("Failed to calculate best scale for the map image", e);
            throw new SOLAException(ServiceMessage.OT_WS_CLAIM_FAILED_TO_GET_MAP_SCALE);
        }
    }

    /**
     * Calculates best scale for the map image
     *
     * @param claimId = Claim ID
     * @param width = Map width
     * @param height = Map height
     * @return
     */
    @Override
    public int getBestScaleForMapImage(String claimId, int width, int height) {
        return getBestScaleForMapImage(getMap(claimId, width, height), width);
    }

    private double round(double number, int precision) {
        return Math.round(number * Math.pow(10, precision)) / Math.pow(10, precision);
    }

    private double calcDistance(ReferencedEnvelope extent) {
// double distance = JTS.orthodromicDistance(
//                new Coordinate(extent.getMaxY(),extent.getMinX()),
//                new Coordinate(extent.getMaxY(), extent.getMaxX()),
//                map.getViewport().getCoordinateReferenceSystem());
        if (isWgs84(extent.getCoordinateReferenceSystem())) {
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

    private boolean isWgs84(CoordinateReferenceSystem crs) {
        return crs.getIdentifiers().iterator().next().getCode().equals("4326");
    }

    private int getBestGridSize(double width, double height, double mpp) {
        // Calculate how many coordinates can fit the width
        int cutsPerWidth = (int) Math.round(width / (coordWidth * 2));

        if (cutsPerWidth <= 0 || width * mpp < 1 || height * mpp < 1) {
            return 0;
        }

        if (cutsPerWidth > minGridCuts) {
            cutsPerWidth = minGridCuts;
        }

        int[] steps = {100000000, 10000000, 1000000, 100000, 10000, 1000, 500, 100, 50, 10, 1};

        for (int cuts = cutsPerWidth; cuts >= 1; cuts--) {
            for (int step : steps) {
                if ((cuts * step) <= (width - (coordWidth / 2)) * mpp && step <= (height - (coordWidth / 2)) * mpp) {
                    return step;
                }
            }
        }
        return 0;
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
