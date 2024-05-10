package com.example.SearchEngine.Repository;

import com.example.SearchEngine.Model.invertedIndex;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvertedFileRepo extends MongoRepository<invertedIndex,String> {
    @Query("{'Word': ?0}") // Specify the field name as it is in the database
    List<invertedIndex> findByWord(String word);
}
