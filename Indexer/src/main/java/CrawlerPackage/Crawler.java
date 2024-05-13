package CrawlerPackage;

import java.util.*;

public class Crawler{
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
        //int i = 0;
        for (Thread thread : threads) {
            try {
                thread.join();
               // System.out.println(++i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

}