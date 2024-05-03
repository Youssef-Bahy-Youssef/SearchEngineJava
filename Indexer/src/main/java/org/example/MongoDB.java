package org.example;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDB {
    MongoDatabase database;
    public  MongoDB() {
        String connectionString = "mongodb://localhost:27017";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                database = mongoClient.getDatabase("SearchIndex");
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }

//        IndexerObj item = new IndexerObj();
//        item.url="google.com";
//        item.weight = 10;
//        item.TFIDF = 0.8;
//        item.rank = 123.3;

//        Document document = new Document("word", "omar")
//                .append("url", item.url)
//                .append("TFIDF", item.TFIDF)
//                .append("positions", item.positions);
//
//        database.getCollection("Tokens").createIndex(document);

        MongoCollection<Document> collection= database.getCollection("Tokens");
        org.bson.Document doc =(org.bson.Document) new org.bson.Document("name","hello world");
        collection.insertOne(doc);
        System.out.println("########### Insertion is completed  ###############");

    }
    public void Insert(String word, IndexerObj item) {
        Document document = new Document("word", word)
                                .append("url", item.url)
                                .append("TFIDF", item.TFIDF)
                                .append("positions", item.positions);
        database.getCollection("Tokens").createIndex(document);
    }
    public static void main(String[] args) {
        MongoDB mongoDB = new MongoDB();
    }
}
