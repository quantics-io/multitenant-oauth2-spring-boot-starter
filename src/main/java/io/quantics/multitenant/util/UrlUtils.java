package io.quantics.multitenant.util;

/**
 * Utility class for various url-related operations.
 */
public final class UrlUtils {

    public static String removeTrailingSlash(String url) {
        StringBuilder sb = new StringBuilder(url);
        int index = sb.length() - 1;
        if (sb.charAt(index) == '/') {
            sb.deleteCharAt(index);
        }
        return sb.toString();
    }

}
