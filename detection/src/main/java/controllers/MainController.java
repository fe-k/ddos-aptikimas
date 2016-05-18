package controllers;

import dto.post.*;
import exceptions.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import service.DataService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private DataService dataService;

    private static final String FAILED = "FAILED";
    private static final String SUCCESS = "SUCCESS";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @RequestMapping("/")
    String home() {
        return "redirect:index.html";
    }

    @RequestMapping(value = "/uploadFiles", method = RequestMethod.POST)
    @ResponseBody
    String uploadFile(@ModelAttribute UploadFilePost uploadFilePost) {
        String response;
        try {
            String[] fileNames = uploadFilePost.getFileName().split(";");
            dataService.uploadFileToDatabase(fileNames);
            response = SUCCESS;
        } catch(Exception e) {
            response = getFullExceptionMessage(e);
        }
        return response;
    }

    @RequestMapping(value = "/createDDoS", method = RequestMethod.POST)
    @ResponseBody
    String createDDoS(@ModelAttribute CreateDDoSPost createDDoSPost) {
        String response;
        try {
            Timestamp start = createDDoSPost.getStartTimestamp(dateFormat);
            Timestamp end = createDDoSPost.getEndTimestamp(dateFormat);
            String destination = createDDoSPost.getDestination();
            dataService.insertDDoSAttack(start, end, destination);
            response = SUCCESS;
        } catch(Exception e) {
            response = getFullExceptionMessage(e);
        }
        return response;
    }

    @RequestMapping(value = "/deleteDDoS", method = RequestMethod.POST)
    @ResponseBody
    String deleteDDoS(@ModelAttribute CreateDDoSPost createDDoSPost) {
        String response;
        try {
            dataService.removeDDoSAttacks();
            response = SUCCESS;
        } catch(Exception e) {
            response = getFullExceptionMessage(e);
        }
        return response;
    }

    @RequestMapping(value = "/entropy", method = RequestMethod.POST)
    @ResponseBody
    public String getEntropy(@ModelAttribute EntropyPost entropyPost) {
        String response = null;
        try {
            Timestamp start;
            Timestamp end;
            try {
                start = new Timestamp(dateFormat.parse(entropyPost.getStart()).getTime());
                end = new Timestamp(dateFormat.parse(entropyPost.getEnd()).getTime());
            } catch (Exception e) {
                throw new GeneralException("Negalima paversti string'o į datą, blogas formatas!", e);
            }
            response = dataService.getEntropy(start, end, entropyPost.getIncrement(), entropyPost.getWindowWidth());
            response = "<pre>" + response + "</pre>";
        } catch (Exception e) {
            response = getFullExceptionMessage(e);
        }
        return response;
    }

    @RequestMapping(value = "/optimalTimeDelay", method = RequestMethod.POST)
    @ResponseBody
    public String getOptimalTimeDelay(@ModelAttribute OptimalTimeDelayPost optimalTimeDelayPost) {
        String response = null;
        try {
            Timestamp start = optimalTimeDelayPost.getStartTimestamp(dateFormat);
            Timestamp end = optimalTimeDelayPost.getEndTimestamp(dateFormat);
            Integer windowWidth = optimalTimeDelayPost.getWindowWidth();
            Integer increment = optimalTimeDelayPost.getIncrement();
            List<Integer> pointCounts = optimalTimeDelayPost.getPointCountList();

            response = dataService.getOptimalTimeDelay(start, end, increment, windowWidth, pointCounts);
            response = "<pre>" + response + "</pre>";
        } catch (Exception e) {
            response = getFullExceptionMessage(e);
        }
        return response;
    }

    @RequestMapping(value = "/calculatePredictionParams", method = RequestMethod.POST)
    @ResponseBody
    public String getPredictionParams(@ModelAttribute PredictionParamsPost predictionParamsPost) {
        String response = null;
        try {
            Timestamp start = predictionParamsPost.getStartTimestamp(dateFormat);
            Timestamp end = predictionParamsPost.getEndTimestamp(dateFormat);
            Integer windowWidth = predictionParamsPost.getWindowWidth();
            Integer increment = predictionParamsPost.getIncrement();
            Integer dimensionCount = predictionParamsPost.getDimensionCount();
            List<Integer> pointCountList = predictionParamsPost.getPointCountList();
            Integer optimalTimeDelay = predictionParamsPost.getOptimalTimeDelay();
            Double startAt = predictionParamsPost.getStartAt();
            Integer pointsToPredict = predictionParamsPost.getPointsToPredict();
            Integer neighbourPointLimit = predictionParamsPost.getNeighbourPointLimit();

            response = dataService.predict(start, end, increment, windowWidth, dimensionCount
                    , optimalTimeDelay);
            response = "<pre>" + response + "</pre>";
        } catch (Exception e) {
            response = getFullExceptionMessage(e);
        }
        return response;
    }

    private double[] getRowColumns(String row) {
        String[] values = row.split(" ");
        int size = values.length;
        double[] doubleRow = new double[size];
        for (int i = 0; i < size; i++) {
            doubleRow[i] = Double.parseDouble(values[i]);
        }
        return doubleRow;
    }

    private String getFullExceptionMessage(Exception e) {
        //String exceptionString = new ExceptionPrinter().setException(e).toString();
        e.printStackTrace();
        return new StringBuilder(FAILED).toString();//.append(":\n").append(exceptionString).toString();
    }

}
