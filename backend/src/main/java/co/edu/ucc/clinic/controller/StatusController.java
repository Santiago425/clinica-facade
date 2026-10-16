package co.edu.ucc.clinic.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatusController {

    @GetMapping("/")
    public Map<String, String> status() {
        return Map.of(
                "service", "Sistema de Gestión Médica - API",
                "status", "UP",
                "basePath", "/api/clinica"
        );
    }
}
