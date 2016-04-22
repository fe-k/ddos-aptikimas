package dto.mutualInformation;

import exceptions.GeneralException;

import java.util.ArrayList;
import java.util.List;

public class MutualInformationItem {

    private Double minX;
    private Double maxX;
    private Double minY;
    private Double maxY;
    private List<TwoDimensionalDot> dots = new ArrayList<TwoDimensionalDot>();
    private MutualInformationColumn column;
    private int dotCount = 0;

    protected MutualInformationItem(Double minX, Double maxX, Double minY, Double maxY, MutualInformationColumn column) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.column = column;
    }

    protected void addDot(double x, double y) throws GeneralException {
        if (x > minX && x <= maxX && y > minY && y <= maxY) {
            TwoDimensionalDot dot = new TwoDimensionalDot(x, y);
            dots.add(dot);
            incrementDotCount();
            column.incrementDotCount();
        } else {
            throw new GeneralException(String.format("Can't add dot, out of borders! x = %f, y = %f, %f - %f; %f - %f", x, y, minX, maxX, minY, maxY));
        }
    }

    private void incrementDotCount() {
        dotCount++;
    }

    protected int getDotCount() {
        return dotCount;
    }

    protected Double getMinX() {
        return minX;
    }

    protected Double getMaxX() {
        return maxX;
    }

    protected Double getMinY() {
        return minY;
    }

    protected Double getMaxY() {
        return maxY;
    }

    protected MutualInformationColumn getColumn() {
        return column;
    }

}
