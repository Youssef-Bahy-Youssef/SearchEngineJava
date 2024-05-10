package com.example.SearchEngine.Controller;

import com.example.SearchEngine.Model.Page;
import com.example.SearchEngine.Model.invertedIndex;
import com.example.SearchEngine.Repository.InvertedFileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import opennlp.tools.stemmer.PorterStemmer;

import java.util.*;

import org.bson.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;

@RestController
public class MainController {
    @Autowired
    InvertedFileRepo invertedFileRepo;
    @PostMapping("/add")
    public  void addStudent(@RequestBody invertedIndex invFile){
        invertedFileRepo.save(invFile);
    }

   @GetMapping("/search")
   public List<String> start( @RequestParam String query){
       List<Page> pages= processQuery(query);
       Map<String, Double> unsortedMap = new HashMap<>();

       for(Page p:pages){
           double prev=0;
           if(unsortedMap.get(p.url)!=null){
               prev=unsortedMap.get(p.url);
           }
           unsortedMap.put(p.url, prev+p.tf_idf*.6+p.rank*.4);
       }
       List<Map.Entry<String, Double>> list = new LinkedList<>(unsortedMap.entrySet());

       // Sort the list by values
       Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
           public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
               return (o1.getValue()).compareTo(o2.getValue());
           }
       });
       // Put sorted entries back into the map
       Map<String, Double> sortedByValueMap = new LinkedHashMap<>();

       for (Map.Entry<String, Double> entry : list) {
           sortedByValueMap.put(entry.getKey(), entry.getValue());
       }

       List<String> results=Arrays.asList(new String[unsortedMap.size()]);;
       int i= unsortedMap.size()-1;
       int j=0;
       // Print the sorted map by values
       for (Map.Entry<String, Double> entry : sortedByValueMap.entrySet()) {
           System.out.println(entry.getKey() + ": " + entry.getValue());
           results.set(i, entry.getKey());
           i--;
           j++;
           if(j==200){
               break;
           }
       }
       i=0;
       for (;i<results.size();i++){
           System.out.println(results.get(i));
       }
       return results;

   }
    @GetMapping("/example")
    public String example(
            @RequestParam String param1,
            @RequestParam(required = false) String param2) {
        // Access the query parameters 'param1' and 'param2'
        return "Value of param1: " + param1 + ", Value of param2: " + param2;
    }
    public  invertedIndex getDocumentsByWord(String word){
        List<invertedIndex> result= invertedFileRepo.findByWord(word);
        if(result.isEmpty()){
            return  null;
        }else {
            return result.getFirst();
        }
    }
    private PorterStemmer stemmer;

    public List<Page> processQuery(String query) {
        this.stemmer = new PorterStemmer();
        // Check if the query is a phrase search
        if (isPhraseSearch(query)) {
//            return searchIndex(query); // Perform phrase search
            System.out.println("Ali task");
            return null;
        } else {
            // Preprocess regular search query and search index
            List<String> processedQuery = preprocessQuery(query);
            return searchIndex(processedQuery);
        }
    }

    /**
     * Preprocesses the search query by splitting into individual words and applying stemming.
     *
     * @param query The search query to preprocess
     * @return List of preprocessed query words
     */
    private List<String> preprocessQuery(String query) {
        // Split the query into individual words
        String[] words = query.split("\\s+");
        List<String> processedWords = new ArrayList<>();

        // Apply stemming to each word using the Porter stemmer
        for (String word : words) {
            // Apply stemming algorithm
            String stemmedWord = stem(word);
            processedWords.add(stemmedWord);
        }

        return processedWords;
    }

    /**
     * Searches the index for relevant documents based on the processed query.
     *
     * @param processedQuery List of preprocessed query words
     * @return List of relevant documents
     */
    private List<Page> searchIndex(List<String> processedQuery) {
        List<Page> relevantPages = new ArrayList<>();

        for (String word : processedQuery) {
            // Retrieve documents associated with each word from the Indexer
            invertedIndex invertedIndexs = getDocumentsByWord(word);
            if (invertedIndexs != null) {
                List<Page> pages = invertedIndexs.getPages();
                if (pages != null) {

                    relevantPages.addAll(pages);
                }
            }
        }

        return relevantPages;
    }

    /**
     * Searches the index for documents containing the exact phrase.
     *
     * @param query The phrase search query
     * @return List of relevant documents
     */
    // after ali part

    /**
     * Stemming method using Porter stemmer.
     *
     * @param word The word to stem
     * @return The stemmed word
     */
    private String stem(String word) {
        return stemmer.stem(word);
    }

    /**
     * Checks if the query is a phrase search (enclosed in quotation marks).
     *
     * @param query The search query to check
     * @return True if the query is a phrase search, false otherwise
     */
    private boolean isPhraseSearch(String query) {
        return query.startsWith("\"") && query.endsWith("\"");
    }
//    public static void main(String[] args) {
//        //MongoClientConnect.start();
//        QueryProcessor queryProcessor = new QueryProcessor();
//        try (Scanner scanner = new Scanner(System.in)) {
//            while (true) {
//                System.out.println("Enter the Query or 'e' to exit:");
//                String query = scanner.nextLine().trim();
//                if (query.equalsIgnoreCase("e")) {
//                    break;
//                }
//                List<invertedIndex> pages = queryProcessor.processQuery(query);
//                System.out.println("Total Results: " + pages.size());
//                for (invertedIndex page : pages) {
//                    System.out.println(page.toString());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}

