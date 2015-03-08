import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Index {

    private Parser parser;
    private HashMap<String, TermHashMap> termHashMaps;
    private ByteOffsetMap byteOffsets;
    private HashMap<String, Integer> docLengths;
    private HashMap<String, long[]> finalByteOffsets;
    private boolean stop;
    private boolean stem;
    private File tempIndex;
    private File finalIndex;
    private File docLengthsFile;
    private File collectionStats;
    private File byteOffsetsFile;

    private static final int MAX_TERMS = 10000;

    public Index(boolean stop, boolean stem, String indexNumber, Parser parser) {
        this.parser = parser;
        this.termHashMaps = new HashMap<String, TermHashMap>();
        this.byteOffsets = new ByteOffsetMap();
        this.docLengths = new HashMap<String, Integer>();
        this.finalByteOffsets = new HashMap<String, long[]>();
        this.stop = stop;
        this.stem = stem;

        this.tempIndex = new File("tempIndex" + indexNumber + ".ser");
        this.finalIndex = new File("finalIndex" + indexNumber + ".ser");
        this.docLengthsFile = new File("docLengths" + indexNumber + ".ser");
        this.collectionStats = new File("collectionStats" + indexNumber
                + ".ser");
        this.byteOffsetsFile = new File("byteOffsets" + indexNumber + ".ser");
        try {
            tempIndex.delete();
            tempIndex.createNewFile();

            finalIndex.delete();
            finalIndex.createNewFile();

            docLengthsFile.delete();
            docLengthsFile.createNewFile();

            collectionStats.delete();
            collectionStats.createNewFile();

            byteOffsetsFile.delete();
            byteOffsetsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Indexes the given docId and text.
     *
     * @param docId
     *            the docId from which the text came from
     * @param text
     *            the text to be indexed
     */
    public void indexDoc(String docId, String text) {
        ArrayList<Term> termList = parser.cleanText(text, stop, stem);
        // System.out.println("Indexing document: " + docId + " which has "
        // + termList.size() + " terms");
        docLengths.put(docId, termList.size());
        for (Term term : termList) {
            indexTerm(docId, term.getTerm(), term.getPosition());
        }
    }

    /**
     * Indexes the given docId, term, and position. If at any point the
     * termHashMaps has the maximum number of elements allowed, the termHashMaps
     * are written to the tempIndex file and the termHashMaps list is cleared
     *
     * @param docId
     *            the docId from which the term came
     * @param term
     *            the term to be indexed
     * @param position
     *            the position of that term in the text
     */
    public void indexTerm(String docId, String term, Integer position) {
        if (termHashMaps.size() >= MAX_TERMS) {
            writeTermHashMapsToFile();
        } else {
            if (termHashMaps.containsKey(term)) {
                termHashMaps.put(term,
                        termHashMaps.get(term).addPosition(docId, position));
            } else {
                termHashMaps.put(term,
                        new TermHashMap(term).addPosition(docId, position));
            }
        }
    }

    private void writeTermHashMapsToFile() {
        // System.out.println("Writing " + termHashMaps.size()
        // + " term hashmaps to file");
        for (String term : termHashMaps.keySet()) {
            TermHashMap termHM = termHashMaps.get(term);
            ByteOffset termBO = IndexUtils.writeToFile(
                    IndexUtils.serialize(termHM.getMap()), tempIndex);
            byteOffsets.addByteOffset(term, termBO);
        }
        termHashMaps.clear();
    }

    /**
     * Writes any termHashMaps left in termHashMaps to the tempIndex file and
     * stores the ByteOffsets in the byteOffsets map.
     */
    public void finishIndexing() {
        writeTermHashMapsToFile();
        System.out.println("Merging partial indexes...");
        mergePartialIndexes();
        System.out
                .println("Writing files for byteOffsets, docLengths, and collectionStats...");

        IndexUtils.writeToFile(IndexUtils.serialize(finalByteOffsets),
                byteOffsetsFile);

        IndexUtils
        .writeToFile(IndexUtils.serialize(docLengths), docLengthsFile);

        writeCollectionStats();

        System.out.println("Finished indexing");
    }

    private void writeCollectionStats() {
        double avgDocLength = 0;
        for (String docId : docLengths.keySet()) {
            avgDocLength += docLengths.get(docId);
        }
        avgDocLength /= docLengths.size();

        double vocabSize = finalByteOffsets.size();
        double[] collectionStats = { avgDocLength, vocabSize };
        IndexUtils.writeToFile(IndexUtils.serialize(collectionStats),
                this.collectionStats);

    }

    @SuppressWarnings("unchecked")
    public void mergePartialIndexes() {
        HashMap<String, ArrayList<ByteOffset>> byteOffsetMap = byteOffsets
                .getMap();
        ArrayList<ByteOffset> byteOffsets = null;
        HashMap<String, ArrayList<Integer>> resultTermHashMap = null;
        System.out.println("Terms to process: " + byteOffsetMap.size());

        // int counter = 1;
        for (String term : byteOffsetMap.keySet()) {
            // System.out.println("Merging maps for term number " + counter +
            // ": "
            // + term);
            byteOffsets = byteOffsetMap.get(term);
            resultTermHashMap = new HashMap<String, ArrayList<Integer>>();

            HashMap<String, ArrayList<Integer>> currentTermHashMap = null;
            for (ByteOffset byteOffset : byteOffsets) {
                currentTermHashMap = (HashMap<String, ArrayList<Integer>>) IndexUtils
                        .readObjectFromFile(byteOffset, tempIndex);
                resultTermHashMap = addAll(resultTermHashMap,
                        currentTermHashMap);
            }

            ByteOffset resultByteOffset = IndexUtils.writeToFile(
                    IndexUtils.serialize(resultTermHashMap), finalIndex);
            long[] byteOffsetArray = { resultByteOffset.getStartByte(),
                    resultByteOffset.getByteArrayLength() };
            finalByteOffsets.put(term, byteOffsetArray);
            // counter++;
        }
    }

    private HashMap<String, ArrayList<Integer>> addAll(
            HashMap<String, ArrayList<Integer>> resultTermHashMap,
            HashMap<String, ArrayList<Integer>> currentTermHashMap) {

        ArrayList<Integer> resultPositions = null;
        ArrayList<Integer> currentPositions = null;
        for (String docId : currentTermHashMap.keySet()) {
            if (resultTermHashMap.containsKey(docId)) {
                resultPositions = resultTermHashMap.get(docId);
                currentPositions = currentTermHashMap.get(docId);
                resultPositions.addAll(currentPositions);
                resultTermHashMap.put(docId, resultPositions);
            } else {
                resultTermHashMap.put(docId, currentTermHashMap.get(docId));
            }
        }
        return resultTermHashMap;
    }

    @SuppressWarnings("unchecked")
    public void checkIndex() {
        System.out.println("Terms in index: " + finalByteOffsets.size());

        for (String term : finalByteOffsets.keySet()) {
            ByteOffset objectByteOffset = new ByteOffset(
                    finalByteOffsets.get(term)[0],
                    (int) finalByteOffsets.get(term)[1]);
            HashMap<String, ArrayList<Integer>> termMap = (HashMap<String, ArrayList<Integer>>) IndexUtils
                    .readObjectFromFile(objectByteOffset, finalIndex);
            System.out.println("The term " + term + " is in " + termMap.size()
                    + " documents");
        }
    }

    public HashMap<String, long[]> getFinalByteOffsets() {
        return finalByteOffsets;
    }
}
