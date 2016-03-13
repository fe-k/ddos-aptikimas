package controllers;

import dto.post.EntropyPost;
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
import java.util.Date;
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

    @RequestMapping("/uploadFiles")
    @ResponseBody
    String first() {
        String response;
        try {
            dataService.uploadFileToDatabase();
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
            response = dataService.getEntropy(start, end, entropyPost.getWindowWidth(), entropyPost.getIncrement());
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
