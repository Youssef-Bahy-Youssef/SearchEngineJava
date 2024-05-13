package CrawlerPackage;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.tika.language.LanguageIdentifier;


import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements Runnable {

    // Concurrent collections for storing data
    private static Set<String> visitedCompactStrings = ConcurrentHashMap.newKeySet(); // Set for storing compact strings of visited pages
    private static Queue<String> frontier = new ConcurrentLinkedQueue<>(); // Queue for frontier URLs
    private static Map<String, Boolean> visitedLinks = new ConcurrentHashMap<>(); // Map for storing visited links
    private static Queue<String> compactStrings = new ConcurrentLinkedQueue<>(); // Queue for storing compact strings of URLs
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Constants and variables
    public static int MAX_CRAWLED_PAPERS = 1000;
    private static AtomicInteger crawledPaperCount = new AtomicInteger(0); // Atomic counter for crawled papers
    private static final String FILE_PATH = "src/main/java/CrawlerPackage/";
    private static final String VISITED_FILE = FILE_PATH + "visited.txt";
    private static final String FRONTIER_FILE = FILE_PATH + "frontier.txt";
    private static final String COMPACT_STRINGS_FILE = FILE_PATH + "compact_strings.txt";
    private static final String SEED_FILE = FILE_PATH + "seed.txt";
    private static final Object fileLock = new Object(); // Lock for file operations
    @Override
    public void run() {
        while (crawledPaperCount.get() < MAX_CRAWLED_PAPERS) {
            String url = frontier.poll();
            if (url != null) {
                //System.out.println("Current Count: " + crawledPaperCount.get());
                crawl(url);
            }
            else {
                // If frontier is empty, sleep for a while to reduce CPU usage
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Start the web crawler.
     */
    public static void StartUP() {
        loadVisited();
        loadFrontier();
        loadCompactStrings();
        loadFromSeedFile(SEED_FILE); // Load URLs from seed file
        crawledPaperCount.set(visitedLinks.size());
        scheduleStateSaving(); // Schedule task for saving state
        addShutdownHook(); // Add shutdown hook to save state before exiting
    }

    /**
     * Crawl a given URL.
     *
     * @param url The URL to crawl.
     */
    public void crawl(String url) {
        Document doc = request(url); // Fetch the HTML document
        if (doc != null && isEnglish(doc)) {
            String compactContent = generateCompactString(doc); // Generate compact string of page content

            // Check if compact string exists in the set of visited pages
            if (!visitedCompactStrings.contains(compactContent)) {
                visitedCompactStrings.add(compactContent); // Add compact string to visited set
                processLinks(doc, url); // Process links on the page
                url = URLNormalizer.normalize(url);
                writeUrlToFile(url); // Append URL to text file
                compactStrings.add(compactContent); // Save compact string for URL
                saveAsHtmlWithUrl(doc, url); // Save document content as HTML with URL
                visitedLinks.put(url, true); // Mark link as visited
                crawledPaperCount.incrementAndGet(); // Increment the crawled paper count
            }
        }
    }

    /**
     * Load URLs from the seed file.
     *
     * @param seedFilePath The path to the seed file.
     */
    private static void loadFromSeedFile(String seedFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(seedFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String url = line.trim();
                if (!visitedLinks.containsKey(url)) { // Check if URL is not already visited
                    frontier.add(url); // Add URL to frontier
                    visitedLinks.put(url, true); // Mark URL as visited
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process links on a page and add allowed links to the frontier.
     *
     * @param doc The document object representing the page.
     * @param url The URL of the page.
     */
    private void processLinks(Document doc, String url) {
        Elements links = doc.select("a[href]"); // Get all links on the page

        Set<String> disallowedLinks = RobotChecker.getDisallowList(url); // Get the disallowed links

        // Filter out disallowed links
        Set<String> allowedLinks = new HashSet<>();
        for (Element link : links) {
            String absUrl = link.absUrl("href"); // Get the absolute URL
            if (!disallowedLinks.contains(absUrl)) { // Check if the link is allowed
                allowedLinks.add(absUrl);
            }
        }

        // Add allowed links to the frontier
        for (String link : allowedLinks) {
            if (!visitedLinks.containsKey(link)) { // Check if link is not already visited
                frontier.add(link); // Add link to frontier
            }
        }
    }

    /**
     * Determines if the content of a given document appears to be in English.
     *
     * @param doc The document to analyze for language detection.
     * @return {@code true} if the detected language is reasonably certain to be English, {@code false} otherwise.
     */
    private static boolean isEnglish(Document doc) {
        // Your language detection logic goes here
        // For demonstration purposes, assuming language detection is done externally
        // Create a language identifier object
        LanguageIdentifier identifier = new LanguageIdentifier(doc.text());

        // Detect the language
        String language = identifier.getLanguage();

        // Check if the detected language is English
        if (language.equals("en")) {
            return true;
        } else {
            return false;
        }

    }



    /**
     * Write a URL to the visited file.
     *
     * @param url The URL to write.
     */
    private void writeUrlToFile(String url) {
        synchronized (fileLock) {
            try (BufferedWriter urlWriter = new BufferedWriter(new FileWriter(VISITED_FILE, true))) {
                urlWriter.write(url + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load visited URLs from the visited file.
     */
    private static void loadVisited() {
        try (BufferedReader reader = new BufferedReader(new FileReader(VISITED_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                visitedLinks.put(line.trim(), true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load frontier URLs from the frontier file.
     */
    private static void loadFrontier() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FRONTIER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                frontier.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load compact strings from the compact strings file.
     */
    private static void loadCompactStrings() {

        try (BufferedReader reader = new BufferedReader(new FileReader(COMPACT_STRINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                compactStrings.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch the HTML document from a URL.
     *
     * @param url The URL to fetch.
     * @return The fetched HTML document.
     */
    private static Document request(String url) {
        Document doc = null;
        Connection conn = null;
        try {
            conn = Jsoup.connect(url);
            doc = conn.get();
            if (conn.response().statusCode() == 200) {
                System.out.println("Received Webpage at " + url);
            }
        } catch (IOException e) {
            System.out.println(conn.response().statusCode());
        }
        return doc;
    }

    /**
     * Save the document content as HTML with the URL.
     *
     * @param doc The document object.
     * @param url The URL.
     */
    private static void saveAsHtmlWithUrl(Document doc, String url) {
        try {
            String websiteName = getWebsiteName(url);
            String pageTitle = doc.title();
            String directoryName = "Htmls";
            File directory = new File(directoryName);
            if (!directory.exists()) {
                directory.mkdir();
            }
            websiteName = websiteName.replaceAll("[^a-zA-Z0-9.-]", "_");
            pageTitle = pageTitle.replaceAll("[^a-zA-Z0-9.-]", "_");
            String fileName = directoryName + File.separator + websiteName + "_" + pageTitle + ".html";
            fileName = fileName.replaceAll("__","");

            doc.select("script").remove(); // Remove all script elements
            doc.select("style").remove(); // Remove all style elements
            doc.select("*").removeAttr("<!--"); // Remove all commented elements

            synchronized (fileLock) {
                try (FileWriter writer = new FileWriter(fileName)) {
                    writer.write("URL: " + url + "\n");
                    writer.write(doc.outerHtml());
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a compact string of the page content using SHA-256 hashing.
     *
     * @param doc The document object representing the page.
     * @return The compact string.
     */
    private static String generateCompactString(Document doc) {
        StringBuilder compactContent = new StringBuilder();
        String text = doc.text();
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                compactContent.append(word.charAt(0));
            }
        }

        String compactString = compactContent.toString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(compactString.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the website name from a URL.
     *
     * @param url The URL.
     * @return The website name.
     * @throws URISyntaxException
     */
    private static String getWebsiteName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    /**
     * Save visited URLs to the visited file.
     */
    private static void saveVisited() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VISITED_FILE))) {
            for (String url : visitedLinks.keySet()) {
                writer.write(url + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save frontier URLs to the frontier file.
     */
    private static void saveFrontier() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FRONTIER_FILE))) {
            for (String url : frontier) {
                writer.write(url + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save compact strings to the compact strings file.
     */
    private static void saveCompactStrings() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COMPACT_STRINGS_FILE))) {
            for (String compactString : compactStrings) {
                writer.write(compactString + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Schedule the state-saving task to run every 10 minutes.
     */
    private static void scheduleStateSaving() {
        scheduler.scheduleAtFixedRate(() -> {
            saveVisited();
            saveFrontier();
            saveCompactStrings();
        }, 0, 5, TimeUnit.MINUTES);
    }

    /**
     * Add a shutdown hook to save the state before exiting.
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveVisited();
            saveFrontier();
            saveCompactStrings();
        }));
    }

    /**
     * Main method to start the web crawler.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        System.out.println("Enter the Number of Threads:");
        Scanner scanner = new Scanner(System.in);
        int nThreads = scanner.nextInt();
        System.out.println("Enter the Number of Pages:");
        int nPages = scanner.nextInt();
        WebCrawler.MAX_CRAWLED_PAPERS = nPages;
        WebCrawler.StartUP(); // Start the web crawler
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            Thread thread = new Thread(new WebCrawler());
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}
