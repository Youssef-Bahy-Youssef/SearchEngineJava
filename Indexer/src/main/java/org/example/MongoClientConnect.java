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
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;

public class MongoClientConnect {
    public static MongoCollection<Document> invertedIndexCollection;
    public static void main(String[] args) {
        String connectionString = "mongodb+srv://abdomohamed9192:abdomohamed9192@cluster0.tuy1dlo.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
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
                MongoDatabase database = mongoClient.getDatabase("SearchIndex");
                invertedIndexCollection = database.getCollection("invertedIndex");
                Document doc = invertedIndexCollection.find().first();
                if (doc != null) {
                    System.out.println(doc.toJson());
                } else {
                    System.out.println("No matching documents found.");
                }
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
                Insert("",new IndexerObj());
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
        public static void Insert(String word, IndexerObj item) {
            Document document = new Document("word", word)
                                .append("url", item.url)
                                .append("tf_idf", item.TFIDF)
                                .append("positions", item.positions)
                                .append("weight",item.weight)
                                .append("rank",item.rank);
            InsertOneResult result = invertedIndexCollection.insertOne(document);
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
    }
}
