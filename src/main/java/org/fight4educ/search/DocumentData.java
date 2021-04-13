package org.fight4educ.search;


import java.util.HashMap;
import java.util.Map;

public class DocumentData {
    private Map<String,Double> termFrequency = new HashMap<String, Double>();
    public void putTermFrequency(String term, Double freq){
        termFrequency.put(term, freq);
    }
    public double getTermFrequency(String term){
        return termFrequency.get(term);
    }
}
