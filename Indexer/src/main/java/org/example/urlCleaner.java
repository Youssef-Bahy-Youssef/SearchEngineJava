package org.example;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class urlCleaner {
    public static void main(String[] args){
        System.out.println(start("http://www.linkedin.com/company/"));
    }
    // Rule 1: Converting percent-encoded triplets to uppercase
    public static String normalizePercentEncoding(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String rawPath = uri.getRawPath();
        if (rawPath != null) {
            Pattern pattern = Pattern.compile("%[0-9a-fA-F]{2}");
            Matcher matcher = pattern.matcher(rawPath);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(result, matcher.group().toUpperCase());
            }
            matcher.appendTail(result);
            String decodedPath = uri.getPath();
            return url.replace(decodedPath, result.toString());
        }
        return url;
    }

    // Rule 2: Converting the scheme and host to lowercase
    public static String normalizeSchemeAndHost(String url) throws URISyntaxException {
        URI uri = new URI(url.toLowerCase());
        return uri.toString();
    }

    // Rule 3: Decoding percent-encoded triplets of unreserved characters
    public static String decodeUnreservedCharacters(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.normalize().toString();
    }

    // Rule 4: Removing dot-segments
    public static String removeDotSegments(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String path = uri.getPath();
        if (path != null) {
            String normalizedPath = path.replaceAll("/\\.(/|$)", "/");
            return url.replace(path, normalizedPath);
        }
        return url;
    }

    // Rule 5: Converting an empty path to a "/" path
    public static String convertEmptyPath(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            // Use capturing groups to capture the parts before and after the potential empty path
            return url.replaceFirst("(://[^/]+)(/?|$)", "$1/");
        }
        return url;
    }


    // Rule 6: Removing the default port
    public static String removeDefaultPort(String url) throws URISyntaxException {
        URI uri = new URI(url);
        if (url.startsWith("http://") ) {
            url = url.substring(7);
        } else if (url.startsWith("https://") ) {
            url= url.substring(8);
        }
        int port = uri.getPort();
        if (port == 80 || port == -1) {
            return url.replaceFirst(":[0-9]+/", "/");
        }

        return url;
    }
    public static String slashAndWww(String url) throws URISyntaxException {
        if (url.endsWith("/")) {
            url= url.substring(0, url.length() - 1);
        }
        if (url.contains("www.")) {
            return url.replaceFirst("www\\.", "");
        }
        return url;
    }
    public static String removeQueryParams(String urlString) {
        try {
            int index=urlString.indexOf("?");
            if(index!=-1){
                urlString=urlString.substring(0,index);
            }
            URI uri = new URI(urlString);
            URI cleanUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, null);
            return cleanUri.toString();
        } catch (URISyntaxException e) {
            // Handle URISyntaxException
            if(!urlString.contains("javascript:void(0)")){
                System.out.println(urlString);
                e.printStackTrace();
            }

            return ""; // Return original URL in case of exception
        }
    }
    public static String start(String url ) {
        try {
            url=removeQueryParams(url);
            url=normalizePercentEncoding(url);
            url=normalizeSchemeAndHost(url);
            url=decodeUnreservedCharacters(url);
            url=removeDefaultPort(url);
            url=removeDotSegments(url);
            url=convertEmptyPath(url);
            url=slashAndWww(url);
            //System.out.println("Final url: " + url);
            return url;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.out.println("ERROR in manipulating the URL");
            return "";
        }
    }
}

