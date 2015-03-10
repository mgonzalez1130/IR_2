import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

public class QueryProcesser {

    private static File finalIndex;
    private static HashMap<String, long[]> byteOffsets;
    private static double avgDocLength;
    private static double vocabSize;
    private static HashMap<String, Integer> docLengths;
    private static Parser parser;

    public static void main(String[] args) {

        parser = new Parser("stoplist.txt");

        // readInFiles("1");
        // processQueries(false, false, "1");
        //
        // readInFiles("1");
        // processQueries(true, false, "2");
        //
        readInFiles("1");
        processQueries(false, true, "3");

        readInFiles("1");
        processQueries(true, true, "4");

    }

    private static void processQueries(boolean stop, boolean stem,
            String indexNumber) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(
                    "query_desc.51-100.short.txt"));

            String line;
            Integer queryCounter = 1;
            while ((line = br.readLine()) != null) {
                System.out.println("Processing query number: " + queryCounter);
                ArrayList<Term> queryTokens = parser
                        .cleanText(line, stop, stem);
                int queryNumber = Integer
                        .parseInt(queryTokens.get(0).getTerm());
                queryTokens.remove(0);
                processQueryTokens(queryTokens, indexNumber, queryNumber);
                queryCounter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked")
    private static void processQueryTokens(ArrayList<Term> queryTokens,
            String indexNumber, int queryNumber) {
        ArrayList<HashMap<String, ArrayList<Integer>>> queryTermStats = new ArrayList<HashMap<String, ArrayList<Integer>>>(
                queryTokens.size());

        for (Term term : queryTokens) {
            String termString = term.getTerm();
            if (!byteOffsets.containsKey(termString)) {
                continue;
            }
            long[] termByteOffsetArray = byteOffsets.get(termString);
            ByteOffset termByteOffset = new ByteOffset(termByteOffsetArray[0],
                    (int) termByteOffsetArray[1]);
            HashMap<String, ArrayList<Integer>> termStats = (HashMap<String, ArrayList<Integer>>) IndexUtils
                    .readObjectFromFile(termByteOffset, finalIndex);
            queryTermStats.add(termStats);
        }

        printModelResults(queryTermStats, indexNumber, queryNumber);

    }

    private static void printModelResults(
            ArrayList<HashMap<String, ArrayList<Integer>>> queryTermStats,
            String indexNumber, int queryNumber) {
        Models models = new Models(queryTermStats, docLengths, avgDocLength,
                vocabSize);
        // printResults(models.okapiTF(), queryNumber, "okapiTF_" +
        // indexNumber);
        // printResults(models.tfIDF(), queryNumber, "tfIDF_" + indexNumber);
        // printResults(models.okapiBM25(), queryNumber, "okapiBM25_"
        // + indexNumber);
        // printResults(models.LMLaplace(), queryNumber, "LMLaplace_"
        // + indexNumber);
        printResults(models.LMJM(), queryNumber, "LMJM_" + indexNumber);

    }

    private static void printResults(
            SortedSet<Entry<String, Double>> scoredDocs, int queryNumber,
            String fileName) {
        Iterator<Entry<String, Double>> docsIt = scoredDocs.iterator();
        File file = new File(fileName);
        FileWriter pw = null;

        try {
            pw = new FileWriter(file, true);

            int rank = 1;
            int counter = 1;
            while ((counter <= 100) && docsIt.hasNext()) {
                Map.Entry<String, Double> nextDoc = docsIt.next();
                String docId = nextDoc.getKey();
                double score = nextDoc.getValue();
                String line = queryNumber + " Q0 " + docId + " " + rank + " "
                        + score + " Exp";
                pw.write(line + "\n");
                rank++;
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readInFiles(String indexNumber) {
        finalIndex = new File("finalIndex" + indexNumber + ".ser");
        readInByteOffsets(indexNumber);
        readInCollectionStats(indexNumber);
        readInDocLengths(indexNumber);
    }

    @SuppressWarnings("unchecked")
    private static void readInDocLengths(String indexNumber) {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;

        try {
            fileIn = new FileInputStream("docLengths" + indexNumber + ".ser");
            in = new ObjectInputStream(fileIn);
            docLengths = (HashMap<String, Integer>) in.readObject();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                fileIn.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
    }

    private static void readInCollectionStats(String indexNumber) {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;

        try {
            fileIn = new FileInputStream("collectionStats" + indexNumber
                    + ".ser");
            in = new ObjectInputStream(fileIn);
            double[] stats = (double[]) in.readObject();
            avgDocLength = stats[0];
            vocabSize = stats[1];
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                fileIn.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void readInByteOffsets(String indexNumber) {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;

        try {
            fileIn = new FileInputStream("byteOffsets" + indexNumber + ".ser");
            in = new ObjectInputStream(fileIn);
            byteOffsets = (HashMap<String, long[]>) in.readObject();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                fileIn.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
    }
}
