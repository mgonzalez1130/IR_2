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

    public void addPosition(String docId, Integer position) {
        if (map.containsKey(docId)) {
            ArrayList<Integer> positions = map.get(docId);
            positions.add(position);
            map.put(docId, positions);
        } else {
            ArrayList<Integer> positions = new ArrayList<Integer>();
            positions.add(position);
            map.put(docId, positions);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((map == null) ? 0 : map.hashCode());
        result = (prime * result) + ((term == null) ? 0 : term.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TermHashMap other = (TermHashMap) obj;
        if (term == null) {
            if (other.term != null)
                return false;
        } else if (!term.equals(other.term))
            return false;
        return true;
    }

}
