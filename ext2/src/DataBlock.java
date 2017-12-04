package ext2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Name: DataBlock
 *
 * This class represents any data block which contains a sequence of bytes in the volume the user opened.
 * This class contains various methods which operate on arrays of bytes specified by the user (e.g. opening a file).
 *
 * @author Harry Baines
 */
public abstract class DataBlock {
    
    private Volume vol;                 /* Volume reference which this data block is part of */
    private ByteBuffer byteBuffer;      /* Byte buffer to store all bytes for this data block */

    /** 
     * Constructor to initialise a data block represented inside a given volume.
     * @param vol The passed volume instance.
     */
    public DataBlock(Volume vol) {
        this.vol = vol;
    }

    /**
     * Reads at most length bytes starting at byte offset startByte from start of
     * file. Byte 0 is first byte in the file.
     *
     * @param startByte The byte to begin reading from in the file.
     * @param length The total number of bytes to read.
     * @return The array of bytes from the file.
     */
    public byte[] readBlock(long startByte, long length) {

        byteBuffer = ByteBuffer.allocate((int) length);

        // Read specified portion of bytes from volume byte buffer
        for (int curByte = 0; curByte < byteBuffer.limit(); curByte++) {
            byteBuffer.put(this.vol.getByteBuffer().get((int) startByte));
            startByte++;
        }

        return byteBuffer.array();
    }

    /** 
     * Returns the volume that this data block is located in.
     * @return The volume instance.
     */
    public Volume getVolume() {
        return this.vol;
    }
}