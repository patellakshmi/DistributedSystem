package org.fight4educ.search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TFIDF {

    public static List<String> getTheWordsFromLine(String lines){
        return Arrays.asList(lines.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }

    public static List<String> getTheWordsFromLines(List<String> lines){

        List<String> words = new ArrayList<>();
        for(String line: lines){
            words.addAll(Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+")));
        }
        return words;
    }

    public static double calculateTermFrequency(List<String> words, String term){
        double count = 0.0;
        for(String word:words){
            if( word.equalsIgnoreCase(term)){
                count++;
            }
        }
        return count/words.size();
    }

    public static DocumentData calculateDocumentDataList(List<String> words, List<String> terms){
        DocumentData documentData = new DocumentData();
        for(String term: terms){
            double calFreq = calculateTermFrequency(words, term);
            documentData.putTermFrequency(term, calFreq);
        }

        return documentData;
    }

    private static double calculateInverseDocFreq(String term, Map<String, DocumentData> documentDataMap){
        double nt = 0;
        for(String document: documentDataMap.keySet()){
            DocumentData documentData = documentDataMap.get(document);
            double termF = documentData.getTermFrequency(term);
            if( termF > 0 ){
                nt++;
            }
        }

        return nt==0 ? 0:Math.log(documentDataMap.size()/nt);
    }

    private static Map<String, Double> getTermToInverseDocFreq(List<String> terms, Map<String, DocumentData> documentDataMap){
        Map<String, Double> termToIDF = new HashMap<String, Double>();
        for(String term: terms){
            double idf = calculateInverseDocFreq(term, documentDataMap);
            termToIDF.put(term, idf);
        }

        return termToIDF;
    }

    private static double calculateDocumentScore(List<String> terms, DocumentData documentData, Map<String, Double> termWeightWRTDocs){
        double score = 0.0;
        for(String term: terms){
            double freq = documentData.getTermFrequency(term);
            double inverseToTermFreq = termWeightWRTDocs.get(term);
            score += freq*inverseToTermFreq;
        }

        return score;
    }



    public static List<Map.Entry<String, Double>>   getDocumentsSortedByScore(List<String> terms, Map<String,DocumentData> documentDataMap ){
        List<Map.Entry<String, Double>>  sortedByScoreDoc = new ArrayList<>();

        Map<String, Double> termWeightWRTDocs = getTermToInverseDocFreq(terms, documentDataMap);
        for(String document: documentDataMap.keySet()){
            DocumentData documentData = documentDataMap.get(document);
            double score = calculateDocumentScore(terms, documentData, termWeightWRTDocs);
            sortedByScoreDoc.add(new AbstractMap.SimpleEntry<>(document, score));
        }

        Collections.sort(sortedByScoreDoc, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        return sortedByScoreDoc;
    }

    public static void printRelevantDoc(List<Map.Entry<String, Double>> relentDocScore){
        for(Map.Entry<String, Double> entry: relentDocScore){
            System.out.println(String.format("book- %s : Score - %f", entry.getKey(), entry.getValue()));
        }
    }

    public static List<Map.Entry<String, Double>>  findMostRelevantDoc(List<String> documents, List<String> terms) throws FileNotFoundException {
        Map<String, DocumentData> documentDataMap = new HashMap<>();
        for(String document: documents){
            BufferedReader bf = new BufferedReader(new FileReader(document));
            List<String> allDocLines = bf.lines().collect(Collectors.toList());
            List<String> allDocWords = TFIDF.getTheWordsFromLines(allDocLines);
            DocumentData documentData = TFIDF.calculateDocumentDataList(allDocWords, terms);
            documentDataMap.put(document, documentData);
        }

        List<Map.Entry<String, Double>>  sortedByScoreDoc = TFIDF.getDocumentsSortedByScore(terms, documentDataMap);
        TFIDF.printRelevantDoc(sortedByScoreDoc);
        return sortedByScoreDoc;

    }
}
