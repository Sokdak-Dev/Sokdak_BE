package ssu.sokdak.compliment.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ssu.sokdak.compliment.dto.ComplimentGenerateResponse;
import ssu.sokdak.compliment.dto.ComplimentHistoryResponse;
import ssu.sokdak.compliment.dto.ComplimentSelectRequest;
import ssu.sokdak.compliment.dto.ComplimentTemplateRequest;
import ssu.sokdak.compliment.service.ComplimentIndexService;
import ssu.sokdak.compliment.service.ComplimentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ssu.sokdak.user.api.MemberController.SESSION_KEY;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ComplimentController {

    private final ComplimentService complimentService;
    private final ComplimentIndexService complimentIndexService;

    @PostMapping("/compliments/embedding")
    public ResponseEntity<Void> saveComplimentTemplate(@RequestBody ComplimentTemplateRequest request) {
        complimentService.saveComplimentTemplate(request.getText(), request.getCategory());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/compliments/clubs/{club_id}")
    public ResponseEntity<List<ComplimentGenerateResponse>> createCompliments(@PathVariable("club_id") Long clubId,
                                                                              HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_KEY);
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        List<ComplimentGenerateResponse> responses = complimentService.createCompliments(clubId, userId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/compliments/select")
    public ResponseEntity<List<ComplimentGenerateResponse>> selectComplimentUser(@RequestBody ComplimentSelectRequest request) {
        complimentService.selectComplimentUser(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/compliments/send")
    public ResponseEntity<List<ComplimentHistoryResponse>> getSentCompliments(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_KEY);
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return ResponseEntity.ok(complimentService.getSentCompliments(userId));
    }

    @GetMapping("/compliments/received")
    public ResponseEntity<List<ComplimentHistoryResponse>> getReceivedCompliments(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_KEY);
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return ResponseEntity.ok(complimentService.getReceivedCompliments(userId));
    }

    @PostMapping("/reindex/compliments")
    public Map<String, Object> reindexCompliments(
            @RequestParam(defaultValue = "200") int batchSize
    ) {
        int indexed = complimentIndexService.reindexAll(batchSize);
        return Map.of(
                "indexed", indexed,
                "batchSize", batchSize
        );
    }
}
