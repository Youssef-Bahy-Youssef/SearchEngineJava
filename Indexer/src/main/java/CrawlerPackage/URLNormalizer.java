package CrawlerPackage;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

public class URLNormalizer {

    /**
     * Normalizes a tainted URL string according to specified rules.
     *
     * @param taintedURL The URL string to be normalized.
     * @return The normalized URL string.
     * @throws MalformedURLException If the provided URL is malformed.
     */
    public static String normalize(final String taintedURL) {
            // Convert the URL to lowercase and normalize the path
        URI uri = null;
        try {
            uri = new URI(taintedURL.toLowerCase()).normalize();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        URL url = null;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String path = url.getPath().replace("/$", ""); // Remove trailing slash
        // Create parameter map from query string
        SortedMap<String, String> params = createParameterMap(url.getQuery());
        int port = url.getPort();
        String queryString = params != null ? "?" + canonicalize(params) : ""; // Construct query string
        // Construct and return the normalized URL
        return url.getProtocol() + "://" + url.getHost() + (port != -1 && port != 80 ? ":" + port : "") + path + queryString;
    }

    /**
     * Parses the query string and stores the parameter name-value pairs in a sorted map.
     *
     * @param queryString The query string to be parsed.
     * @return A sorted map containing parameter name-value pairs.
     */
    private static SortedMap<String, String> createParameterMap(final String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return null;
        }

        // Split query string into individual parameter name-value pairs
        String[] pairs = queryString.split("&");
        // Create a map to store parameter name-value pairs
        SortedMap<String, String> params = new TreeMap<>();
        for (String pair : pairs) {
            if (pair.length() < 1) {
                continue;
            }
            String[] tokens = pair.split("=", 2);
            for (int j = 0; j < tokens.length; j++) {
                try {
                    tokens[j] = URLDecoder.decode(tokens[j], "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
            }
            // Add parameter name-value pair to the map
            if (tokens.length == 1) {
                params.put(tokens[0], "");
            } else {
                params.put(tokens[0], tokens[1]);
            }
        }

        return params;
    }

    /**
     * Creates the canonical form of the query string by encoding parameter names and values.
     *
     * @param sortedParamMap A sorted map containing parameter name-value pairs.
     * @return The canonical form of the query string.
     */
    private static String canonicalize(final SortedMap<String, String> sortedParamMap) {
        if (sortedParamMap == null || sortedParamMap.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(350);
        // Iterate over parameter name-value pairs and construct the canonical query string
        for (var entry : sortedParamMap.entrySet()) {
            sb.append(percentEncodeRfc3986(entry.getKey()));
            sb.append('=');
            sb.append(percentEncodeRfc3986(entry.getValue()));
            sb.append('&');
        }
        // Remove the trailing '&' character
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Percent-encodes a string according to RFC 3986.
     *
     * @param string The string to be encoded.
     * @return The percent-encoded string.
     */
    private static String percentEncodeRfc3986(final String string) {
        try {
            // Percent-encode the string and replace special characters
            return URLEncoder.encode(string, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            // Return the original string if encoding fails
            return string;
        }
    }


    public static void main(String[] args) {
        // Test the URL normalizer
        List<String> urls = Arrays.asList(
                "https://www.example.com/page1/index.html?query=search&category=electronics#section",
                "https://subdomain.example.com:8080/path/to/page.html",
                "http://user:password@example.com/resource",
                "https://www.example.com/path/to/page.html?query=example&key=value#section",
                "ftp://ftp.example.com/pub/file.tar.gz",
                "https://www.example.com/path/to/../page.html",
                "https://www.example.com/%7Eusername/path/../index.html",
                "https://www.example.com:8080/path/../index.html",
                "HTTP://www.Example.com:80/path/to/file.html?key=value&foo=bar#fragment"
        );

        for (String urlString : urls) {
            String normalizedUrl = URLNormalizer.normalize(urlString);
            System.out.println("Original URL: " + urlString);
            System.out.println("Normalized URL: " + normalizedUrl);
            System.out.println();
        }
    }
}
