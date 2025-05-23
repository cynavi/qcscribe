package com.cyn.qcscribe.controller;

import com.cyn.qcscribe.service.TestCasesGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestCasesGeneratorController {

    private final TestCasesGeneratorService testCasesGeneratorService;

    public TestCasesGeneratorController(TestCasesGeneratorService testCasesGeneratorService) {
        this.testCasesGeneratorService = testCasesGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<String>> generate(@RequestBody TestCasesGeneratorRequest request) {
        return ResponseEntity.ok(testCasesGeneratorService.generateTestCases(request.title(), request.criteria()));
    }

    public record TestCasesGeneratorRequest(String title, List<String> criteria) {
    }
}
