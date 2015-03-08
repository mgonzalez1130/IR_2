import java.io.File;
import java.util.ArrayList;

public class Main {

    private static Index index;
    private static Parser parser;

    public static void main(String[] args) {

        parser = new Parser("stoplist.txt");

        // index without stopping and no stemming
        index = new Index(false, false, "1", parser);
        buildIndex();
        System.out.println();

        // index with stopping but without stemming
        index = new Index(true, false, "2", parser);
        buildIndex();
        System.out.println();

        // index without stopping but with stemming
        index = new Index(false, true, "3", parser);
        buildIndex();
        System.out.println();

        // index with both stopping and stemming
        index = new Index(true, true, "4", parser);
        buildIndex();
    }

    private static void buildIndex() {
        System.out.println("Processing files...");
        File folder = new File("ap89_collection");
        File[] files = folder.listFiles();
        for (File file : files) {
            ArrayList<Document> documents = parser.getDocuments(file);
            // System.out.println("Indexing file " + file.getName()
            // + " which contains " + documents.size() + " documents");

            for (Document doc : documents) {
                index.indexDoc(doc.getDocID(), doc.getText());
            }
        }
        index.finishIndexing();
    }

    // private static void processQueries(boolean stop, boolean stem,
    // String indexNumber) {
    //
    // }
}
