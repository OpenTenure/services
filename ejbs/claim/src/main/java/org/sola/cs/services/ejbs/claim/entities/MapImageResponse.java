package org.sola.cs.services.ejbs.claim.entities;

import java.awt.image.BufferedImage;

public class MapImageResponse {

    private BufferedImage map;
    private int scale = 1000;
    private String crsName = "";
    private double minX = 0;
    private double maxX = 0;
    private double minY = 0;
    private double maxY = 0;
    private boolean isDegrees = false;
    
    public MapImageResponse() {
    }
    
    public BufferedImage getMap() {
        return map;
    }

    public void setMap(BufferedImage map) {
        this.map = map;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getCrsName() {
        return crsName;
    }

    public void setCrsName(String crsName) {
        this.crsName = crsName;
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public boolean isIsDegrees() {
        return isDegrees;
    }

    public void setIsDegrees(boolean isDegrees) {
        this.isDegrees = isDegrees;
    }
}
