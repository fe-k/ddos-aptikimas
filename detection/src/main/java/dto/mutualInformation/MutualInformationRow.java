package dto.mutualInformation;

import dto.mutualInformation.MutualInformationItem;
import exceptions.GeneralException;

import java.util.ArrayList;
import java.util.List;

public class MutualInformationRow {

    private List<MutualInformationItem> items = new ArrayList<MutualInformationItem>();
    private int dotCount = 0;
    private Double minY;
    private Double maxY;

    protected MutualInformationItem getItem(int index) {
        return items.get(index);
    }

    protected MutualInformationRow(Double minY, Double maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    protected void addItem(MutualInformationItem item) throws GeneralException {
        //jeigu tuščias masyvas galime pridėti bet kokį item
        if (items.isEmpty()) {
            items.add(item);
        } else {
            MutualInformationItem lastItem = items.get(items.size() - 1);
            if (lastItem.getMaxX().compareTo(item.getMinX()) != 0) {
                throw new GeneralException("Max X not equal to Min X!");
            }
            if (minY.compareTo(item.getMinY()) != 0 && maxY.compareTo(item.getMaxY()) != 0) {
                throw new GeneralException("Not the same row!");
            }
            items.add(item);
        }
    }

    private void incrementDotCount() {
        dotCount++;
    }

    protected int getDotCount() {
        return dotCount;
    }

    protected void addDot(double x, double y) throws GeneralException {
        boolean isAdded = false;
        for (MutualInformationItem item: items) {
            if (x > item.getMinX() && x <= item.getMaxX() && y > item.getMinY() && y <= item.getMaxY()) {
                item.addDot(x, y);
                incrementDotCount();
                isAdded = true;
                break;
            }
        }
        if (!isAdded) {
            throw new GeneralException("Dot was not added!");
        }
    }

    protected Double getMinY() {
        return minY;
    }

    protected Double getMaxY() {
        return maxY;
    }
}
