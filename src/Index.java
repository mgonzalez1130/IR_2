import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Index {

    private ArrayList<TermHashMap> termHashMaps;
    private ByteOffsetMap byteOffsets;
    private static boolean stop;
    private static boolean stem;
    private static File tempIndex;
    private static File finalIndex;

    private static final int MAX_TERMS = 1000;

    public Index(boolean stop, boolean stem, String indexNumber) {
        this.termHashMaps = new ArrayList<TermHashMap>();
        this.byteOffsets = new ByteOffsetMap();
        this.stop = stop;
        this.stem = stem;

        this.tempIndex = new File("tempIndex.ser");
        this.finalIndex = new File("finalIndex.ser" + indexNumber);
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
     * Indexes the given docId and text. If at any point the termHashMaps has
     * the maximum number of elements allowed, the termHashMaps are written to
     * the tempIndex file and the termHashMaps list is cleared
     *
     * @param docId
     *            the docId from which the text came from
     * @param text
     *            the text to be indexed
     */
    public void indexDoc(String docId, String text) {
        text = IndexUtils.cleanText(text, stop, stem);
    }

    /**
     * Indexes the given docId, term, and position
     *
     * @param docId
     *            the docId from which the term came
     * @param term
     *            the term to be indexed
     * @param position
     *            the position of that term in the text
     */
    public void indexTerm(String docId, String term, int position) {

    }

    /**
     * Writes any termHashMaps left in termHashMaps to the tempIndex file and
     * stores the ByteOffsets in the byteOffsets map.
     */
    public void finishIndexing() {

    }

    /**
     * Returns the position of the termHashMap associated with the given term.
     * If there is no termHashMap in termHashMaps that is associated with the
     * given term, returns -1.
     *
     * @param term
     *            the term being searched for
     * @return the position of the termHashMap associated with the given term,
     *         or -1 if there is no termHashMap currently associated with the
     *         given term.
     */
    private int getTermListPosition(String term) {
        for (int i = 0; i < termHashMaps.size(); i++) {
            if (termHashMaps.get(i).getTerm() == term) {
                return i;
            }
        }
        return -1;
    }
}
