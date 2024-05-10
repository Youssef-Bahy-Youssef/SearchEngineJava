package CrawlerPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class to retrieve and parse the disallow list from a website's robots.txt file.
 */
public class RobotChecker {

    /**
     * Retrieves the disallow list from a website's robots.txt file.
     *
     * @param websiteUrl The URL of the website.
     * @return A set of disallowed URLs.
     */
    public static Set<String> getDisallowList(String websiteUrl) {
        Set<String> disallowList = new HashSet<>();
        try {
            // Open a connection to the robots.txt file
            URL robotsUrl = new URL(websiteUrl + "/robots.txt");
            URLConnection connection = robotsUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Read the robots.txt file line by line
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Parse the robots.txt file and extract disallowed URLs
            parseRobotsTxt(reader, websiteUrl, disallowList);

            reader.close();
        } catch (IOException e) {

        }

        return disallowList;
    }

    /**
     * Parses the contents of a robots.txt file and extracts disallowed URLs.
     *
     * @param reader       The BufferedReader for reading the robots.txt file.
     * @param websiteUrl   The URL of the website.
     * @param disallowList The set to store the disallowed URLs.
     * @throws IOException If an I/O error occurs.
     */
    private static void parseRobotsTxt(BufferedReader reader, String websiteUrl, Set<String> disallowList) throws IOException {
        String line;
        boolean userAgentMatched = false;
        while ((line = reader.readLine()) != null) {
            // Check if the line specifies a user-agent
            if (line.toLowerCase().startsWith("user-agent:")) {
                userAgentMatched = checkUserAgent(line);
            } else if (userAgentMatched && line.toLowerCase().startsWith("disallow:")) {
                // If the user-agent matches and a disallow rule is found, extract the disallowed URL
                String disallowUrl = extractDisallowUrl(line, websiteUrl);
                disallowList.add(disallowUrl);
            }
        }
    }

    /**
     * Checks if a line in the robots.txt file specifies a user-agent.
     *
     * @param line The line to check.
     * @return True if the line contains a user-agent wildcard, false otherwise.
     */
    private static boolean checkUserAgent(String line) {
        return line.toLowerCase().contains("*");
    }

    /**
     * Extracts the disallowed URL from a line in the robots.txt file.
     *
     * @param line       The line containing the disallow rule.
     * @param websiteUrl The URL of the website.
     * @return The complete disallowed URL.
     */
    private static String extractDisallowUrl(String line, String websiteUrl) {
        return websiteUrl + line.substring("disallow:".length()).trim();
    }

    /**
     * Main method to test the RobotChecker utility.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Set<String> disallowList = RobotChecker.getDisallowList("https://geeksforgeeks.org");
        System.out.println("Disallowed URLs:");
        for (String url : disallowList) {
            System.out.println(url);
            System.out.println("******************************************************************************");
        }
    }
}
