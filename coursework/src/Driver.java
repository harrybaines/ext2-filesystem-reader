package coursework;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Driver {
    
    public Driver() {

        // API user will use
        Volume vol = new Volume("ext2fs");
        Ext2File file = new Ext2File(vol, "/files/two-cities.txt");
        //byte buf[ ] = f.read(0L, f.size);
        //System.out.format ("%s\n", new String(buf)); 
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