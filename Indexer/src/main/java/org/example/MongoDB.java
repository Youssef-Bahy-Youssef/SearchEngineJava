//package org.example;
//
//
//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.MongoException;
//import com.mongodb.ServerApi;
//import com.mongodb.ServerApiVersion;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoDatabase;
//import org.bson.Document;
//
//public class MongoDB {
//    MongoDatabase database;
//    public  MongoDB() {
//        String connectionString = "mongodb+srv://youssefbahy2022:OsBa!19496@mongodb.sad4ylf.mongodb.net/?retryWrites=true&w=majority&appName=MongoDB";
//        ServerApi serverApi = ServerApi.builder()
//                .version(ServerApiVersion.V1)
//                .build();
//        MongoClientSettings settings = MongoClientSettings.builder()
//                .applyConnectionString(new ConnectionString(connectionString))
//                .serverApi(serverApi)
//                .build();
//        // Create a new client and connect to the server
//        try (MongoClient mongoClient = MongoClients.create(settings)) {
//            try {
//                // Send a ping to confirm a successful connection
//                database = mongoClient.getDatabase("admin");
//                database.runCommand(new Document("ping", 1));
//                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
//            } catch (MongoException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//    public void Insert(String word, IndexerObj item) {
//        Document document = new Document("word", word)
//                                .append("url", item.url)
//                                .append("TFIDF", item.TFIDF)
//                                .append("positions", item.positions);
//        database.getCollection("Tokens").createIndex(document);
//    }
//    public static void main(String[] args) {
//
//    }
//}
