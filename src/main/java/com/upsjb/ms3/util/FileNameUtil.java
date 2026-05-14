// ruta: src/main/java/com/upsjb/ms3/util/FileNameUtil.java
package com.upsjb.ms3.util;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class FileNameUtil {

    private static final int MAX_FILENAME_LENGTH = 180;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private FileNameUtil() {
    }

    public static String sanitizeOriginalFilename(String originalFilename) {
        if (!StringNormalizer.hasText(originalFilename)) {
            return "archivo";
        }

        String cleaned = originalFilename
                .replace("\\", "/")
                .replaceAll("^.*/", "");

        String baseName = baseName(cleaned);
        String extension = extension(cleaned);

        String safeBaseName = StringNormalizer.lowerWithoutAccents(baseName)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        if (safeBaseName.isBlank()) {
            safeBaseName = "archivo";
        }

        safeBaseName = StringNormalizer.truncate(safeBaseName, MAX_FILENAME_LENGTH);

        return extension.isBlank() ? safeBaseName : safeBaseName + "." + extension;
    }

    public static String baseName(String filename) {
        if (!StringNormalizer.hasText(filename)) {
            return "";
        }

        String cleaned = filename
                .replace("\\", "/")
                .replaceAll("^.*/", "");

        int dotIndex = cleaned.lastIndexOf(".");
        if (dotIndex <= 0) {
            return cleaned;
        }

        return cleaned.substring(0, dotIndex);
    }

    public static String extension(String filename) {
        if (!StringNormalizer.hasText(filename)) {
            return "";
        }

        String cleaned = filename
                .replace("\\", "/")
                .replaceAll("^.*/", "");

        int dotIndex = cleaned.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == cleaned.length() - 1) {
            return "";
        }

        return cleaned.substring(dotIndex + 1)
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "");
    }

    public static String generateStoredFilename(String prefix, String originalFilename) {
        String safePrefix = StringNormalizer.normalizeForCode(prefix).toLowerCase();
        String safeOriginal = sanitizeOriginalFilename(originalFilename);
        String extension = extension(safeOriginal);
        String baseName = baseName(safeOriginal);

        String timestamp = DateTimeUtil.nowUtc().format(TIMESTAMP_FORMATTER);
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        String generated = String.join(
                "-",
                safePrefix.isBlank() ? "file" : safePrefix,
                timestamp,
                uuid,
                baseName
        );

        generated = StringNormalizer.truncate(generated, MAX_FILENAME_LENGTH);

        return extension.isBlank() ? generated : generated + "." + extension;
    }

    public static String cloudinaryPublicId(String folder, String prefix, String originalFilename) {
        String safeFolder = normalizeFolder(folder);
        String storedName = generateStoredFilename(prefix, originalFilename);
        String withoutExtension = baseName(storedName);

        return safeFolder.isBlank() ? withoutExtension : safeFolder + "/" + withoutExtension;
    }

    public static String normalizeFolder(String folder) {
        if (!StringNormalizer.hasText(folder)) {
            return "";
        }

        return folder.trim()
                .replace("\\", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .replaceAll("/{2,}", "/");
    }
}