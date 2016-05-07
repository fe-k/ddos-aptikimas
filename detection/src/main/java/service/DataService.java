package service;

import exceptions.GeneralException;

import java.sql.Timestamp;
import java.util.List;

public interface DataService {

    void uploadFileToDatabase(String[] fileNames) throws GeneralException;
    String getEntropy(Timestamp start, Timestamp end, Integer increment, Integer windowWidth) throws GeneralException;
    String calculateMutualInformationReturnOptimalTimeDelay(Timestamp start, Timestamp end, Integer increment
            , Integer windowWidth, List<Integer> pointCountList) throws GeneralException;
    String getPredictionParams(Timestamp start, Timestamp end, Integer increment, Integer windowWidth
            , Integer dimensionCount, List<Integer> pointCount, Integer optimalTimeDelay, Double startAt, Integer pointsToPredict) throws GeneralException;
}


