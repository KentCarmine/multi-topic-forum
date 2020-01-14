package com.kentcarmine.multitopicforum.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Helper class that handles encoding and decoding strings from and to URL-safe formats.
 */
public class URLEncoderDecoderHelper {

    public static String encode(String urlStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(urlStr, StandardCharsets.UTF_8.toString());
    }

    public static String decode(String urlStr) throws UnsupportedEncodingException {
        return URLDecoder.decode(urlStr, StandardCharsets.UTF_8.toString());
    }
}
