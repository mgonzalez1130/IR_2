public class Document {

    private String docId;
    private String text;

    public Document(String docId, String text) {
        this.docId = docId;
        this.text = text;
    }

    public String getDocID() {
        return this.docId;
    }

    public String getText() {
        return this.text;
    }

}
