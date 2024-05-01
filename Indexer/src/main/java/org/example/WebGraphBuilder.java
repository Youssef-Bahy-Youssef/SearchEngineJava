package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WebGraphBuilder {
    // Map to store normalized URLs and their corresponding index in the graph
    public Map<String, Integer> urlIndexMap;
    // Adjacency matrix representation of the graph
    public int[][] adjacencyMatrix;
    // Size of the matrix
    private int matrixSize;

    public WebGraphBuilder() {
        urlIndexMap = new HashMap<>();
        matrixSize = 0;
    }

    public void buildGraphFromHTMLFiles(String directoryPath) {
        File folder = new File(directoryPath);
        File[] files = folder.listFiles();

        if (files == null) {
            System.err.println("Invalid directory path");
            return;
        }

        // Count unique URLs to determine matrix size
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".html")) {
                String filePath = file.getAbsolutePath();
                try {
                    Document doc = Jsoup.parse(file, "UTF-8");
                    String pageUrl = doc.text().split("\n")[0].split(" ")[1];
                    pageUrl=normalizeURL(pageUrl);
                    if(!pageUrl.isEmpty()) {
                        if(!urlIndexMap.containsKey(pageUrl))
                            urlIndexMap.put(pageUrl,matrixSize++);
                        Elements links = doc.select("a[href]");
                        for (Element link : links) {
                            String url = link.attr("abs:href");
                            if (!url.isEmpty()) {
                                String normalizedUrl = normalizeURL(url);
                                if (!normalizedUrl.isEmpty() && !urlIndexMap.containsKey(normalizedUrl)) {
                                    urlIndexMap.put(normalizedUrl, matrixSize++);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error parsing HTML file: " + filePath);
                }
            }
        }

        // Initialize adjacency matrix
        adjacencyMatrix = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            Arrays.fill(adjacencyMatrix[i],0);
        }
        // Populate adjacency matrix
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".html")) {
                String filePath = file.getAbsolutePath();
                try {
                    Document doc = Jsoup.parse(file, "UTF-8");
                    String fromPageUrl = doc.text().split("\n")[0].split(" ")[1];
                    fromPageUrl=normalizeURL(fromPageUrl);
                    if(!fromPageUrl.isEmpty()) {
                        int indexFrom = urlIndexMap.get(fromPageUrl);
                        //indexfrom will be colum
                        Elements links = doc.select("a[href]");
                        for (Element link : links) {
                            String url = link.attr("abs:href");
                            String normalizedUrl = normalizeURL(url);
                            if (!normalizedUrl.isEmpty()) {
                                int indexTo = urlIndexMap.get(normalizedUrl);
                                adjacencyMatrix[indexTo][indexFrom]=1;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error parsing HTML file: " + filePath);
                }
            }
        }

    }

    private String normalizeURL(String url) {
        return urlCleaner.start(url);
    }

    // Optionally, you can add a method to print the adjacency matrix
    public void printAdjacencyMatrix() {
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                System.out.print(adjacencyMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        WebGraphBuilder builder = new WebGraphBuilder();
        builder.buildGraphFromHTMLFiles("C:\\Users\\abdom\\Downloads\\Second Term-20240213T104511Z-001\\projects\\sample");
        builder.printAdjacencyMatrix();

    }
}
