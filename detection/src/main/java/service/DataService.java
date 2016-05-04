package service;

import exceptions.GeneralException;

import java.sql.Timestamp;
import java.util.List;

public interface DataService {

    void uploadFileToDatabase(String[] fileNames) throws GeneralException;
    String getEntropy(Timestamp start, Timestamp end, Integer increment, Integer windowWidth) throws GeneralException;
    String getMutualInformationList(Timestamp start, Timestamp end, Integer increment, Integer windowWidth, Integer dimension) throws GeneralException;
    String getMatrixInverse(double[][] matrix);
}


