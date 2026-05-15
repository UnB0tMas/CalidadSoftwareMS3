// ruta: src/main/java/com/upsjb/ms3/integration/cloudinary/CloudinaryUploadRequest.java
package com.upsjb.ms3.integration.cloudinary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record CloudinaryUploadRequest(
        byte[] content,
        String originalFilename,
        String contentType,
        String folder,
        String publicId,
        String resourceType,
        Boolean overwrite,
        Boolean uniqueFilename,
        Boolean useFilename,
        Map<String, String> metadata,
        List<String> tags
) {

    public CloudinaryUploadRequest {
        content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
        originalFilename = clean(originalFilename);
        contentType = clean(contentType);
        folder = clean(folder);
        publicId = clean(publicId);
        resourceType = clean(resourceType);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        tags = tags == null ? List.of() : List.copyOf(new ArrayList<>(tags));
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }

    public boolean hasContent() {
        return content.length > 0;
    }

    public long sizeBytes() {
        return content.length;
    }

    public boolean hasCustomPublicId() {
        return publicId != null && !publicId.isBlank();
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }

        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }
}