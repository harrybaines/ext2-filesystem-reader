package coursework;

import java.io.*;

public class Volume {
    
    private RandomAccessFile file;      // The file that represents the volume the user wishes to open
    private byte[] fileInBytes;

    /** 
     * Constructor used to open a file given a file path to that file.
     * @param filePath The file path to the volume.
     */
    public Volume(String filePath) { 
        this.openVolume(filePath);
    }

    /**
     * Opens a given volume given a filename into an array of bytes and returns 1 if successful.
     * @param filePath The file path to the volume as a string.
     * @return 1 if the file was successfully opened, 0 otherwise.
     */
    public int openVolume(String filePath) {
        try {
            file = new RandomAccessFile(filePath, "r");
            fileInBytes = new byte[(int) file.length()];
            file.readFully(fileInBytes);
            return 1;
        } catch (FileNotFoundException f) {
            f.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }   

    public byte[] getFileInBytes() {
        return this.fileInBytes;
    }     
}