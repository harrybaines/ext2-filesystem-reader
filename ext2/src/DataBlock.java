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
public class DataBlock {
    
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
            byteBuffer.put(this.getByte((int) startByte, this.vol.getByteBuffer()));
            startByte++;
        }
        
        return byteBuffer.array();
    }

    /**
     * Method to obtain the group number the iNode belongs to and the index in the array of iNode table pointers.
     * The correct iNode table pointer can then be used to find the iNode required.
     * E.g. A table index of 2 indicates the iNode exists in block group 2 and index 2 of the iNode table pointers is the table pointer to use.
     *
     * @param iNodeNumber The iNode number.
     * @param iNodesPerGroup The total number of iNodes per group.
     * @param totaliNodes The total number of iNodes in the filesystem.
     * @return The table index (group number and index in array of iNode table pointers).
     */
    public int getTablePointerForiNode(int iNodeNumber, int iNodesPerGroup, int totaliNodes) {
        int tableIndex = 0;
        for (int i = iNodesPerGroup; i <= totaliNodes; i += iNodesPerGroup) {
            if (iNodeNumber <= i)
                break;
            tableIndex++;
        }
        return tableIndex;
    }

    /**
     * Method to obtain an unsigned byte from a signed 2s complement signed byte from the byte buffer.
     * @return Unsigned byte.
     */
    public byte getByte(int i, ByteBuffer b) {
        return (b.get(i));
    }

    /**
     * Method to obtain an unsigned short from a signed 2s complement integer (byte) from the byte buffer.
     * @return Unsigned integer value.
     */
    public short getShortFromBytes(int i, ByteBuffer b) {
        return (b.getShort(i));
    }

    /**
     * Method to obtain an unsigned integer from a signed 2s complement integer (byte) from the byte buffer.
     * @return Unsigned integer value.
     */
    public int getIntFromBytes(int i, ByteBuffer b) {
        return (b.getInt(i));
    }

    /** 
     * Returns the volume that this data block is located in.
     * @return The volume instance.
     */
    public Volume getVolume() {
        return this.vol;
    }
}