package coursework;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Driver {

	private static final int BLOCK_SIZE = 1024;
	private RandomAccessFile file;
	private byte[] fileInBytes;
	private ByteBuffer byteBuffer;

	public Driver() {

		try {
			file = new RandomAccessFile("ext2fs", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read text file into byte array
		try {
			// Array of 8-bit signed 2s complement integers
			fileInBytes = new byte[(int) file.length()];
			file.readFully(fileInBytes);

			// Wrap existing byte array to byte buffer
			byteBuffer = ByteBuffer.wrap(fileInBytes);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			// Obtain superblock (1024 byte size)
			byte[] block = this.read(1 * BLOCK_SIZE, BLOCK_SIZE);
			ByteBuffer byteBlockBuffer = ByteBuffer.wrap(block);
			byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
			
			// Obtain hex and ASCII values of bytes
			Helper.dumpHexBytes(block);
			Helper.printSuperblockInfo(byteBlockBuffer);

            // Finds the iNode table pointer from group descriptor
			block = this.read(2 * BLOCK_SIZE, 32);
			byteBlockBuffer = ByteBuffer.wrap(block);
			byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
			int iNodeTblPointer1 = byteBlockBuffer.getInt(8);
			System.out.println("Group Descriptor: ");
            Helper.dumpHexBytes(block);
            System.out.println("iNodeTblPointer: " + iNodeTblPointer1 + " (Block number of first inode table block)\n");

            // Block containing first iNode - block number of first iNode table block
            block = this.read(iNodeTblPointer1 * BLOCK_SIZE + (128), 128);
            byteBlockBuffer = ByteBuffer.wrap(block);
            byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int pointerToFirstDataBlock = byteBlockBuffer.getInt(40);
            System.out.println("Block containing first iNode: ");
            Helper.dumpHexBytes(block);
            System.out.println("Pointer to first data block (from iNode 2): " + pointerToFirstDataBlock + "\n");

            // Accessing data block in file referenced by iNode 2
            block = this.read(pointerToFirstDataBlock * BLOCK_SIZE, 1024);
            byteBlockBuffer = ByteBuffer.wrap(block);
            byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
            System.out.println("Data block found using iNode 2 pointer: ");
            Helper.dumpHexBytes(block);

            // Prints row/entry of a directory
            int currentLength = 0;

            while (currentLength < BLOCK_SIZE) {

                System.out.println("----------");
                System.out.println("iNode number: " + byteBlockBuffer.getInt(currentLength + 0));
                System.out.println("length: " + byteBlockBuffer.getShort(currentLength + 4));
                System.out.println("name len: " + byteBlockBuffer.get(currentLength + 6));
                System.out.println("file type: " + byteBlockBuffer.get(currentLength + 7));

                byte[] filenameBytes = new byte[byteBlockBuffer.getShort(currentLength + 4) - 6];
                for (int i = 0; i < byteBlockBuffer.get(currentLength + 6); i++) {
                    filenameBytes[i] = byteBlockBuffer.get(currentLength + (8+i));
                }
                String filenameString = new String(filenameBytes);
                System.out.println("filename: " + filenameString);
                System.out.println("----------\n");

                currentLength += byteBlockBuffer.getShort(currentLength + 4);

            }

            





		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads at most length bytes starting at byte offset startByte from start of
	 * file. Byte 0 is first byte in the file.
	 *
	 * @param startByte The byte to begin reading from in the file.
	 * @param length The total number of bytes to read.
	 * @return The array of bytes from the file.
	 */
	private byte[] read(long startByte, long length) {

		byte[] specifiedBytes = new byte[(int) length];

		for (int curByte = 0; curByte < length; curByte++) {

			specifiedBytes[curByte] = byteBuffer.get((int) startByte);
			startByte++;

		}
		return specifiedBytes;
	}

	/**
	 * Main method to begin the driver program.
	 * 
	 * @param args Unused.
	 */
	public static void main(String[] args) {
		new Driver();
	}
}