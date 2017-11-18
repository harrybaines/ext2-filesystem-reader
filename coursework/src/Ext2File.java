package coursework;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Ext2File extends DataBlock {

    private String fileString;          // Filename of the file the user wishes to open from the volume

    /**
     * Constructor used to represent a file in the given volume.
     * The file can then be read and output to the user and can also view important info.
     *
     * @param vol The volume represented by the file opened.
     * @param fileString The name of the file to open in the volume.
     */
    public Ext2File(Volume vol, String fileString) {
        super(vol);
        this.fileString = fileString;  
    }
}