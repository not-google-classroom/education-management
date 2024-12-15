package com.org.education_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("api/test")
public class TestController {
    @GetMapping(value = "api1", produces = "application/json")
    private HashMap<String, String> get1() {
        HashMap map = new HashMap<>();
        map.put("api1", "working");
        return map;
    }

    @GetMapping(value = "api2", produces = "application/json")
    private HashMap<String, String> get2() {
        HashMap map = new HashMap<>();
        map.put("api2", "working");
        return map;
    }
}
