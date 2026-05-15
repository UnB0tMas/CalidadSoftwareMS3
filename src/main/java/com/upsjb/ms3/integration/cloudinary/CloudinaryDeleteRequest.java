// ruta: src/main/java/com/upsjb/ms3/integration/cloudinary/CloudinaryDeleteRequest.java
package com.upsjb.ms3.integration.cloudinary;

public record CloudinaryDeleteRequest(
        String publicId,
        String resourceType,
        Boolean invalidate
) {

    public CloudinaryDeleteRequest {
        publicId = clean(publicId);
        resourceType = clean(resourceType);
    }

    public boolean shouldInvalidate() {
        return Boolean.TRUE.equals(invalidate);
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }

        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }
}