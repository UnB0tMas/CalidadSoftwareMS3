// ruta: src/main/java/com/upsjb/ms3/util/DateTimeUtil.java
package com.upsjb.ms3.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateTimeUtil {

    public static final ZoneId ZONE_LIMA = ZoneId.of("America/Lima");
    public static final ZoneId ZONE_UTC = ZoneId.of("UTC");

    private static final Clock UTC_CLOCK = Clock.systemUTC();

    private DateTimeUtil() {
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(UTC_CLOCK);
    }

    public static LocalDateTime nowLima() {
        return LocalDateTime.now(ZONE_LIMA);
    }

    public static ZonedDateTime zonedNowUtc() {
        return ZonedDateTime.now(ZONE_UTC);
    }

    public static ZonedDateTime zonedNowLima() {
        return ZonedDateTime.now(ZONE_LIMA);
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    public static boolean isValidRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return true;
        }
        return !end.isBefore(start);
    }

    public static boolean isCurrentBetween(LocalDateTime start, LocalDateTime end) {
        return isCurrentBetween(nowUtc(), start, end);
    }

    public static boolean isCurrentBetween(LocalDateTime current, LocalDateTime start, LocalDateTime end) {
        if (current == null) {
            return false;
        }

        boolean afterStart = start == null || !current.isBefore(start);
        boolean beforeEnd = end == null || !current.isAfter(end);

        return afterStart && beforeEnd;
    }

    public static LocalDateTime toUtc(LocalDateTime localDateTime, ZoneId sourceZone) {
        if (localDateTime == null) {
            return null;
        }

        ZoneId safeSourceZone = sourceZone == null ? ZONE_LIMA : sourceZone;

        return localDateTime
                .atZone(safeSourceZone)
                .withZoneSameInstant(ZONE_UTC)
                .toLocalDateTime();
    }

    public static LocalDateTime fromUtc(LocalDateTime utcDateTime, ZoneId targetZone) {
        if (utcDateTime == null) {
            return null;
        }

        ZoneId safeTargetZone = targetZone == null ? ZONE_LIMA : targetZone;

        return utcDateTime
                .atZone(ZONE_UTC)
                .withZoneSameInstant(safeTargetZone)
                .toLocalDateTime();
    }

    public static LocalDateTime max(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    public static LocalDateTime min(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isBefore(right) ? left : right;
    }
}