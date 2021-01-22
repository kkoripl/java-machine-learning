package djl.controller;

import ai.djl.translate.TranslateException;
import djl.objectDetection.ObjectDetectionService;
import djl.objectDetection.dto.ObjectDetectionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/objectDetection")
public class ObjectDetectionController {
    private final ObjectDetectionService detectionService;

    @PostMapping(value = "/detect", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectDetectionDto> detect(@RequestParam("image") MultipartFile image) throws TranslateException, IOException {
        return ResponseEntity.ok(detectionService.autoConfigDetectObjects(image));
    }

    @PostMapping(value = "/detect-external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ObjectDetectionDto> detectExternal(@RequestParam("image") MultipartFile image) throws TranslateException, IOException {
        return ResponseEntity.ok(detectionService.externalDetectObjects(image));
    }
}
