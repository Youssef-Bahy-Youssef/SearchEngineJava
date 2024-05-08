package QueryProcessorPackage;

import org.example.Indexer;
import opennlp.tools.stemmer.PorterStemmer;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.example.MongoClientConnect;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The QueryProcessor class handles the processing of search queries,
 * including preprocessing and searching the index for relevant documents.
 */
public class QueryProcessor {
    private PorterStemmer stemmer;

    /**
     * Constructor for the QueryProcessor class.
     * Initializes with an instance of Indexer.
     *
     */
    public QueryProcessor() {
        this.stemmer = new PorterStemmer();
    }

    /**
     * Processes the search query by preprocessing and searching the index.
     * Handles both regular search queries and phrase searching.
     *
     * @param query The search query to process
     * @return List of relevant documents
     */
    public List<Document> processQuery(String query) {
        // Check if the query is a phrase search
        if (isPhraseSearch(query)) {
            return searchIndex(query); // Perform phrase search
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
    private List<Document> searchIndex(List<String> processedQuery) {
        List<Document> relevantDocuments = new ArrayList<>();

        for (String word : processedQuery) {
            // Retrieve documents associated with each word from the Indexer
            List<Document> documents = MongoClientConnect.getDocumentsByWord(word);
            if (documents != null) {
                relevantDocuments.addAll(documents);
            }
        }

        return relevantDocuments;
    }

    /**
     * Searches the index for documents containing the exact phrase.
     *
     * @param query The phrase search query
     * @return List of relevant documents
     */
    // after ali part
    private List<Document> searchIndex(String query) {
        List<Document> relevantDocuments = new ArrayList<>();

        // Remove quotation marks from the query
        String phrase = query.substring(1, query.length() - 1);
        // Split the phrase into individual words
        String[] words = phrase.split("\\s+");
        // Build regex pattern to match exact phrase
        StringBuilder patternBuilder = new StringBuilder();
        for (String word : words) {
            patternBuilder.append("\\b").append(Pattern.quote(word)).append("\\b.*?");
        }
        Pattern pattern = Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);

//        // Search index for documents containing the exact phrase
//        for (Document document : searchIndex.getAllDocuments()) {
//            String text = document.getString("text"); // Assuming "text" field contains document content
//            Matcher matcher = pattern.matcher(text);
//            if (matcher.find()) {
//                relevantDocuments.add(document);
//            }
//        }
        return relevantDocuments;
    }

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
    /*for testing*/
    public static void main(String[] args) throws Exception {
        MongoClientConnect.start();
        QueryProcessor queryProcessor = new QueryProcessor();
        while(true) {
            System.out.println("Enter the Query or e to break");
            Scanner scanner = new Scanner(System.in);
            String query = scanner.nextLine();
            if(query.equals("e"))
                break;
            List<Document> result = queryProcessor.processQuery(query);
        }
    }
}
