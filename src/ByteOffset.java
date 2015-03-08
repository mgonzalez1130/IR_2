public class ByteOffset {

    private long startByte;
    private int byteArrayLength;

    public ByteOffset(long startByte, int byteArrayLength) {
        this.startByte = startByte;
        this.byteArrayLength = byteArrayLength;
    }

    public long getStartByte() {
        return this.startByte;
    }

    public int getByteArrayLength() {
        return this.byteArrayLength;
    }

}
