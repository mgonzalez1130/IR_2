import java.io.File;
import java.util.ArrayList;

public class Main {

    private static Index index;

    public static void main(String[] args) {
        index = new Index(false, false, "1");
        buildIndex();
        processQueries("1");
    }

    private static void buildIndex() {
        File folder = new File("ap89_collection");
        File[] files = folder.listFiles();
        for (File file : files) {
            ArrayList<Document> documents = DocumentParser.getDocuments(file);

            for (Document doc : documents) {
                index.indexDoc(doc.getDocID(), doc.getText());
            }
        }
        index.finishIndexing();
    }

    private static void processQueries(String indexNumber) {

    }
}
