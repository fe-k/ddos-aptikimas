package dto.post;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by K on 4/28/2016.
 */
public class OptimalTimeDelayPost extends EntropyPost {

    private String pointCount;

    public OptimalTimeDelayPost() {
    }

    public String getPointCount() {
        return pointCount;
    }

    public void setPointCount(String pointCount) {
        this.pointCount = pointCount;
    }

    @JsonIgnore
    public List<Integer> getPointCountList() {
        List<Integer> pointCountList = new ArrayList<Integer>();

        String[] separatedBySpaces = pointCount.split(" ");
        for (String pointCountString: separatedBySpaces) {
            pointCountList.add(Integer.parseInt(pointCountString));
        }

        return pointCountList;
    }
}
