package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object message = request.getAttribute("javax.servlet.error.message");
        Object exception = request.getAttribute("javax.servlet.error.exception");

        if (status != null) {
            model.addAttribute("status", status.toString());
        }

        if (message != null) {
            model.addAttribute("message", message.toString());
        }

        if (exception != null) {
            model.addAttribute("exception", exception.toString());
            System.err.println("Error occurred: " + exception.toString());
        }

        return "error";
    }


}
