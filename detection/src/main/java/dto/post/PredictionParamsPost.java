package dto.post;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class PredictionParamsPost extends EntropyPost {

    private Integer dimensionCount;
    private String pointCount;
    private Integer optimalTimeDelay;
    private Double startAt;
    private Integer pointsToPredict;

    public Integer getDimensionCount() {
        return dimensionCount;
    }

    public void setDimensionCount(Integer dimensionCount) {
        this.dimensionCount = dimensionCount;
    }

    public String getPointCount() {
        return pointCount;
    }

    public void setPointCount(String pointCount) {
        this.pointCount = pointCount;
    }

    public Integer getOptimalTimeDelay() {
        return optimalTimeDelay;
    }

    public void setOptimalTimeDelay(Integer optimalTimeDelay) {
        this.optimalTimeDelay = optimalTimeDelay;
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

    public Double getStartAt() {
        return startAt;
    }

    public void setStartAt(Double startAt) {
        this.startAt = startAt;
    }

    public Integer getPointsToPredict() {
        return pointsToPredict;
    }

    public void setPointsToPredict(Integer pointsToPredict) {
        this.pointsToPredict = pointsToPredict;
    }
}
