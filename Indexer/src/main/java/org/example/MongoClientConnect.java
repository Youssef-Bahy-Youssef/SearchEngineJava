package org.example;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
public class MongoClientConnect {
    public static void main(String[] args) {
        String connectionString = "mongodb+srv://abdomohamed9192:abdomohamed9192@cluster0.tuy1dlo.mongodb.net/SearchIndex?retryWrites=true&w=majority&appName=Cluster0";
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
                MongoDatabase database = mongoClient.getDatabase("admin");
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");

            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
        public void Insert(String word, IndexerObj item) {
        Document document = new Document("word", word)
                                .append("url", item.url)
                                .append("tf_idf", item.TFIDF)
                                .append("positions", item.positions)
                                .append("weight",item.weight)
                                .append("rank",item.rank);
        //database.getCollection("Tokens").createIndex(document);
    }
}