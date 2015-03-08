import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to handle storing all of the <docId:listOfPositions> pairs associated
 * with a term
 *
 * @author Moses
 *
 */
public class TermHashMap {

    private String term;
    private HashMap<String, ArrayList<Integer>> map; // HashMap<docId:ListOfPositions>

    public TermHashMap(String term) {
        this.term = term;
        map = new HashMap<String, ArrayList<Integer>>();
    }

    public String getTerm() {
        return this.term;
    }

    public HashMap<String, ArrayList<Integer>> getMap() {
        return this.map;
    }

    public TermHashMap addPosition(String docId, Integer position) {
        if (map.containsKey(docId)) {
            ArrayList<Integer> positions = map.get(docId);
            positions.add(position);
            map.put(docId, positions);
        } else {
            ArrayList<Integer> positions = new ArrayList<Integer>();
            positions.add(position);
            map.put(docId, positions);
        }
        return this;
    }
}
