package com.iti.attendance.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    @Value("${app.error.mode:prod}")
    private String errorMode;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
        if ("dev".equalsIgnoreCase(errorMode)) {
            options = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.EXCEPTION,
                    ErrorAttributeOptions.Include.MESSAGE,
                    ErrorAttributeOptions.Include.STACK_TRACE,
                    ErrorAttributeOptions.Include.BINDING_ERRORS);
        }
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(new ServletWebRequest(request), options);
        model.addAttribute("timestamp", attributes.get("timestamp"));
        model.addAttribute("path", attributes.get("path"));
        model.addAttribute("status", attributes.get("status"));
        model.addAttribute("error", attributes.get("error"));
        model.addAttribute("message", attributes.get("message"));
        model.addAttribute("trace", attributes.get("trace"));
        model.addAttribute("errorMode", errorMode);
        return "dev".equalsIgnoreCase(errorMode) ? "error-dev" : "error";
    }
}
