package model;

import java.util.HashMap;
import java.util.Map;

public class DocumentData {
    private Map<String, Double> termTofrequency = new HashMap<String, Double>();

    public void putTermFrequency(String term, Double frequency) {
        termTofrequency.put(term, frequency);
    }

    public double getFrequency(String term) {
        return termTofrequency.get(term);
    }
}
