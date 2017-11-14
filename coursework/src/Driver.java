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
            
            byte[] byteArray = new byte[2048];

            byteBuffer.get(byteArray, 0, 2048);

            this.read(1023, 1024);

            // for (byte b : byteArray)
            //     System.out.println(b);

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

        int offset = (int) startByte;

        for (int curByte = 0; curByte < length; curByte++) {

            offset++;
            System.out.println("Reading byte: " + curByte + " at byte offset: " + offset);
            specifiedBytes[curByte] = fileInBytes[(int)offset];
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