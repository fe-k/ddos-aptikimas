package controllers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @RequestMapping("/")
    String home() {
        return "redirect:index.html";
    }

    @RequestMapping("/uploadFiles")
    @ResponseBody
    String first() {
        return "XXX";
    }

}
