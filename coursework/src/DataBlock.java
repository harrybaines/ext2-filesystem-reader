package coursework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataBlock {
	
    private Volume vol;                 // Volume reference which this file is part of
    private ByteBuffer byteBuffer;

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

            specifiedBytes[curByte] = this.byteBuffer.get((int) startByte); // ******
            startByte++;

        }
        return specifiedBytes;
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

    // /**
    //  * Method to obtain an unsigned integer from a signed 2s complement integer (byte) from the byte buffer.
    //  * @return Unsigned integer value.
    //  */
    // public int getUnsignedByte(int i, ByteBuffer b) {
    //     return (b.get(i) & 0xFF);
    // }

    // /**
    //  * Method to obtain an unsigned integer from a signed 2s complement integer (byte) from the byte buffer.
    //  * @return Unsigned integer value.
    //  */
    // public int getUnsignedIntFromBytes(int i, ByteBuffer b) {
    //     return (b.getInt(i) & 0xFFFFFFFF);
    // }

    // /**
    //  * Method to obtain an unsigned short from a signed 2s complement integer (byte) from the byte buffer.
    //  * @return Unsigned integer value.
    //  */
    // public int getUnsignedShortFromBytes(int i, ByteBuffer b) {
    //     return (b.getShort(i) & 0xFFFF);
    // }

    /**
     * Method to obtain the size of the file in bytes.
     * @return File size in bytes.
     */
    public int size() {
        return 1;
    }


}