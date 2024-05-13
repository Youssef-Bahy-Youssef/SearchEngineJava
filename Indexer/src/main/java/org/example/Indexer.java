package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;
public class Indexer {
    private Map<String, HashMap<String, IndexerObj>> invertedIndex;
    private Map<String, Map<String, ArrayList<Integer>>> wordIndex;
    private Map<String, ArrayList<String>> documents;
    private Map<String, Boolean>urls;
    private Map<String, Boolean> stopWords;
    private Map<String, Integer> weightedIndices;
    private Map<String, Map<String, Float>> TFIDF;
    private Map<String, Map<String, Integer>> TF;
    private Map<String, Integer> DF;
    private Map<String, Boolean> words;
    private Map<String, Integer> docLength;
    //    private MongoDB mongoDB;
    private Stemmer stemmer;
    private Integer docCount;
    private long lastModified;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int INTERVAL_HOURS = 10; // Interval in hours

    private static final Map<String, Integer> tagWeights = new HashMap<>();

    static {
        tagWeights.put("title", 10);
        tagWeights.put("h1", 10);
        tagWeights.put("h2", 9);
        tagWeights.put("h3", 8);
        tagWeights.put("h4", 7);
        tagWeights.put("h5", 6);
        tagWeights.put("h6", 5);
        tagWeights.put("p", 3);
        tagWeights.put("b", 4);
        tagWeights.put("em", 4);
        tagWeights.put("i", 4);
        tagWeights.put("strong", 4);
        tagWeights.put("span", 2);
        tagWeights.put("a", 2);

    }

    public Indexer(File folder) throws Exception {
        invertedIndex = new HashMap<>();
        wordIndex = new HashMap<>();
        documents = new HashMap<>();
        urls = new HashMap<>();
        stopWords = new HashMap<>();
        stemmer = new Stemmer();
        DF = new HashMap<>();
        TF = new HashMap<>();
        TFIDF = new HashMap<>();
        docLength = new HashMap<>();
        words = new HashMap<>();
        docCount = 0;

//        MongoClientConnect.start();
        //calculate pagerank
        PageRank.start();
        //PageRank.pageRankMap[]
        for(String key:PageRank.pageRankMap.keySet()){
            System.out.println("URL:"+key);
            System.out.println("\t\tRank:"+PageRank.pageRankMap.get(key));
        }
        readStopWords();

        getDocuments(folder);
        stemming();
        calcTFIDF();
        createIndex();
        createSearchIndex();
        createWordIndex();
        printDocuments();
        scheduleTask();
    }
    private boolean calculatePhraseDFS(int lastIndex, int wordCount, String url, ArrayList<String> words) {
        if (words == null || words.isEmpty() || wordCount == words.size()) {
            return true;
        }
        boolean result = true;
        Map<String, ArrayList<Integer>> indexes = new HashMap<>();
        indexes = wordIndex.get(words.get(wordCount));
        ArrayList<Integer> positions = indexes.get(url);
        if (positions != null) {
            for (int pos : positions) {
                if (lastIndex == -1 || lastIndex == pos - 1) {
                    result |= calculatePhraseDFS(pos, wordCount + 1, url, words);
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }

        return result;
    }
    private void phrase(String query) {
        ArrayList<String> words = new ArrayList<>();
        splitWords(query, words);
        ArrayList<String> pages = new ArrayList<>();
        urls.forEach((url, ok)-> {
            if(ok) {
                Boolean ret = calculatePhraseDFS(-1, 0, url, words);
                System.out.println(ret);
                if(ret) {
                    pages.add(url);
                }
            }
        });
    }
    private void readStopWords() throws IOException {
        Scanner scanner = new Scanner(new File("/Users/youssefbahy/Education/Search Engine/Saturday/SearchEngineJava/Indexer/src/main/resources/stopwords.txt"));
        while (scanner.hasNext()) {
            stopWords.put(scanner.next(), Boolean.TRUE);
        }
    }
    private void getDocuments(File folder) {
        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            String filePath = fileEntry.getPath();
            lastModified = max(lastModified, fileEntry.lastModified());
            StringBuilder html = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String url = br.readLine();
                url = url.split(" ")[1];
                url = urlCleaner.start(url);
                urls.put(url, Boolean.TRUE);
                String line;
                while ((line = br.readLine()) != null) {
                    html.append(line).append("\n");
                }
                //url:stackoverflow.com Rank:1846697.2658817559
                //URL:meta.stackoverflow.com
                //Rank:1846697.2658817559

                String parsedText = html2text(html.toString());
                ArrayList<String> list = new ArrayList<>();
                tokenize(parsedText, list);
//                -------------------------------------------------------------------------------
//                List<String> originalWordsList = List.of(parsedText.split(" "));
//                ArrayList<String>originalWords = new ArrayList<>();
//                originalWords.addAll(originalWordsList);
//                -------------------------------------------------------------------------------
//                for(String word: list) {
//                    wordIndex.p
//
//                }
                ArrayList<String> originalWords = new ArrayList<>();
                splitWords(parsedText, originalWords);
                int i=0;
                for(String word:originalWords) {
                    i++;
                   wordIndex.putIfAbsent(word, new HashMap<>());
                    Map<String, ArrayList<Integer>>page = wordIndex.get(word);
                    page.putIfAbsent(url, new ArrayList<>());
                    ArrayList<Integer> item = page.get(url);
                    item.add(i);
                }
                removeStopWords(list);
                documents.put(url, list);
                docCount++;
                calcWeight(html.toString(), url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        urls.forEach((url, ok) -> {
            phrase("Find centralized, trusted content and collaborate around the");
        });
    }
//    private void splitWords(String str, ArrayList<String> list) {
//        String[] words = str.split(" ");
//        for (String word : words) {
//            list.add(word);
//        }
//    }
private void splitWords(String document, ArrayList<String> tokens) {
//    document = document.toLowerCase(Locale.ROOT);
    StringTokenizer st = new StringTokenizer(document);
    while (st.hasMoreTokens()) {
        List<String> l = List.of(st.nextToken().split(" "));
        tokens.addAll(l);
    }
}
    private void tokenize(String document, ArrayList<String> tokens) {
        document = document.toLowerCase(Locale.ROOT);
        StringTokenizer st = new StringTokenizer(document);
        while (st.hasMoreTokens()) {
            List<String> l = List.of(st.nextToken().split("\\W+"));
            tokens.addAll(l);
        }
    }
    private static String html2text(String html) {
        return Jsoup.parse(html).text();
    }
    private void removeStopWords(ArrayList<String> list) {
        list.removeIf(stopWords::containsKey);
    }
    private void calcWeight(String htmlContent, String url) {
        weightedIndices = new HashMap<>();
        Document doc = Jsoup.parse(htmlContent);
        for (String tagName : tagWeights.keySet()) {
            Elements elements = doc.select(tagName);
            for (Element element : elements) {
                String text = element.text().toLowerCase(Locale.ROOT);
                List<String> words = List.of(text.split("\\W+"));
                ArrayList<String>arr = new ArrayList<>();
                arr.addAll(words);
                removeStopWords(arr);
                for (int i = 0; i < arr.size(); i++) {
                    String word = arr.get(i);
                    word = stemmer.stemWord(word);
                    arr.set(i, word);
                }
                for (String word : arr) {
                    weightedIndices.merge(word, tagWeights.get(tagName), Integer::sum);
                }
            }
        }
        weightedIndices.forEach((word, weight) -> {
            invertedIndex.putIfAbsent(word, new HashMap<>());
            Map<String, IndexerObj>page = invertedIndex.get(word);
            page.putIfAbsent(url, new IndexerObj());
            IndexerObj item = page.get(url);
            item.url = url;
            item.weight = weight;
        });
    }
    private void stemming() {
        documents.forEach((url, doc) -> {
            for (int i = 0; i < doc.size(); i++) {
                String originalWord = doc.get(i);
                String word = stemmer.stemWord(originalWord);
                doc.set(i, word);
                invertedIndex.putIfAbsent(word, new HashMap<>());
                HashMap<String, IndexerObj>page = invertedIndex.get(word);
                page.putIfAbsent(url, new IndexerObj());
                IndexerObj item = page.get(url);
                item.positions.put(i, originalWord);
            }
        });
    }
    private void initMaps() {
        for (Map.Entry<String, ArrayList<String>> entry : documents.entrySet()) {
            String url = entry.getKey();
            ArrayList<String> doc = entry.getValue();
            for (String word : doc) {
                DF.putIfAbsent(word, 0);
                TF.putIfAbsent(word, new HashMap<>());
                TF.get(word).putIfAbsent(url, 0);
                TFIDF.putIfAbsent(word, new HashMap<>());
                TFIDF.get(word).putIfAbsent(url, (float) (0));
                words.putIfAbsent(word, Boolean.TRUE);
            }
        }
    }
    private void calcDF() {
        for (Map.Entry<String, ArrayList<String>> entry : documents.entrySet()) {
            String url = entry.getKey();
            ArrayList<String> doc = entry.getValue();
            Map<String, Boolean>isVisited = new HashMap<>();
            for (String word : doc) {
                if(isVisited.containsKey(word)) {
                    continue;
                }else {
                    DF.put(word, DF.get(word) + 1);
                }
                isVisited.put(word, Boolean.TRUE);
            }
        }
    }
    private void calcTF() {
        for (Map.Entry<String, ArrayList<String>> entry : documents.entrySet()) {
            String url = entry.getKey();
            ArrayList<String> doc = entry.getValue();
            docLength.put(url, doc.size());
            for (String word : doc) {
                TF.get(word).put(url, TF.get(word).get(url) + 1);
            }
        }
    }
    private void calcTFIDF() {
        initMaps();
        calcDF();
        calcTF();
        for (Map.Entry<String, ArrayList<String>> entry : documents.entrySet()) {
            String url = entry.getKey();
            ArrayList<String> doc = entry.getValue();
            for (String word : doc) {
                float val = TF.get(word).get(url) * (1/(float)docLength.get(url)) * (float) log(docCount/(float)DF.get(word));
                TFIDF.get(word).put(url, val);
            }
        }
    }
    private void createIndex() {
        for (Map.Entry<String, ArrayList<String>> entry : documents.entrySet()) {
            String url = entry.getKey();
            ArrayList<String> doc = entry.getValue();
            int wordPos=0;
            for (String word : doc) {
                wordPos++;
                invertedIndex.putIfAbsent(word, new HashMap<>());
                HashMap<String, IndexerObj>page = invertedIndex.get(word);
                page.putIfAbsent(url, new IndexerObj());
                IndexerObj item = page.get(url);
                item.url = url;
                item.rank =PageRank.pageRankMap.get(url);
                item.TFIDF = (double) TFIDF.get(word).get(url);
//                item.positions.add(wordPos);
            }
        }
    }
    //    private Map<String, HashMap<String, IndexerObj>> invertedIndex;
    private void createSearchIndex() {
        invertedIndex.forEach((word, documents) -> {
            ArrayList<IndexerObj>pages = new ArrayList<>();
            documents.forEach((url, obj) -> {
                pages.add(obj);
            });
            MongoClientConnect.InsertWord(word, pages);
        });
    }
    private void createWordIndex() {
        wordIndex.forEach(MongoClientConnect::InsertWordIndex);
    }
    private void printDocuments() {
        System.out.print(String.format("%10s", " "));
        words.forEach((word, ok) -> {
            System.out.print(String.format("%10s", word));
        });
        System.out.println("");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------");
        for (Map.Entry<String, ArrayList<String>> entry : documents.entrySet()) {
            String url = entry.getKey();
            ArrayList<String> doc = entry.getValue();
            System.out.print(String.format("%10s", "document"));
            words.forEach((word, ok) -> {
                System.out.print(String.format("%10s", (TF.get(word).get(url))).toString());
            });
            System.out.println("");
        }
    }
    private   boolean hasFilesModifiedAfterLastReindexing(File folder) {
        System.out.println("Last Modified In Files");
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                System.out.print(fileEntry.lastModified());
                System.out.print(" ");
                if (fileEntry.lastModified() >  lastModified) {
                    lastModified = fileEntry.lastModified();
                    return true; // File has been modified since the last reindexing
                }
            }
        }
        System.out.println("");
        System.out.println(lastModified);
        return false; // No files have been modified since the last reindexing
    }
    public void checkReIndexing() throws Exception {
        File folder = new File("/Users/youssefbahy/Education/Search Engine/Saturday/SearchEngineJava/Indexer/src/main/resources/samole");
        Boolean ok = hasFilesModifiedAfterLastReindexing(folder);
        if(ok) {
            Indexer indexer = new Indexer(folder);
        }
    }
    public void scheduleTask() throws Exception {
        // Run the task initially and then schedule it to run periodically every x hours
        Runnable task = () -> {
            try {
                this.checkReIndexing();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(task, 10000, INTERVAL_HOURS, TimeUnit.HOURS);
    }
    public static void main(String[] args) throws Exception {
        File folder = new File("/Users/youssefbahy/Education/Search Engine/Saturday/SearchEngineJava/Indexer/src/main/resources/sample");
        Indexer indexer = new Indexer(folder);
    }
}