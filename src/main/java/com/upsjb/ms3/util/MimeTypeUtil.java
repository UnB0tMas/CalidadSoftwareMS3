// ruta: src/main/java/com/upsjb/ms3/util/MimeTypeUtil.java
package com.upsjb.ms3.util;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class MimeTypeUtil {

    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_WEBP = "image/webp";
    public static final String IMAGE_JPG = "image/jpg";

    private static final Set<String> ALLOWED_PRODUCT_IMAGE_TYPES = Set.of(
            IMAGE_JPEG,
            IMAGE_JPG,
            IMAGE_PNG,
            IMAGE_WEBP
    );

    private static final Map<String, String> EXTENSION_TO_MIME = Map.of(
            "jpg", IMAGE_JPEG,
            "jpeg", IMAGE_JPEG,
            "png", IMAGE_PNG,
            "webp", IMAGE_WEBP
    );

    private static final Map<String, String> MIME_TO_EXTENSION = Map.of(
            IMAGE_JPEG, "jpg",
            IMAGE_JPG, "jpg",
            IMAGE_PNG, "png",
            IMAGE_WEBP, "webp"
    );

    private MimeTypeUtil() {
    }

    public static String normalize(String contentType) {
        if (!StringNormalizer.hasText(contentType)) {
            return "";
        }

        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isImage(String contentType) {
        return normalize(contentType).startsWith("image/");
    }

    public static boolean isAllowedProductImage(String contentType) {
        return ALLOWED_PRODUCT_IMAGE_TYPES.contains(normalize(contentType));
    }

    public static boolean isAllowedProductImageExtension(String extension) {
        return EXTENSION_TO_MIME.containsKey(normalizeExtension(extension));
    }

    public static String extensionFromMimeType(String contentType) {
        return MIME_TO_EXTENSION.getOrDefault(normalize(contentType), "");
    }

    public static String mimeTypeFromExtension(String extension) {
        return EXTENSION_TO_MIME.getOrDefault(normalizeExtension(extension), "");
    }

    public static String normalizeExtension(String extension) {
        if (!StringNormalizer.hasText(extension)) {
            return "";
        }

        return extension
                .replace(".", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public static Set<String> allowedProductImageTypes() {
        return ALLOWED_PRODUCT_IMAGE_TYPES;
    }
}