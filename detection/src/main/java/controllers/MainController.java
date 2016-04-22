package controllers;

import dto.post.EntropyAgainstEntropyPost;
import dto.post.EntropyPost;
import dto.post.MutualInformationPost;
import dto.post.UploadFilePost;
import exceptions.ExceptionPrinter;
import exceptions.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import service.DataService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @RequestMapping(value = "/mutualInformation", method = RequestMethod.POST)
    @ResponseBody
    public String getMutualInformation(@ModelAttribute MutualInformationPost mutualInformationPost) {
        String response = null;
        try {
            String[] currentStringValues = mutualInformationPost.getCurrentValues().split(",");
            String[] shiftedStringValues = mutualInformationPost.getShiftedValues().split(",");

            if (currentStringValues.length != shiftedStringValues.length) {
                throw new GeneralException("Lengths should be identical!");
            }

            List<Double> currentValues = new ArrayList<Double>();
            for (String currentValue: currentStringValues) {
                currentValues.add(Double.parseDouble(currentValue));
            }
            List<Double> shiftedValues = new ArrayList<Double>();
            for (String shiftedValue: shiftedStringValues) {
                shiftedValues.add(Double.parseDouble(shiftedValue));
            }

            int numberOfItems = mutualInformationPost.getNumberOfItems();

            return dataService.getMutualInformation(currentValues, shiftedValues, numberOfItems);
        } catch (Exception e) {
            response = getFullExceptionMessage(e);
        }
        return response;
    }

    @RequestMapping(value = "/entropyAgainstEntropy", method = RequestMethod.POST)
    @ResponseBody
    public String getEntropyAgainstEntropy(@ModelAttribute EntropyAgainstEntropyPost entropyAgainstEntropyPost) {
        String response = null;
        try {
            Timestamp start;
            Timestamp end;
            try {
                start = new Timestamp(dateFormat.parse(entropyAgainstEntropyPost.getStart()).getTime());
                end = new Timestamp(dateFormat.parse(entropyAgainstEntropyPost.getEnd()).getTime());
            } catch (Exception e) {
                throw new GeneralException("Negalima paversti string'o į datą, blogas formatas!", e);
            }
            response = dataService.getEntropyAgainstEntropy(start, end, entropyAgainstEntropyPost.getIncrement(), entropyAgainstEntropyPost.getWindowWidth(), entropyAgainstEntropyPost.getGoBack());
            response = "<pre>" + response + "</pre>";
        } catch (Exception e) {
            response = getFullExceptionMessage(e);
        }
        return response;
    }

    private String getFullExceptionMessage(Exception e) {
        String exceptionString = new ExceptionPrinter().setException(e).toString();
        return new StringBuilder(FAILED).append(":\n").append(exceptionString).toString();
    }

}
