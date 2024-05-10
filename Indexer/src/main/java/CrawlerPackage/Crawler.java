package CrawlerPackage;

import java.util.Scanner;

public class Crawler{

    public static void main(String[] args) {
        System.out.println("Enter the Number of Threads.");
        Scanner scanner = new Scanner(System.in);
        int nThreads = scanner.nextInt();
        WebCrawler.StartUP();
        for (int i = 0; i < nThreads; i++) {
            new Thread(new WebCrawler()).start();
        }
    }
}