package ext2;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
    private ByteBuffer byteBuffer;      /* Byte buffer to store all bytes in this volume */
    private SuperBlock superBlock;      /* Super block reference containing all info about the file system in this volume */

    /** 
     * Constructor used to open a file given a file path to that file.
     * @param filePath The file path to the volume.
     */
    public Volume(String filePath) { 

        try {
            if (this.openVolume(filePath))
                System.out.println("----------\nVolume opened successfully.\n----------");
        } catch (IOException f) {
            System.out.println("----------\nCouldn't find/open the file.\n----------");
            System.exit(0);
        }

        // Wrap existing volume byte array to byte buffer
        byteBuffer = ByteBuffer.wrap(this.fileInBytes);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Create new super block instance
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
     * Returns the byte buffer which stores this entire volume, in bytes.
     * @return The byte buffer of bytes in this volume.
     */
    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }   
}