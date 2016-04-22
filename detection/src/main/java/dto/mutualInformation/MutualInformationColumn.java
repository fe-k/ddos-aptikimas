package dto.mutualInformation;

public class MutualInformationColumn {

    private int dotCount = 0;

    protected void incrementDotCount() {
        dotCount++;
    }

    protected int getDotCount() {
        return dotCount;
    }
}
