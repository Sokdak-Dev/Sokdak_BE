package ssu.sokdak.compliment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssu.sokdak.compliment.dto.ComplimentGenerateResponse;
import ssu.sokdak.compliment.dto.ComplimentTemplateRequest;
import ssu.sokdak.compliment.service.ComplimentService;

import java.util.ArrayList;
import java.util.List;

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

    @PostMapping("/compliments/clubs/{club_id}/users/{user_id}")
    public ResponseEntity<List<ComplimentGenerateResponse>> createCompliments(@PathVariable("club_id") Long clubId,
                                                                                @PathVariable("user_id") Long userId) {
        List<ComplimentGenerateResponse> responses = complimentService.createCompliments(clubId, userId);
        return ResponseEntity.ok(responses);
    }
}
