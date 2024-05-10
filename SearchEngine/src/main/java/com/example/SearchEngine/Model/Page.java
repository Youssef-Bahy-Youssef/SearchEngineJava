package com.example.SearchEngine.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page {
    private String url;
    private Double weight;
    private Double tf_idf;
    private Double rank;
    private String  word;

    private List<Integer> positions;

}
