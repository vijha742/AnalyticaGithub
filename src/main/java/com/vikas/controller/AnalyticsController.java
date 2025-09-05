package com.vikas.controller;

import com.vikas.model.CodeMetrics;
import com.vikas.model.ReadmeQuality;
import com.vikas.model.TechnicalProfile;
import com.vikas.service.impl.RepositoryAnalyticsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/public/u/{username}")
public class AnalyticsController {
    private final RepositoryAnalyticsServiceImpl analyticsService;

    @GetMapping("/readme-analysis")
    public ResponseEntity<?> getAnalysis(@PathVariable String username) {
        List<ReadmeQuality> data = analyticsService.analyzeReadmeQuality(username);
        if (data != null) {
            return ResponseEntity.ok(data);
        } else return ResponseEntity.notFound().build();
    }

    @GetMapping("/tech-analysis")
    public ResponseEntity<?> getTechAnalysis(@PathVariable String username) {
        TechnicalProfile data = analyticsService.getTechnicalProfile(username);
        if (data != null) {
            return ResponseEntity.ok(data);
        } else return ResponseEntity.notFound().build();
    }

    @GetMapping("/code-analysis")
    public ResponseEntity<?> getCodeAnalysis(@PathVariable String username) {
        List<CodeMetrics> data = analyticsService.getCodeMetrics(username);
        if (data != null) {
            return ResponseEntity.ok(data);
        } else return ResponseEntity.notFound().build();
    }

}
