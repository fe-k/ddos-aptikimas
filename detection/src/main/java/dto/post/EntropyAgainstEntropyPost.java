package dto.post;

public class EntropyAgainstEntropyPost {

    private String start;
    private String end;
    private Integer windowWidth;
    private Integer increment;
    private Integer goBack;

    public EntropyAgainstEntropyPost() {
    }

    public EntropyAgainstEntropyPost(String start, String end, Integer windowWidth, Integer increment, Integer goBack) {
        this.start = start;
        this.end = end;
        this.windowWidth = windowWidth;
        this.increment = increment;
        this.goBack = goBack;
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

    public Integer getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(Integer windowWidth) {
        this.windowWidth = windowWidth;
    }

    public Integer getIncrement() {
        return increment;
    }

    public void setIncrement(Integer increment) {
        this.increment = increment;
    }

    public Integer getGoBack() {
        return goBack;
    }

    public void setGoBack(Integer goBack) {
        this.goBack = goBack;
    }
}
