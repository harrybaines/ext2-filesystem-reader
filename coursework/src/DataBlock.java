package coursework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataBlock {
	
    private Volume vol;                 // Volume reference which this file is part of
    private ByteBuffer byteBuffer;

    // magic line - (int) fileInBytes[i] & 0xFF;

	public DataBlock(Volume vol) {

		this.vol = vol;

		// Wrap existing byte array to byte buffer
        byteBuffer = ByteBuffer.wrap(vol.getFileInBytes());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public void setByteBuffer(byte[] byteArray) {
		this.byteBuffer = ByteBuffer.wrap(byteArray);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public Volume getVolume() {
		return this.vol;
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
     * Simple method to print the contents of a file in ASCII format.
     * A string is returned containing the full file contents.
     * @return The string of characters in the file.
     */
    public String printFileContents(byte[] bytes) {

        String asciiString = "";

        for (byte b : bytes) {

            // Obtain ASCII equivalent of given byte in array
            int asciiInt = b & 0xFF;
            
            if (asciiInt >= 1 && asciiInt < 256)
                asciiString += (char)asciiInt;
        
        }

        return asciiString;
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

	public ByteBuffer getByteBuffer() {
		return this.byteBuffer;
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

    // /**
    //  * Method to obtain an unsigned short from a signed 2s complement integer (byte) from the byte buffer.
    //  * @return Unsigned integer value.
    //  */
    // public int getUnsignedShortFromBytes(int i, ByteBuffer b) {
    //     return (b.getShort(i) & 0xFFFF);
    // }

    // /**
    //  * Method to obtain an unsigned integer from a signed 2s complement integer (byte) from the byte buffer.
    //  * @return Unsigned integer value.
    //  */
    // public int getUnsignedIntFromBytes(int i, ByteBuffer b) {
    //     return (b.getInt(i) & 0xFFFFFFFF);
    // }


    /**
     * Method to obtain the size of the file in bytes.
     * @return File size in bytes.
     */
    public int size() {
        return this.vol.getFileInBytes().length;
    }
}