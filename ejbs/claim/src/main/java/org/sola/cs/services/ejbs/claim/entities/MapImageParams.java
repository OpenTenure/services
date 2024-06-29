package org.sola.cs.services.ejbs.claim.entities;

public class MapImageParams {
    private int width = 700;
    private int height = 400;
    private String scaleLabel = "Scale";
    private boolean showScale = true;
    private boolean showNorth = true;
    private boolean showGrid = true;
    private boolean showLabels = true;
    private boolean showPoints = false;
    private boolean showNeighbors = true;
    private boolean showSmaller = false;
    private boolean useWmsBackground = false;
    private boolean useGoogleBackground = false;
    private boolean forPdf = true;
    
    public MapImageParams() {
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getScaleLabel() {
        return scaleLabel;
    }

    public void setScaleLabel(String scaleLabel) {
        this.scaleLabel = scaleLabel;
    }

    public boolean isShowScale() {
        return showScale;
    }

    public void setShowScale(boolean showScale) {
        this.showScale = showScale;
    }

    public boolean isShowNorth() {
        return showNorth;
    }

    public void setShowNorth(boolean showNorth) {
        this.showNorth = showNorth;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public boolean isShowLabels() {
        return showLabels;
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
    }

    public boolean isShowPoints() {
        return showPoints;
    }

    public void setShowPoints(boolean showPoints) {
        this.showPoints = showPoints;
    }

    public boolean isShowNeighbors() {
        return showNeighbors;
    }

    public void setShowNeighbors(boolean showNeighbors) {
        this.showNeighbors = showNeighbors;
    }

    public boolean isShowSmaller() {
        return showSmaller;
    }

    public void setShowSmaller(boolean showSmaller) {
        this.showSmaller = showSmaller;
    }

    public boolean isUseWmsBackground() {
        return useWmsBackground;
    }

    public void setUseWmsBackground(boolean useWmsBackground) {
        this.useWmsBackground = useWmsBackground;
    }

    public boolean isUseGoogleBackground() {
        return useGoogleBackground;
    }

    public void setUseGoogleBackground(boolean useGoogleBackground) {
        this.useGoogleBackground = useGoogleBackground;
    }

    public boolean isForPdf() {
        return forPdf;
    }

    public void setForPdf(boolean forPdf) {
        this.forPdf = forPdf;
    }
}
