package org.example;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IndexerObj {
    String url;
//    ArrayList<Integer>positions;
    Map<Integer, String>positions;
    Double TFIDF;
    Integer weight;
    Double rank;
    IndexerObj() {
        url = new String();
        positions = new HashMap<>();
    }
}
