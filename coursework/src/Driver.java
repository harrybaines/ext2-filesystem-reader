package coursework;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Driver {

    private RandomAccessFile file;
    private byte[] fileInBytes;

	public Driver() {

		try {
			file = new RandomAccessFile("hello.txt", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}  

        // Read text file into byte array
        try {

            System.out.println(ByteOrder.nativeOrder());

            // Array of 8-bit signed 2s complement integers 
            fileInBytes = new byte[(int)file.length()];
            file.readFully(fileInBytes);

            ByteBuffer.wrap(fileInBytes);

            for (byte b : fileInBytes)
                System.out.print(b + " "); 
            System.out.print("\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

	}

    /**
     * Main method to begin the driver program.
     * @param args Unused.
     */
    public static void main(String[] args) {
        Driver d = new Driver();
    }

}