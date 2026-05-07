package io.binghe.ai.review.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description FrontendController
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@RestController
public class FrontendController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("static/index.html")) {
            if (is == null) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("<h1>Error loading frontend</h1>");
        }
    }
}
