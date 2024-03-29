package ext2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.ArrayList;

/** 
 * Name: Directory
 * 
 * This class represents a directory in a given volume.
 * In this example in the ext2 filsystem, directories are treated as files, hence a reference to the file in this directory is stored.
 * The user can choose to output the directory listing for a given file/directory.
 *
 * @author Harry Baines
 * @see DataBlock
 */
public class Directory extends DataBlock {

    public static final int INODE_LENGTH    = 4;    /* The length of an iNode in a directory listing in bytes */
    public static final int NAME_LEN_OFFSET = 6;    /* The offset to find the namelen field in a directory listing */
    public static final int FILENAME_OFFSET = 8;    /* The offset to find the filename in a directory listing */
    
    private Ext2File file;                          /* Reference to the file - would print directory contents for this file once instance is created */

    private ByteBuffer dirDataBuffer;               /* Buffer to store bytes in this directory */
    private SuperBlock superBlock;                  /* Stores a reference to the super block for file system information */

    private INode nextINode;                        /* Stores the iNode of the next file in this directory */

    /**
     * Constructor to initialise a directory with a given file and initialise relevant instance variables.
     * @param file The file the user wishes to view the directory listing for.
     */
    public Directory(Ext2File file) {
        super(file.getVolume());
        this.file = file;
        this.dirDataBuffer = file.getDirDataBuffer();
        this.superBlock = file.getVolume().getSuperblock();
    }

    /**
     * Method to retrieve an array of strings - each string represents a directory/file in the listing.
     * Each directory contains relevant information to that directory.
     * The output is presented in a Unix-like format.
     *
     * @return A dynamic list of directory strings.
     */
    public List<String> getFileInfo() {

        // Create dynamic list of strings to store individual 'row' strings
        List<String> directoryStrings = new ArrayList<String>();

        int currentLength = 0;
        while (currentLength < dirDataBuffer.limit()) {

            // Add next 'row' to directory string
            directoryStrings.add(this.getDirRowAsString(currentLength));

            // Add length to find next entry 'row'
            currentLength += dirDataBuffer.getShort(currentLength + INODE_LENGTH);
        }
        return directoryStrings;
    }

    /**
     * Method which returns a String containing a full, single line in a directory listing.
     *
     * @param offset The current offset in the directory listing (see getFileInfo())
     * @return The directory 'row' as a string.
     */
    public String getDirRowAsString(int offset) {

        // Obtain iNode information at current row
        INode currentINode = this.getINodeFromRow(offset);

        // Obtain user ID (root etc.)
        String users = "";
        users += (currentINode.getUserID() == 0) ? "root  " : Integer.toString(currentINode.getUserID()) + "  ";   // User ID of owner
        users += (currentINode.getGroupID() == 0) ? "root" : Integer.toString(currentINode.getGroupID());          // Group ID of owner

        // Obtain file name given the filename length
        byte[] fileNameBytes = new byte[dirDataBuffer.get(offset + NAME_LEN_OFFSET)];
        for (int i = 0; i < fileNameBytes.length; i++)
            fileNameBytes[i] = dirDataBuffer.get(i + offset + FILENAME_OFFSET);
        String filenameStr = new String(fileNameBytes);

        // Find the file you're supposed to search for in the current directory listing
        if (filenameStr.equals(file.getNextDirectoryString()))
            this.nextINode = currentINode;

        String rowString = currentINode.getFileModeAsString() + " " + String.format("%2s", Integer.toString(currentINode.getNumHardLinks())) + "  " + users + "  "; 

        // Check for corruption
        if (currentINode.getTotalFileSize() < 0)
            rowString = "error - corrupted file!\n";
        else
            rowString += String.format("%12s", Long.toString(currentINode.getTotalFileSize())) + "  " + currentINode.getLastModifiedTime() + "  " + filenameStr + "\n"; 

        // Unix-style directory listing for a given iNode
        return rowString;
    }

    /**
     * Method which returns the iNode of a file for a particular 'row' in the directory listing.
     *
     * @param offset The current offset of the 'row' in the listing (see getDirRowAsString())
     * @return The iNode of the file in this 'row'.
     */
    public INode getINodeFromRow(int offset) {

        int iNodeNumber = dirDataBuffer.getInt(offset);

        int tablePointerIndex = this.getVolume().getTablePointerForiNode(iNodeNumber, this.superBlock.getiNodesPerGroup(), this.superBlock.getTotaliNodes());

        return (new INode(iNodeNumber, this.getVolume().getiNodeTablePointers()[tablePointerIndex], tablePointerIndex, this.superBlock));
    }

    /**
     * Returns the iNode of the next file to search for when traversing the file system.
     * @return The iNode instance for the next file to find.
     */
    public INode getNextINode() {
        return this.nextINode;
    }
}