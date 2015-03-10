import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Models {

    // private static double avgDocLength = 165.97319319792157;
    // private static Map<String, Integer> docLengths;
    // private static long vocabSize = 138220;
    // private static int NUM_OF_DOCS = 84678;
    // private static int df;
    // private static int ttf;
    // private static int collectionLength = 14054610;

    private ArrayList<HashMap<String, ArrayList<Integer>>> queryTermStats;
    private HashMap<String, Integer> docLengths;
    private double avgDocLength;
    private double vocabSize;
    private double collectionLength;
    private double K1 = 1.2;
    private double K2 = 0.25;
    private double B = 0;

    public Models(
            ArrayList<HashMap<String, ArrayList<Integer>>> queryTermStats,
            HashMap<String, Integer> docLengths, double avgDocLength,
            double vocabSize) {
        this.queryTermStats = queryTermStats;
        this.docLengths = docLengths;
        this.avgDocLength = avgDocLength;
        this.vocabSize = vocabSize;

        this.collectionLength = 0;
        for (String docId : docLengths.keySet()) {
            this.collectionLength += docLengths.get(docId);
        }
    }

    public SortedSet<Map.Entry<String, Double>> okapiTF() {
        HashMap<String, Double> results = new HashMap<>();

        for (int i = 0; i < queryTermStats.size(); i++) {
            HashMap<String, ArrayList<Integer>> currentStats = queryTermStats
                    .get(i);

            for (String docId : currentStats.keySet()) {
                double termFreq = currentStats.get(docId).size();

                double score = termFreq
                        / (termFreq + 0.5 + ((1.5 * docLengths.get(docId)) / avgDocLength));
                if (results.containsKey(docId)) {
                    results.put(docId, results.get(docId) + score);
                } else {
                    results.put(docId, score);
                }
            }
        }
        return entriesSortedByValues(results);
    }

    public SortedSet<Map.Entry<String, Double>> tfIDF() {
        HashMap<String, Double> results = new HashMap<>();

        for (int i = 0; i < queryTermStats.size(); i++) {
            HashMap<String, ArrayList<Integer>> currentStats = queryTermStats
                    .get(i);

            for (String docId : currentStats.keySet()) {
                double termFreq = currentStats.get(docId).size();

                double score = (termFreq / (termFreq + 0.5 + (1.5 * (docLengths
                        .get(docId) / avgDocLength))))
                        * Math.log(docLengths.size() / currentStats.size());
                if (results.containsKey(docId)) {
                    results.put(docId, results.get(docId) + score);
                } else {
                    results.put(docId, score);
                }
            }
        }
        return entriesSortedByValues(results);
    }

    public SortedSet<Map.Entry<String, Double>> okapiBM25() {
        HashMap<String, Double> results = new HashMap<>();

        for (int i = 0; i < queryTermStats.size(); i++) {
            HashMap<String, ArrayList<Integer>> currentStats = queryTermStats
                    .get(i);

            double numOfDocs = docLengths.size();
            double df = currentStats.size();
            for (String docId : currentStats.keySet()) {
                double tf = currentStats.get(docId).size();

                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }

                double term1 = Math.log((numOfDocs + 0.5) / (df + 0.5));
                double term2 = (tf + (K1 * tf))
                        / (tf + (K1 * ((1 - B) + (B * (docLengths.get(docId) / avgDocLength)))));
                double term3 = (tf + (K2 * tf)) / (tf + K2);

                double okapiBM25Score = term1 * term2 * term3;

                results.put(docId, results.get(docId) + okapiBM25Score);
            }
        }
        return entriesSortedByValues(results);
    }

    public SortedSet<Map.Entry<String, Double>> LMLaplace() {
        HashMap<String, Double> results = new HashMap<>();
        HashSet<String> allDocIds = new HashSet<String>();

        for (int i = 0; i < queryTermStats.size(); i++) {
            allDocIds.addAll(queryTermStats.get(i).keySet());
        }

        for (int i = 0; i < queryTermStats.size(); i++) {
            HashMap<String, ArrayList<Integer>> currentStats = queryTermStats
                    .get(i);

            for (String docId : allDocIds) {
                Integer tf;
                if (currentStats.containsKey(docId)) {
                    tf = currentStats.get(docId).size();
                } else {
                    tf = 0;
                }

                double p_laplace = (tf + 1)
                        / ((double) docLengths.get(docId) + vocabSize);
                double LMLaplace = Math.log(p_laplace);
                if (results.containsKey(docId)) {
                    results.put(docId, results.get(docId) + LMLaplace);
                } else {
                    results.put(docId, LMLaplace);
                }
            }
        }
        return entriesSortedByValues(results);
    }

    public SortedSet<Map.Entry<String, Double>> LMJM() {
        HashMap<String, Double> results = new HashMap<>();
        Set<String> allDocIds = new HashSet<String>();

        for (int i = 0; i < queryTermStats.size(); i++) {
            allDocIds.addAll(queryTermStats.get(i).keySet());
        }

        for (int i = 0; i < queryTermStats.size(); i++) {
            HashMap<String, ArrayList<Integer>> currentStats = queryTermStats
                    .get(i);

            double ttf = 0;

            for (String docId : currentStats.keySet()) {
                ttf += currentStats.get(docId).size();
            }

            for (String docId : allDocIds) {
                Integer tf = 0;
                if (currentStats.containsKey(docId)) {
                    tf = currentStats.get(docId).size();
                }

                double currentDocLength = docLengths.get(docId);
                double lambda = 0.1;
                double term1 = lambda * (tf / currentDocLength);
                double term2 = (1 - lambda) * (ttf / collectionLength);
                double LMJMScore = Math.log(term1 + term2);

                if (results.containsKey(docId)) {
                    results.put(docId, results.get(docId) + LMJMScore);
                } else {
                    results.put(docId, LMJMScore);
                }
            }
        }
        return entriesSortedByValues(results);
    }

    public SortedSet<Map.Entry<String, Double>> proximity() {
        HashMap<String, Double> results = new HashMap<>();
        Set<String> allDocIds = new HashSet<String>();
        for (int i = 0; i < queryTermStats.size(); i++) {
            allDocIds.addAll(queryTermStats.get(i).keySet());
        }

        for (String docId : allDocIds) {
            ArrayList<ArrayList<Integer>> docTermResults = new ArrayList<ArrayList<Integer>>();
            double score = 0;
            ArrayList<Integer> numOfPositions = new ArrayList<Integer>();
            int longestPositionList = 0;

            for (HashMap<String, ArrayList<Integer>> queryTermResult : queryTermStats) {
                if (queryTermResult.containsKey(docId)) {
                    docTermResults.add(queryTermResult.get(docId));
                    numOfPositions.add(queryTermResult.get(docId).size());
                    if (queryTermResult.get(docId).size() > longestPositionList) {
                        longestPositionList = queryTermResult.get(docId).size();
                    }
                }
            }

            int[][] positions = new int[docTermResults.size()][longestPositionList];

            for (int i = 0; i < docTermResults.size(); i++) {
                for (int j = 0; j < longestPositionList; j++) {

                }
            }

        }

        return null;

    }

    // Returns the given HashMap as a SortedSet sorted in descending order
    // This method was copied from a post on stack overflow found at:
    // http://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
    private static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
            Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue()) * -1;
                        return res != 0 ? res : 1;
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}
