import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to handle storing all the byte offsets per term, which describe the
 * locations of all the HashMap<docId:positions> associated with that term.
 *
 * @author Moses
 *
 */
public class ByteOffsetMap {

    private HashMap<String, ArrayList<ByteOffset>> map;

    public ByteOffsetMap() {
        map = new HashMap<String, ArrayList<ByteOffset>>();
    }

    public HashMap<String, ArrayList<ByteOffset>> getMap() {
        return this.map;
    }

    public void addByteOffset(String term, ByteOffset byteOffset) {
        if (map.containsKey(term)) {
            ArrayList<ByteOffset> byteOffsets = map.get(term);
            byteOffsets.add(byteOffset);
            map.put(term, byteOffsets);
        } else {
            ArrayList<ByteOffset> byteOffsets = new ArrayList<ByteOffset>();
            byteOffsets.add(byteOffset);
            map.put(term, byteOffsets);
        }
    }
}
