package service;

import exceptions.GeneralException;

import java.sql.Timestamp;
import java.util.List;

public interface DataService {

    void uploadFileToDatabase(String[] fileNames) throws GeneralException;

    void insertDDoSAttack(Timestamp start, Timestamp end, String destination) throws GeneralException;

    void removeDDoSAttacks();

    String getEntropy(Timestamp start, Timestamp end, Integer increment, Integer windowWidth) throws GeneralException;

    String getOptimalTimeDelay(Timestamp start, Timestamp end, Integer increment
            , Integer windowWidth, String type, List<Integer> pointCountList) throws GeneralException;

    String predict(Timestamp start, Timestamp end, Integer increment, Integer windowWidth
            , Integer dimensionCount, Integer optimalTimeDelay) throws GeneralException;
}


