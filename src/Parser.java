import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static ArrayList<String> stopList;
    private static final int QUERY_START_INDEX = 4;

    public Parser(String stopListPath) {
        stopList = readStopList(stopListPath);
    }

    public static ArrayList<Document> getDocuments(File file) {
        ArrayList<Document> documents = new ArrayList<Document>();

        // Initialize the BufferedReader
        BufferedReader docReader = null;
        try {
            docReader = new BufferedReader(new FileReader(file));

            String line;
            String docId = "";
            boolean readingText = false;
            StringBuilder textBuilder = new StringBuilder();
            while ((line = docReader.readLine()) != null) {
                line = line.trim();
                if (line.equals(""))
                    continue;

                String[] splitLine = line.split("\\s+");
                if (splitLine[0].equals("<DOCNO>")) {
                    docId = splitLine[1];
                    // System.out.println("ID: " + docId);
                }
                if (splitLine[0].equals("<TEXT>")) {
                    readingText = true;
                    continue;
                }
                if (splitLine[0].equals("</TEXT>")) {
                    readingText = false;
                    continue;
                }
                if (readingText) {
                    textBuilder.append(line);
                    textBuilder.append(" ");
                    continue;
                }
                if (splitLine[0].equals("</DOC>")) {
                    documents.add(new Document(docId, textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close the BufferedReader
            try {
                docReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return documents;
    }

    public static ArrayList<Term> cleanText(String text, boolean stop,
            boolean stem) {
        ArrayList<String> textTokens = tokenize(text);
        ArrayList<Term> processedTokens = new ArrayList<Term>(textTokens.size());

        int termPosition = 1;
        for (int i = 0; i < textTokens.size(); i++) {
            String token = textTokens.get(i);

            if (token.equals("s") || token.equals("t")) {
                continue;
            }
            if (stop) {
                if (stopList.contains(token)) {
                    termPosition++;
                    continue;
                }
            }
            if (stem) {
                Stemmer stemmer = new Stemmer();
                stemmer.add(token.toCharArray(), token.length());
                stemmer.stem();
                processedTokens.add(new Term(stemmer.toString(), termPosition));
                termPosition++;
                continue;
            }
            processedTokens.add(new Term(token, termPosition));
        }
        return processedTokens;
    }

    private static ArrayList<String> tokenize(String text) {
        Pattern pattern = Pattern.compile("\\w+(\\.?\\w+)*");
        Matcher matcher = pattern.matcher(text.toLowerCase());
        // System.out.println(text.toLowerCase());
        ArrayList<String> result = new ArrayList<String>(matcher.groupCount());

        while (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                result.add(matcher.group(i));
            }
        }
        return result;
    }

    private ArrayList<String> readStopList(String stopListPath) {
        ArrayList<String> stopList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(stopListPath));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("")) {
                    continue;
                }
                stopList.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stopList;
    }
}
