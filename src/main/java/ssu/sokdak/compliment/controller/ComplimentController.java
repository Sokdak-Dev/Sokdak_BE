package ssu.sokdak.compliment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssu.sokdak.compliment.dto.ComplimentTemplateRequest;
import ssu.sokdak.compliment.service.ComplimentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ComplimentController {

    private final ComplimentService complimentService;

    @PostMapping("/compliments/embedding")
    public ResponseEntity<Void> saveComplimentTemplate(ComplimentTemplateRequest request) {
        complimentService.saveComplimentTemplate(request.getText(), request.getCategory());
        return ResponseEntity.noContent().build();
    }
}
