package com.example.website.citu.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/well-known/acme-challenge")
public class AcmeChallengeController {

    @GetMapping("/{token}")
    public ResponseEntity<String> serveChallenge(@PathVariable String token) {
        Path path = Paths.get("C:/wwwroot/.well-known/acme-challenge/" + token);
        try {
            String content = Files.readString(path);
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
