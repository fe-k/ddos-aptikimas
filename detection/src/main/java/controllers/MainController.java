package controllers;

import dto.post.*;
import exceptions.ExceptionPrinter;
import exceptions.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import service.DataService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @RequestMapping(value = "/mutualInformationList", method = RequestMethod.POST)
    @ResponseBody
    public String getMutualInformationList(@ModelAttribute MutualInformationListPost mutualInformationListPost) {
        String response = null;
        try {
            Timestamp start;
            Timestamp end;
            try {
                start = new Timestamp(dateFormat.parse(mutualInformationListPost.getStart()).getTime());
                end = new Timestamp(dateFormat.parse(mutualInformationListPost.getEnd()).getTime());
            } catch (Exception e) {
                throw new GeneralException("Negalima paversti string'o į datą, blogas formatas!", e);
            }
            String entropyParams = mutualInformationListPost.getEntropyParams();
            Integer windowWidth = Integer.parseInt(entropyParams.split(" ")[0]);
            Integer increment = Integer.parseInt(entropyParams.split(" ")[1]);
            Integer dimension = Integer.parseInt(mutualInformationListPost.getDimension());

            response = dataService.getMutualInformationList(start, end, increment, windowWidth, dimension);
            response = "<pre>" + response + "</pre>";
        } catch (Exception e) {
            response = getFullExceptionMessage(e);
        }
        return response;
    }

    @RequestMapping(value = "/calculateMatrixInverse", method = RequestMethod.POST)
    @ResponseBody
    public String getMatrixInverse(@ModelAttribute MatrixInversePost matrixInversePost) {
        String response = null;
        try {
            String[] matrixRows = matrixInversePost.getMatrix().split(";");
            double[][] matrix = new double[matrixRows.length][getRowColumns(matrixRows[0]).length];

            for (int i = 0; i < matrix.length; i++) {
                matrix[i] = getRowColumns(matrixRows[i]);
            }

            response = dataService.getMatrixInverse(matrix);
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
        String exceptionString = new ExceptionPrinter().setException(e).toString();
        return new StringBuilder(FAILED).append(":\n").append(exceptionString).toString();
    }

}
