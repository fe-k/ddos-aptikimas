package service;

import exceptions.GeneralException;

import java.sql.Timestamp;
import java.util.List;

public interface DataService {

    void uploadFileToDatabase(String[] fileNames) throws GeneralException;
    String getEntropy(Timestamp start, Timestamp end, Integer increment, Integer windowWidth) throws GeneralException;
    String getMutualInformation(List<Double> currentValues, List<Double> shiftedValues, int numberOfItems) throws GeneralException;
    String getEntropyAgainstEntropy(Timestamp start, Timestamp end, Integer increment, Integer windowWidth, Integer goBack) throws GeneralException;
}


