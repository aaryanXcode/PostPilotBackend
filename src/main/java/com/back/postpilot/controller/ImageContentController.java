package com.back.postpilot.controller;

import com.back.postpilot.entity.ContentImage;
import com.back.postpilot.repository.ContentImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImageContentController {

    private final ContentImageRepository contentImageRepository;

    /**
     * Get all images with pagination for gallery view
     * 
     * @param page Page number (0-based, default: 0)
     * @param size Page size (default: 20)
     * @param sortBy Sort field (default: generatedAt)
     * @param sortDir Sort direction (asc/desc, default: desc)
     * @return Paginated list of images
     */
    @GetMapping
    public ResponseEntity<Page<ContentImage>> getAllImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "generatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            log.info("Fetching images - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                    page, size, sortBy, sortDir);
            
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ContentImage> images = contentImageRepository.findAll(pageable);
            
            log.info("Found {} images on page {}", images.getContent().size(), page);
            return ResponseEntity.ok(images);
            
        } catch (Exception e) {
            log.error("Error fetching images", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all images without pagination (for simple gallery)
     * 
     * @return List of all images
     */
    @GetMapping("/all")
    public ResponseEntity<List<ContentImage>> getAllImagesSimple() {
        try {
            log.info("Fetching all images");
            List<ContentImage> images = contentImageRepository.findAll(
                Sort.by("generatedAt").descending()
            );
            
            log.info("Found {} total images", images.size());
            return ResponseEntity.ok(images);
            
        } catch (Exception e) {
            log.error("Error fetching all images", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get images by user ID
     * 
     * @param userId User ID
     * @param page Page number (0-based, default: 0)
     * @param size Page size (default: 20)
     * @return Paginated list of user's images
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ContentImage>> getImagesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            log.info("Fetching images for user: {}", userId);
            
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by("generatedAt").descending());
            
            Page<ContentImage> images = contentImageRepository.findByUserId(userId, pageable);
            
            log.info("Found {} images for user {}", images.getContent().size(), userId);
            return ResponseEntity.ok(images);
            
        } catch (Exception e) {
            log.error("Error fetching images for user {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get images by session ID
     * 
     * @param sessionId Session ID
     * @return List of images for the session
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ContentImage>> getImagesBySession(
            @PathVariable String sessionId) {
        
        try {
            log.info("Fetching images for session: {}", sessionId);
            
            List<ContentImage> images = contentImageRepository.findBySessionId(sessionId);
            
            log.info("Found {} images for session {}", images.size(), sessionId);
            return ResponseEntity.ok(images);
            
        } catch (Exception e) {
            log.error("Error fetching images for session {}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get a specific image by ID
     * 
     * @param id Image ID
     * @return Image details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContentImage> getImageById(@PathVariable Long id) {
        try {
            log.info("Fetching image with ID: {}", id);
            
            return contentImageRepository.findById(id)
                .map(image -> {
                    log.info("Found image: {}", image.getFileName());
                    return ResponseEntity.ok(image);
                })
                .orElseGet(() -> {
                    log.warn("Image not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
                
        } catch (Exception e) {
            log.error("Error fetching image with ID {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete an image by ID
     * 
     * @param id Image ID
     * @return Success/error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        try {
            log.info("Deleting image with ID: {}", id);
            
            if (contentImageRepository.existsById(id)) {
                contentImageRepository.deleteById(id);
                log.info("Successfully deleted image with ID: {}", id);
                return ResponseEntity.ok().build();
            } else {
                log.warn("Image not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error deleting image with ID {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
