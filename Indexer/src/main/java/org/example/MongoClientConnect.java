package org.example;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MongoClientConnect {
    public static MongoCollection<Document> invertedIndexCollection;
    public static MongoCollection<Document> wordIndexedCollection;
    public static MongoDatabase database;
    public static MongoClient mongoClient;

    public static void start() {
        String connectionString = "mongodb+srv://abdomohamed9192:abdomohamed9192@cluster0.tuy1dlo.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        try {
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase("SearchIndex");
            invertedIndexCollection = database.getCollection("invertedIndex");
            wordIndexedCollection = database.getCollection("WordIndex");
            Document doc = invertedIndexCollection.find().first();
            if (doc != null) {
                System.out.println(doc.toJson());
            } else {
                System.out.println("No matching documents found.");
            }
            System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static void InsertWord(String word, ArrayList<IndexerObj> items) {
        List<Document> documents = new ArrayList<>();
        for (IndexerObj item : items) {
            Document document = new Document("word", word)
                    .append("url", item.url)
                    .append("tf_idf", item.TFIDF)
                    .append("positions", item.positions)
                    .append("weight", item.weight)
                    .append("rank", item.rank);
            documents.add(document);
        }
        Document wordDocument = new Document("Word", word)
                .append("Pages", documents);
        InsertOneResult result = invertedIndexCollection.insertOne(wordDocument);
        System.out.println("Success! Inserted document id: " + result.getInsertedId());
    }
    public static void InsertWordIndex(String word, Map<String, ArrayList<Integer>> items) {
        List<Document> documents = new ArrayList<>();
        items.forEach((url, indexes) -> {
            Document document = new Document("word", word)
                    .append("url", url)
                    .append("positions", indexes);
            documents.add(document);
        });
        Document wordIndexDocument = new Document("Word", word)
                .append("Pages", documents);
        InsertOneResult result = wordIndexedCollection.insertOne(wordIndexDocument);
        System.out.println("Success! Inserted document id: " + result.getInsertedId());
    }
    /**
     * Retrieves documents associated with a given word from the MongoDB collection.
     *
     * @param word The word to search for in the documents.
     * @return A list of documents associated with the given word.
     */
    public static List<Document> getDocumentsByWord(String word) {
        List<Document> resultDocuments = new ArrayList<>();

        // Filter documents where the "Word" field matches the provided word
        FindIterable<Document> documents = invertedIndexCollection.find(Filters.eq("Word", word));

        // Iterate over the documents found and add them to a list
        for (Document doc : documents) {
            resultDocuments.add(doc);
        }

        return resultDocuments;
    }
    public static List<Document> getWordIndexesByWord(String word) {
        List<Document> resultDocuments = new ArrayList<>();

        // Filter documents where the "Word" field matches the provided word
        FindIterable<Document> documents = wordIndexedCollection.find(Filters.eq("Word", word));

        // Iterate over the documents found and add them to a list
        for (Document doc : documents) {
            resultDocuments.add(doc);
        }

        return resultDocuments;
    }
}

