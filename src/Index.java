import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Index {

    private HashMap<String, TermHashMap> termHashMaps;
    private ByteOffsetMap byteOffsets;
    private HashMap<String, Integer> docLengths;
    private HashMap<String, ByteOffset> finalByteOffsets;
    private boolean stop;
    private boolean stem;
    private File tempIndex;
    private File finalIndex;

    private static final int MAX_TERMS = 10000;

    public Index(boolean stop, boolean stem, String indexNumber) {
        this.termHashMaps = new HashMap<String, TermHashMap>();
        this.byteOffsets = new ByteOffsetMap();
        this.docLengths = new HashMap<String, Integer>();
        this.finalByteOffsets = new HashMap<String, ByteOffset>();
        this.stop = stop;
        this.stem = stem;

        this.tempIndex = new File("tempIndex" + indexNumber + ".ser");
        this.finalIndex = new File("finalIndex" + indexNumber + ".ser");
        try {
            tempIndex.delete();
            tempIndex.createNewFile();

            finalIndex.delete();
            finalIndex.createNewFile();
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
        ArrayList<Term> termList = Parser.cleanText(text, stop, stem);
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
        System.out.println("Merging partial indexes");
        mergePartialIndexes();
        System.out.println("Finished indexing");
    }

    @SuppressWarnings("unchecked")
    public void mergePartialIndexes() {
        HashMap<String, ArrayList<ByteOffset>> byteOffsetMap = byteOffsets
                .getMap();
        ArrayList<ByteOffset> byteOffsets = null;
        HashMap<String, ArrayList<Integer>> resultTermHashMap = null;

        int counter = 1;
        for (String term : byteOffsetMap.keySet()) {
            System.out.println("Merging term number " + counter + " : " + term);
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
            finalByteOffsets.put(term, resultByteOffset);
            counter++;
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
            HashMap<String, ArrayList<Integer>> termMap = (HashMap<String, ArrayList<Integer>>) IndexUtils
                    .readObjectFromFile(finalByteOffsets.get(term), finalIndex);
            System.out.println("The term " + term + " is in " + termMap.size()
                    + " documents");
        }
    }
}
