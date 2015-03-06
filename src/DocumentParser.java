import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DocumentParser {

    public static ArrayList<Document> getDocuments(File file) {
        ArrayList<Document> documents = new ArrayList<Document>();

        // Initialize the BufferedReader
        BufferedReader docReader = null;
        try {
            docReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        }

        // iterate through all lines in the file
        try {
            String line;
            String docId = "";
            boolean readingText = false;
            StringBuilder textBuilder = new StringBuilder();
            while ((line = docReader.readLine()) != null) {
                if (line.equals(""))
                    continue;

                String[] splitLine = line.split("\\s+");
                if ((splitLine[0]).equals("<DOCNO>")) {
                    docId = splitLine[1];
                    // System.out.println("ID: " + docId);
                }
                if ((splitLine[0]).equals("<TEXT>")) {
                    readingText = true;
                    continue;
                }
                if ((splitLine[0]).equals("</TEXT>")) {
                    readingText = false;
                    continue;
                }
                if (readingText) {
                    textBuilder.append(line);
                    textBuilder.append(" ");
                    continue;
                }
                if ((splitLine[0]).equals("</DOC>")) {
                    documents.add(new Document(docId, textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close the BufferedReader
        try {
            docReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documents;
    }
}
