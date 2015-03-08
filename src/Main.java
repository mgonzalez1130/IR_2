import java.io.File;
import java.util.ArrayList;

public class Main {

    private static Index index;

    public static void main(String[] args) {

        // index without stopping and no stemming
        index = new Index(false, false, "1");
        buildIndex();
        processQueries("1");

        // index with stopping but without stemming
        // index = new Index(true, false, "2");

        // index without stopping but with stemming
        // index = new Index(false, true, "3");

        // index with both stopping and stemming
        // index = new Index(true, true, "4");
    }

    private static void buildIndex() {
        System.out.println("Starting indexing");
        File folder = new File("ap89_collection");
        File[] files = folder.listFiles();
        for (File file : files) {
            ArrayList<Document> documents = Parser.getDocuments(file);
            System.out.println("Indexing file " + file.getName()
                    + " which contains " + documents.size() + " documents");

            for (Document doc : documents) {
                index.indexDoc(doc.getDocID(), doc.getText().toLowerCase());
            }
        }
        index.finishIndexing();
        index.mergePartialIndexes();
    }

    private static void processQueries(String indexNumber) {

    }
}
