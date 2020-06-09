import model.DocumentData;
import search.TFIDF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SequentialSearch {
    public static String BOOKS_DIRECTORY = "./resources/books";
    private static final String SEARCH_QUERY_1 = "the best detective that catches many criminals using his deductive methods";
    private static final String SEARCH_QUERY_2 = "the girl who falls down a rabbit hole";
    private static final String SEARCH_QUERY_3 = "A war between Russia and France in the cold winter";

    public static void main(String[] args) throws FileNotFoundException {
        File documentsDirectory = new File(BOOKS_DIRECTORY);

        List<String> documents = Arrays.asList(documentsDirectory.list())
            .stream()
            .map(documentName -> BOOKS_DIRECTORY + "/" + documentName)
            .collect(Collectors.toList());

        List<String> terms = TFIDF.getWordsFromLine(SEARCH_QUERY_2);

        findMostRelevantDocuments(documents, terms);
    }

    private static void findMostRelevantDocuments(List<String> documents, List<String> terms) throws FileNotFoundException {
        Map<String, DocumentData> documentDataMap = new HashMap<>();
        for (String document : documents) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(document));
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromDocument(lines);
            DocumentData documentData = TFIDF.createDocumentData(words, terms);
            documentDataMap.put(document, documentData);
        }

        Map<Double, List<String>> documentsByScore = TFIDF.getDocumentsScores(terms, documentDataMap);
        printResults(documentsByScore);
    }

    private static void printResults(Map<Double, List<String>> documentsByScore) {
        for (Map.Entry<Double, List<String>> documentByScoreEntry : documentsByScore.entrySet()) {
            double score = documentByScoreEntry.getKey();
            for (String document : documentByScoreEntry.getValue()) {
                System.out.println(String.format("Book: %s - score %f", document.split("/")[3], score));
            }
        }
    }
}
