package dto.post;

/**
 * Created by K on 4/28/2016.
 */
public class MutualInformationListPost {

    private String start;
    private String end;
    private String entropyParams;
    private String dimension;

    public MutualInformationListPost() {
    }

    public MutualInformationListPost(String start, String end, String entropyParams, String dimension) {
        this.start = start;
        this.end = end;
        this.entropyParams = entropyParams;
        this.dimension = dimension;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getEntropyParams() {
        return entropyParams;
    }

    public void setEntropyParams(String entropyParams) {
        this.entropyParams = entropyParams;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
}
