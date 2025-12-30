package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestRest {

    @PostMapping("/test")
    public String test(@RequestParam Map<String, String> formData, Model model) {


        return formData.toString();
    }

    @GetMapping("/hello123")
    public String hello() {
        return "THIS IS IS THE NEW COMMIT";
    }
}
