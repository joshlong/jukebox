package com.joshlong.jukebox2.services.impl.util;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Bytes {
    private static final Pattern valuePattern = Pattern.compile("([0-9]+([\\\\.,][0-9]+)?)\\\\s*(|K|M|G|T)B?", Pattern.CASE_INSENSITIVE);
    public static Bytes MAX = bytes(Long.MAX_VALUE);
    protected long value;

    private Bytes(final long bytes) {
        this.value = bytes;
    }

    public static Bytes bytes(final long bytes) {
        return new Bytes(bytes);
    }

    public static Bytes kilobytes(final long kilobytes) {
        return bytes(kilobytes * 1024);
    }

    public static Bytes megabytes(final long megabytes) {
        return kilobytes(megabytes * 1024);
    }

    public static Bytes gigabytes(final long gigabytes) {
        return megabytes(gigabytes * 1024);
    }

    public static Bytes terabytes(final long terabytes) {
        return gigabytes(terabytes * 1024);
    }

    public static Bytes bytes(final double bytes) {
        return bytes(Math.round(bytes));
    }

    public static Bytes kilobytes(final double kilobytes) {
        return bytes(kilobytes * 1024.0);
    }

    public static Bytes megabytes(final double megabytes) {
        return kilobytes(megabytes * 1024.0);
    }

    public static Bytes gigabytes(final double gigabytes) {
        return megabytes(gigabytes * 1024.0);
    }

    public static Bytes terabytes(final double terabytes) {
        return gigabytes(terabytes * 1024.0);
    }

    public final long bytes() {
        return value;
    }

    public final double kilobytes() {
        return value / 1024.0;
    }

    public final double megabytes() {
        return kilobytes() / 1024.0;
    }

    public final double gigabytes() {
        return megabytes() / 1024.0;
    }

    public final double terabytes() {
        return gigabytes() / 1024.0;
    }

    public static Bytes valueOf(final String string, final Locale locale) {
        final Matcher matcher = valuePattern.matcher(string);

        if (matcher.matches()) {
            try {
                final double value = NumberFormat.getNumberInstance(locale).parse(matcher.group(1)).doubleValue();

                final String units = matcher.group(3);

                if (units.equalsIgnoreCase("")) {
                    return bytes(value);
                } else if (units.equalsIgnoreCase("K")) {
                    return kilobytes(value);
                } else if (units.equalsIgnoreCase("M")) {
                    return megabytes(value);
                } else if (units.equalsIgnoreCase("G")) {
                    return gigabytes(value);
                } else if (units.equalsIgnoreCase("T")) {
                    return terabytes(value);
                } else {
                    throw new RuntimeException("Units not recognized: " + string);
                }
            } catch (ParseException e) {
                throw new RuntimeException("Unable to parse numeric part: " + string, e);
            }
        } else {
            throw new RuntimeException("Unable to parse bytes: " + string);
        }
    }

    public static Bytes valueOf(final String string) {
        return valueOf(string, Locale.getDefault());
    }
}
