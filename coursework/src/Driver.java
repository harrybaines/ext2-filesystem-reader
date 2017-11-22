package coursework;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Driver {
    
    public Driver() {

        // API user will use
        Volume vol = new Volume("ext2fs");
        Ext2File file = new Ext2File(vol, "/deep/down/in/the/filesystem/there/lived/");

        // // Prints directory contents of the file passed in
        // Directory d = new Directory(file);
        // d.printDirectoryInfo();

        // NEED TO CHANGE
        byte buf[] = file.readFile(0L, 2048);
        System.out.format ("%s\n", new String(buf)); 
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