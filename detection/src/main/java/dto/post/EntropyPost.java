package dto.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import exceptions.GeneralException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class EntropyPost {

    private String start;
    private String end;
    private Integer windowWidth;
    private Integer increment;

    public EntropyPost() {
    }

    public EntropyPost(String start, String end, Integer windowWidth, Integer increment) {
        this.start = start;
        this.end = end;
        this.windowWidth = windowWidth;
        this.increment = increment;
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

    @JsonIgnore
    public Timestamp getStartTimestamp(SimpleDateFormat dateFormat) throws GeneralException {
        try {
            return new Timestamp(dateFormat.parse(start).getTime());
        } catch (Exception e) {
            throw new GeneralException("Negalima paversti string'o į datą, blogas formatas!", e);
        }
    }

    @JsonIgnore
    public Timestamp getEndTimestamp(SimpleDateFormat dateFormat) throws GeneralException {
        try {
            return new Timestamp(dateFormat.parse(end).getTime());
        } catch (Exception e) {
            throw new GeneralException("Negalima paversti string'o į datą, blogas formatas!", e);
        }
    }
}
