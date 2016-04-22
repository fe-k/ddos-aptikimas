package dto.post;

public class MutualInformationPost {
    private String currentValues;//comma separated
    private String shiftedValues;//comma separated
    private int numberOfItems;

    public MutualInformationPost() {
    }

    public MutualInformationPost(String currentValues, String shiftedValues, int numberOfItems) {
        this.currentValues = currentValues;
        this.shiftedValues = shiftedValues;
        this.numberOfItems = numberOfItems;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public void setNumberOfItems(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    public String getCurrentValues() {
        return currentValues;
    }

    public void setCurrentValues(String currentValues) {
        this.currentValues = currentValues;
    }

    public String getShiftedValues() {
        return shiftedValues;
    }

    public void setShiftedValues(String shiftedValues) {
        this.shiftedValues = shiftedValues;
    }
}
