package coursework;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Driver {

    private RandomAccessFile file;
    private byte[] fileInBytes;

	public Driver() {

        System.out.println(ByteOrder.nativeOrder());

		try {
			file = new RandomAccessFile("ext2fs", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}  

        // Read text file into byte array
        try {

            // Array of 8-bit signed 2s complement integers 
            fileInBytes = new byte[(int)file.length()];
            file.readFully(fileInBytes);

            // Wrap existing byte array to byte buffer
            ByteBuffer byteBuffer = ByteBuffer.wrap(fileInBytes);
            
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);


            // Obtain block (1024 byte size)
            byte[] block = this.read(1024*2, 1024);
            ByteBuffer byteBlockBuffer = ByteBuffer.wrap(block);

            System.out.print("inode: ");
            int iNode = byteBlockBuffer.getInt();
            System.out.println(iNode);

            System.out.print("length: ");
            short length = byteBlockBuffer.getShort();
            System.out.println(length);

            System.out.print("name len: ");
            byte nameLen = byteBuffer.get();
            System.out.println(nameLen);

            System.out.print("file type: ");
            byte fileType = byteBuffer.get();
            System.out.println(fileType);

            System.out.print("filename: ");
            for (int i = 8; i < block.length; i++)
                System.out.print(block[i]);
            System.out.print("\n");



        } catch (IOException e) {
            e.printStackTrace();
        }

	}

    /**
     * Reads at most length bytes starting at byte offset startByte from start of file.
     * Byte 0 is first byte in the file.
     *
     * @param startByte The byte to begin reading from in the file.
     * @param length The total number of bytes to read.
     * @return The array of bytes from the file.
     */
    private byte[] read(long startByte, long length) {

        byte[] specifiedBytes = new byte[(int)length];

        for (int curByte = 0; curByte < length; curByte++) {

            System.out.println("Reading byte: " + curByte + " at byte offset: " + (startByte));
            specifiedBytes[curByte] = fileInBytes[(int)startByte];
            startByte++;
            System.out.println(specifiedBytes[curByte]);

        }

        return specifiedBytes;
    } 

    /**
     * Main method to begin the driver program.
     * @param args Unused.
     */
    public static void main(String[] args) {
        Driver d = new Driver();
    }

}