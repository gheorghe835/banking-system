package com.bank;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleTestController {

    @GetMapping("/health")
    public String health() {
        return "Aplicația rulează!";
    }
}
