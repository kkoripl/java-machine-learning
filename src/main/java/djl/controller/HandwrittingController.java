package djl.controller;

import ai.djl.translate.TranslateException;
import djl.handwritting.dto.HandwrittingClassificationDto;
import djl.service.HandwrittingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/handwritting")
public class HandwrittingController {
    private final HandwrittingService handwrittingService;

    @PostMapping(value = "/recognize", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HandwrittingClassificationDto> recognize(@RequestParam("image") MultipartFile image) throws TranslateException, IOException {
        return ResponseEntity.ok(handwrittingService.predict(image));
    }
}
