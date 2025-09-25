package com.back.postpilot.controller;

import com.back.postpilot.entity.ContentImage;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.repository.ContentImageRepository;
import com.back.postpilot.repository.GeneratedContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImageContentController {

    private final ContentImageRepository contentImageRepository;
    private final GeneratedContentRepository generatedContentRepository;
    
    // Upload directory configuration
    @Value("${image.upload.directory:uploads/images/}")
    private String uploadDirectory;
    
    @Value("${image.serve.url-prefix:/api/images/serve/}")
    private String serveUrlPrefix;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;

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
            
            // Update URLs to be frontend-compatible
            images.getContent().forEach(this::updateImageUrls);
            
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
            
            // Update URLs to be frontend-compatible
            updateImageUrls(images);
            
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
            
            // Update URLs to be frontend-compatible
            images.getContent().forEach(this::updateImageUrls);
            
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
            
            // Update URLs to be frontend-compatible
            updateImageUrls(images);
            
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
                    // Update URL to be frontend-compatible
                    updateImageUrls(image);
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
     * Search images by tag/keyword in alt text or image prompt
     * 
     * @param tag Search keyword/tag
     * @param page Page number (0-based, default: 0)
     * @param size Page size (default: 20)
     * @return Paginated list of matching images
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ContentImage>> searchImagesByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            log.info("Searching images by tag: {}", tag);
            
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by("generatedAt").descending());
            
            Page<ContentImage> images = contentImageRepository.findByTag(tag, pageable);
            
            // Update URLs to be frontend-compatible
            images.getContent().forEach(this::updateImageUrls);
            
            log.info("Found {} images matching tag '{}'", images.getContent().size(), tag);
            return ResponseEntity.ok(images);
            
        } catch (Exception e) {
            log.error("Error searching images by tag {}", tag, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get images by tag (alternative endpoint for /api/images/tags)
     * 
     * @param tag Search keyword/tag
     * @return List of matching images
     */
    @GetMapping("/tags")
    public ResponseEntity<List<ContentImage>> getImagesByTag(@RequestParam String tag) {
        try {
            log.info("Fetching images by tag: {}", tag);
            
            List<ContentImage> images = contentImageRepository.findByTag(tag, Sort.by("generatedAt").descending());
            
            // Update URLs to be frontend-compatible
            updateImageUrls(images);
            
            log.info("Found {} images for tag '{}'", images.size(), tag);
            return ResponseEntity.ok(images);
            
        } catch (Exception e) {
            log.error("Error fetching images by tag {}", tag, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Upload an image for existing generated content
     * 
     * @param file The image file to upload
     * @param generatedContentId The ID of the generated content to associate with
     * @param fileName Custom file name (optional)
     * @param altText Alt text for the image (optional)
     * @param imagePrompt Image prompt/description (optional)
     * @param fileSize File size in bytes (optional)
     * @return Created ContentImage entity
     */
    @PostMapping("/upload-for-content")
    public ResponseEntity<ContentImage> uploadImageForContent(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("generatedContentId") Long generatedContentId,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "imagePrompt", required = false) String imagePrompt,
            @RequestParam(value = "fileSize", required = false) String fileSize) {
        
        try {
            log.info("Uploading image for content ID: {}, file: {}", generatedContentId, file.getOriginalFilename());
            
            // Validate file
            if (file.isEmpty()) {
                log.warn("Upload failed: File is empty");
                return ResponseEntity.badRequest().build();
            }
            
            // Check if generated content exists
            Optional<GeneratedContent> generatedContentOpt = generatedContentRepository.findById(generatedContentId);
            if (generatedContentOpt.isEmpty()) {
                log.warn("Upload failed: Generated content not found with ID: {}", generatedContentId);
                return ResponseEntity.notFound().build();
            }
            
            GeneratedContent generatedContent = generatedContentOpt.get();
            
            // Create unique filename to avoid conflicts
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
            
            // Store file on disk
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored on disk: {}", filePath.toAbsolutePath());
            
            // Convert file to base64 for database storage
            byte[] fileBytes = Files.readAllBytes(filePath);
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            
            // Create ContentImage entity
            ContentImage contentImage = new ContentImage();
            contentImage.setGeneratedContent(generatedContent); // Link to generated content
            contentImage.setImageUrl(generateFullImageUrl(uniqueFileName)); // Full serving URL
            contentImage.setImageData(base64Image); // Base64 data for database storage
            contentImage.setImagePrompt(imagePrompt != null ? imagePrompt : "User uploaded image");
            contentImage.setAltText(altText != null ? altText : file.getOriginalFilename());
            contentImage.setFileName(fileName != null ? fileName : file.getOriginalFilename());
            contentImage.setFileSize(fileSize != null ? fileSize : String.valueOf(file.getSize()));
            contentImage.setGeneratedAt(LocalDateTime.now());
            
            // Save to database
            ContentImage savedImage = contentImageRepository.save(contentImage);
            
            // Add to generated content's images list
            if (generatedContent.getImages() == null) {
                generatedContent.setImages(new java.util.ArrayList<>());
            }
            generatedContent.getImages().add(savedImage);
            generatedContentRepository.save(generatedContent);
            
            log.info("Successfully uploaded image with ID: {} for content ID: {}", 
                    savedImage.getId(), generatedContentId);
            
            return ResponseEntity.ok(savedImage);
            
        } catch (Exception e) {
            log.error("Error uploading image for content {}: {}", generatedContentId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Serve image file from disk
     * 
     * @param fileName The filename to serve
     * @return Image file as resource
     */
    @GetMapping("/serve/{fileName}")
    public ResponseEntity<Resource> serveImage(@PathVariable String fileName) {
        try {
            log.info("Serving image file: {}", fileName);
            
            // Check if this is a data/{id} request (fallback for null URLs)
            if (fileName.startsWith("data/")) {
                try {
                    Long imageId = Long.parseLong(fileName.substring(5)); // Remove "data/" prefix
                    return serveImageById(imageId);
                } catch (NumberFormatException e) {
                    log.warn("Invalid data ID format: {}", fileName);
                    return ResponseEntity.badRequest().build();
                }
            }
            
            // Regular file serving
            Path filePath = Paths.get(uploadDirectory).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type based on file extension
                String contentType = getContentType(fileName);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
            } else {
                log.warn("Image file not found or not readable: {}", fileName);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error serving image file {}: {}", fileName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Serve image by ID (fallback for images with null URLs)
     * 
     * @param id Image ID
     * @return Image as resource (from base64 data)
     */
    @GetMapping("/serve/data/{id}")
    public ResponseEntity<Resource> serveImageById(@PathVariable Long id) {
        try {
            log.info("Serving image by ID: {}", id);
            
            Optional<ContentImage> imageOpt = contentImageRepository.findById(id);
            if (imageOpt.isPresent()) {
                ContentImage image = imageOpt.get();
                String base64Data = image.getImageData();
                
                if (base64Data != null && !base64Data.isEmpty()) {
                    // Convert base64 to byte array
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                    
                    // Create a resource from the byte array
                    Resource resource = new org.springframework.core.io.ByteArrayResource(imageBytes);
                    
                    // Determine content type
                    String contentType = getContentType(image.getFileName() != null ? image.getFileName() : "image.jpg");
                    
                    return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
                        .body(resource);
                } else {
                    log.warn("No image data found for ID: {}", id);
                    return ResponseEntity.notFound().build();
                }
            } else {
                log.warn("Image not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error serving image by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get base64 image data from database
     * 
     * @param id Image ID
     * @return Base64 image data
     */
    @GetMapping("/{id}/data")
    public ResponseEntity<String> getImageData(@PathVariable Long id) {
        try {
            log.info("Fetching image data for ID: {}", id);
            
            Optional<ContentImage> imageOpt = contentImageRepository.findById(id);
            if (imageOpt.isPresent()) {
                ContentImage image = imageOpt.get();
                String base64Data = image.getImageData();
                
                if (base64Data != null && !base64Data.isEmpty()) {
                    return ResponseEntity.ok(base64Data);
                } else {
                    log.warn("No image data found for ID: {}", id);
                    return ResponseEntity.notFound().build();
                }
            } else {
                log.warn("Image not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error fetching image data for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Helper method to determine content type based on file extension
     */
    private String getContentType(String fileName) {
        String extension = fileName.toLowerCase();
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (extension.endsWith(".png")) {
            return "image/png";
        } else if (extension.endsWith(".gif")) {
            return "image/gif";
        } else if (extension.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }
    
    /**
     * Helper method to generate full URL for image serving
     */
    private String generateFullImageUrl(String fileName) {
        String baseUrl = "http://localhost:" + serverPort;
        if (contextPath != null && !contextPath.isEmpty()) {
            baseUrl += contextPath;
        }
        return baseUrl + serveUrlPrefix + fileName;
    }
    
    /**
     * Helper method to update image URLs in ContentImage objects
     */
    private void updateImageUrls(ContentImage image) {
        if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
            // If it's a relative path starting with our serve prefix, convert to full URL
            if (image.getImageUrl().startsWith(serveUrlPrefix)) {
                String fileName = image.getImageUrl().substring(serveUrlPrefix.length());
                image.setImageUrl(generateFullImageUrl(fileName));
            }
            // If it's already a full URL (external), leave it as is
        } else {
            // Provide fallback URL for images with null/empty URLs
            log.warn("Image {} has null or empty URL, providing fallback", image.getId());
            image.setImageUrl(generateFullImageUrl("data/" + image.getId()));
        }
    }
    
    /**
     * Helper method to update image URLs in a list of ContentImage objects
     */
    private void updateImageUrls(List<ContentImage> images) {
        images.forEach(this::updateImageUrls);
    }
    

    /**
     * Test endpoint to verify image storage setup
     * 
     * @return Status information about the image storage setup
     */
    @GetMapping("/test-setup")
    public ResponseEntity<Map<String, Object>> testImageStorageSetup() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // Check upload directory
            Path uploadPath = Paths.get(uploadDirectory);
            boolean dirExists = Files.exists(uploadPath);
            status.put("uploadDirectory", uploadDirectory);
            status.put("directoryExists", dirExists);
            status.put("directoryPath", uploadPath.toAbsolutePath().toString());
            
            // Check serve URL configuration
            status.put("serveUrlPrefix", serveUrlPrefix);
            status.put("serverPort", serverPort);
            status.put("contextPath", contextPath);
            
            // Generate sample URL
            String sampleFileName = "test-image.jpg";
            String sampleUrl = generateFullImageUrl(sampleFileName);
            status.put("sampleImageUrl", sampleUrl);
            
            // Check if directory is writable
            if (dirExists) {
                try {
                    Path testFile = uploadPath.resolve("test-write.tmp");
                    Files.write(testFile, "test".getBytes());
                    Files.delete(testFile);
                    status.put("directoryWritable", true);
                } catch (Exception e) {
                    status.put("directoryWritable", false);
                    status.put("writeError", e.getMessage());
                }
            }
            
            log.info("Image storage setup test completed: {}", status);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error testing image storage setup", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
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
