import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class IndexUtils {

    public static Object readObjectFromFile(ByteOffset byteOffset, File file) {
        ByteBuffer result = ByteBuffer
                .allocate(byteOffset.getByteArrayLength());
        FileChannel fc = null;

        try {
            fc = FileChannel.open(file.toPath());
            fc.read(result, byteOffset.getStartByte());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return deserialize(result.array());
    }

    public static ByteOffset writeToFile(byte[] objBytes, File file) {
        FileChannel fc = null;
        FileOutputStream fos = null;
        long startPosition = 0;

        try {
            fc = FileChannel.open(file.toPath());
            fos = new FileOutputStream(file, true);
            startPosition = fc.size();
            fos.write(objBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ByteOffset(startPosition, objBytes.length);
    }

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = null;
        ObjectOutputStream os = null;

        try {
            out = new ByteArrayOutputStream();
            os = new ObjectOutputStream(out);
            os.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) {
        ByteArrayInputStream bin = null;
        ObjectInputStream ois = null;
        Object result = null;

        try {
            bin = new ByteArrayInputStream(data);
            ois = new ObjectInputStream(bin);
            result = ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                ois.close();
                bin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
