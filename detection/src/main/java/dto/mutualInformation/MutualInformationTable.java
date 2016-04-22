package dto.mutualInformation;

import dto.ValueInTimeInterval;
import exceptions.GeneralException;

import java.util.ArrayList;
import java.util.List;

public class MutualInformationTable {

    protected List<MutualInformationRow> rows = new ArrayList<MutualInformationRow>();
    protected List<MutualInformationColumn> columns = new ArrayList<MutualInformationColumn>();
    private Double minValueX;
    private Double maxValueX;
    private Double minValueY;
    private Double maxValueY;
    private Double mutualInformation = 0.0;
    private int dotCount = 0;
    private int numberOfItems;
    private Double reduceLowerLimitBy = 0.001;

    public MutualInformationTable(List<Double> currentValues, List<Double> shiftedVales, int numberOfItems) throws GeneralException {
        setMinAndMax(currentValues, true);
        setMinAndMax(shiftedVales, false);

        this.numberOfItems = numberOfItems;

        createTable(numberOfItems, minValueX, maxValueX, minValueY, maxValueY);

        for (int i = 0; i < currentValues.size(); i++) {
            Double x = currentValues.get(i);
            Double y = shiftedVales.get(i);

            addDot(x, y);
        }

        calculateMutualInformation();
    }

    private void createTable(int numberOfItems, Double minValueX, Double maxValueX, Double minValueY, Double maxValueY) throws GeneralException {
        Double stepX = (maxValueX - minValueX) / numberOfItems;
        Double stepY = (maxValueY - minValueY) / numberOfItems;

        for (int i = 0; i < numberOfItems; i++) {
            columns.add(new MutualInformationColumn());
        }

        //Susikuriam lentelę
        for (int i = 0; i < numberOfItems; i++) {
            MutualInformationRow row = new MutualInformationRow(minValueY, minValueY + stepY);

            for (int j = 0; j < numberOfItems; j++) {
                MutualInformationItem item = new MutualInformationItem(minValueX, minValueX + stepX, minValueY, minValueY + stepY, columns.get(j));
                row.addItem(item);

                minValueX = minValueX + stepX;
            }

            rows.add(row);
            minValueX = minValueX - numberOfItems * stepX;
            minValueY = minValueY + stepY;
        }
    }

    private void addDot(double x, double y) throws GeneralException {
        for (MutualInformationRow row: rows) {
            if (y > row.getMinY() && y <= row.getMaxY()) {
                row.addDot(x, y);
                incrementDotCount();
            }
        }
    }

    private void incrementDotCount() {
        dotCount++;
    }

    private void setMinAndMax(List<Double> values, boolean x) {
        Double minValue = values.get(0);
        Double maxValue = values.get(0);

        for (Double currentValue: values) {
            if (currentValue < minValue) {
                minValue = currentValue;
            }
            if (currentValue > maxValue) {
                maxValue = currentValue;
            }
        }

        if (x) {
            minValueX = minValue - reduceLowerLimitBy;
            maxValueX = maxValue;
        } else {
            minValueY = minValue - reduceLowerLimitBy;
            maxValueY = maxValue;
        }
    }

    private void calculateMutualInformation() {
        int size = rows.size();
        for (int i = 0; i < size; i++) {
            MutualInformationRow row = rows.get(i);
            for (int j = 0; j < size; j++) {
                MutualInformationItem item = row.getItem(j);
                MutualInformationColumn column = item.getColumn();

                int itemDotCount = item.getDotCount();
                int rowDotCount = row.getDotCount();
                int columnDotCount = column.getDotCount();

                if (itemDotCount > 0) {
                    double px = columnDotCount / (double) dotCount;
                    double py = rowDotCount / (double) dotCount;
                    double pxy = itemDotCount / (double) dotCount;

                    mutualInformation += pxy * Math.log(pxy / (px * py)) / Math.log(2);
                }
            }
        }
    }

    public Double getMutualInformation() {
        return mutualInformation;
    }
}
