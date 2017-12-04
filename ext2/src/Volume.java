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
    
    private RandomAccessFile file;                /* The file that represents the volume the user wishes to open */
    private byte[] fileInBytes;                   /* Array of bytes to store the entire volume */
    private ByteBuffer byteBuffer;                /* Byte buffer to store all bytes in this volume */
    private SuperBlock superBlock;                /* Super block reference containing all info about the file system in this volume */
    
    private GroupDescriptor[] groupDescriptors;   /* Array of all group descriptors in this volume */
    private int[] iNodeTablePointers;             /* Array of all the iNode table pointers - the super block fields aid in it's construction */

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
        this.iNodeTablePointers = this.getAllINodeTblPointers();
    }

    /**
     * Opens a given volume given a filename into an array of bytes and returns true if successful.
     * @param filePath The file path to the volume as a string.
     * @throws IOException if file couldn't be opened.
     * @return true if the file was successfully opened, false otherwise.
     */
    public boolean openVolume(String filePath) throws IOException {
        this.file = new RandomAccessFile(filePath, "r");
        this.fileInBytes = new byte[(int) this.file.length()];
        this.file.readFully(this.fileInBytes);
        return true;
    } 

    /**
     * Method to obtain the group number the iNode belongs to and the index in the array of iNode table pointers.
     * The correct iNode table pointer can then be used to find the iNode required.
     * E.g. A table index of 2 indicates the iNode exists in block group 2 and index 2 of the iNode table pointers is the table pointer to use.
     *
     * @param iNodeNumber The iNode number.
     * @param iNodesPerGroup The total number of iNodes per group.
     * @param totaliNodes The total number of iNodes in the filesystem.
     * @return The table index (group number and index in array of iNode table pointers).
     */
    public int getTablePointerForiNode(int iNodeNumber, int iNodesPerGroup, int totaliNodes) {
        int tableIndex = 0;
        for (int i = iNodesPerGroup; i <= totaliNodes; i += iNodesPerGroup) {
            if (iNodeNumber <= i)
                break;
            tableIndex++;
        }
        return tableIndex;
    }

    /**
     * Retrieves an array of all iNode table pointers from the group descriptors.
     * The method uses block group 0 to access all group descriptors and uses relevant fields in this class to find the pointers.
     *
     * @return The array of all integer iNode table pointers.
     */
    private int[] getAllINodeTblPointers() {

        // Obtains array of iNode table pointers from all group descriptors
        int numBlockGroups = (int) Math.ceil((double) this.superBlock.getTotalBlocks() / (double) this.superBlock.getBlocksPerGroup());

        // Array of iNode table pointers
        int[] iNodeTablePointers = new int[numBlockGroups];

        // Finds the group descriptors using block group 0 (1024 bytes after superblock)
        byte[] groupDescBytes = this.superBlock.readBlock(2 * this.superBlock.getBlockSize(), numBlockGroups * GroupDescriptor.GROUP_DESCRIPTOR_SIZE);
        ByteBuffer groupDescBuffer = ByteBuffer.wrap(groupDescBytes);
        groupDescBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int currentDesc = 0;
        groupDescriptors = new GroupDescriptor[numBlockGroups];

        // Create new GroupDescriptor instances to obtain iNode table pointers
        while (currentDesc < numBlockGroups) {
            groupDescriptors[currentDesc] = new GroupDescriptor(groupDescBuffer, currentDesc, this.superBlock);
            iNodeTablePointers[currentDesc] = groupDescriptors[currentDesc].getINodeTblPointer();
            currentDesc++;
        }
        return iNodeTablePointers;
    }

    /**
     * Obtains the list of all group descriptors in this volume.
     * @return List of group descriptors.
     */
    public GroupDescriptor[] getGroupDescriptors() {
        return this.groupDescriptors;
    }

    /**
     * Obtains the list of all iNode table pointers found in the group descriptors.
     * @return List of iNode table pointers.
     */
    public int[] getiNodeTablePointers() {
        return this.iNodeTablePointers;
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