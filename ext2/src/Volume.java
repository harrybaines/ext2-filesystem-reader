package ext2;

import java.io.RandomAccessFile;
import java.io.IOException;

/**
 * Name: Volume
 * 
 * This class represents a volume the user wishes to open and read from.
 * This class provides methods for initialising various fields that are contained in the file the user wishes to open,
 * such as the superblock, group descriptor fields etc.
 * If the volume is opened successfully a success message is printed, otherwise an error message is printed.
 *
 * @author Harry Baines
 */
public class Volume {
    
    private RandomAccessFile file;      /* The file that represents the volume the user wishes to open */
    private byte[] fileInBytes;         /* Array of bytes to store the entire volume */
    private SuperBlock superBlock;

    /** 
     * Constructor used to open a file given a file path to that file.
     * @param filePath The file path to the volume.
     */
    public Volume(String filePath) { 
        try {
            if (this.openVolume(filePath))
                System.out.println("----------\nVolume opened successfully.\n----------");
        } catch (IOException f) {
            System.out.println("Couldn't find and open the file.");
            System.exit(0);
        }

        this.superBlock = new SuperBlock(this);
    }

    /**
     * Opens a given volume given a filename into an array of bytes and returns true if successful.
     * @param filePath The file path to the volume as a string.
     * @return true if the file was successfully opened, false otherwise.
     */
    public boolean openVolume(String filePath) throws IOException {
        this.file = new RandomAccessFile(filePath, "r");
        this.fileInBytes = new byte[(int) this.file.length()];
        this.file.readFully(this.fileInBytes);
        return true;
    } 

    /**
     * Obtains the reference to the superblock.
     * @return The superblock instance.
     */
    public SuperBlock getSuperblock() {
        return this.superBlock;
    }  

    /**
     * Returns the entire volume in the form of an array of bytes.
     * @return Array of bytes containing the volume.
     */
    public byte[] getFileInBytes() {
        return this.fileInBytes;
    }     
}