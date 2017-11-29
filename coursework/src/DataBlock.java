package coursework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Name: DataBlock
 *
 * This class represents any data block which contains a sequence of bytes in the volume the user opened.
 * This class contains various methods which operate on arrays of bytes specified by the user (e.g. opening a file).
 * MORE
 *
 * @author Harry Baines
 */
public class DataBlock {
    
    private Volume vol;                 /* Volume reference which this file is part of */
    private ByteBuffer byteBuffer;      /* Buffer containing bytes for this data block */

    /** 
     * Constructor to initialise a data block represented inside a given volume.
     * @param vol The passed volume instance.
     */
    public DataBlock(Volume vol) {
        this.vol = vol;

        // Wrap existing volume byte array to byte buffer
        byteBuffer = ByteBuffer.wrap(vol.getFileInBytes());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Reads at most length bytes starting at byte offset startByte from start of
     * file. Byte 0 is first byte in the file.
     *
     * @param startByte The byte to begin reading from in the file.
     * @param length The total number of bytes to read.
     * @return The array of bytes from the file.
     */
    public byte[] read(long startByte, long length) {

        byte[] specifiedBytes = new byte[(int) length];

        for (int curByte = 0; curByte < length; curByte++) {

            specifiedBytes[curByte] = this.getUnsignedByte((int) startByte, this.byteBuffer);
            startByte++;

        }
        return specifiedBytes;
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
     * Method to obtain an unsigned integer from a signed 2s complement integer (byte) from the byte buffer.
     * @return Unsigned integer value.
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
     * Method to obtain an unsigned integer from a signed 2s complement integer (byte) from the byte buffer.
     * @return Unsigned integer value.
     */
    public byte getUnsignedByte(int i, ByteBuffer b) {
        return ((byte) (b.get(i) & 0xFF));
    }
    
    /**
     * Returns the byte buffer which stores bytes in this data block.
     * @return The byte buffer of data block bytes.
     */
    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    /** 
     * Returns the volume that this data block is located in.
     * @return The volume instance.
     */
    public Volume getVolume() {
        return this.vol;
    }
}