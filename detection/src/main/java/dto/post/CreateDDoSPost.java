package dto.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import exceptions.GeneralException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class CreateDDoSPost {

    private String start;
    private String end;
    private String destination;

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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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
