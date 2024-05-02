//package org.example;
//
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoCursor;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.ServerApi;
//import com.mongodb.ServerApiVersion;
//import com.mongodb.MongoException;
//import com.mongodb.client.MongoClients;
//import org.bson.Document;
//
//public class Connection {
//
//    public static void main(String[] args) {
//
//        // Creating a Mongo client
//        MongoClient mongoClient = new MongoClient("localhost", 27017);
//        System.out.println("Created Mongo Connection successfully");
//
//        MongoDatabase db = mongoClient.getDatabase("mongodbjava");
//        System.out.println("Get database is successful");
//
//        System.out.println("Below are list of databases present in MongoDB");
//        // To get all database names
//        MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
//        while (dbsCursor.hasNext()) {
//            System.out.println(dbsCursor.next());
//        }
//
//        //Inserting sample record by creating collection and document.
//        MongoCollection<Document> collection = db.getCollection("javaprogram");
//        Document doc = new Document("name", "hello world");
//        collection.insertOne(doc);
//        System.out.println("########### Insertion is completed  ###############");
//        MongoCursor<String> dbsCursord = mongoClient.listDatabaseNames().iterator();
//        while (dbsCursord.hasNext()) {
//            System.out.println(dbsCursord.next());
//
//        }
//    }
//}
