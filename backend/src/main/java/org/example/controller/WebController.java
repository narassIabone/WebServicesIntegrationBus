package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "login"; // вернет login.html
    }

    @GetMapping("/index")
    public String index() {
        return "index"; // вернет index.html
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }
}