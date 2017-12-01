package coursework;

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
    
    private Ext2File file;                  /* Reference to the file - would print directory contents for this file once instance is created */

    private ByteBuffer dirDataBuffer;       /* Buffer to store bytes in this directory */
    private SuperBlock superBlock;          /* Stores a reference to the super block for file system information */

    private boolean fileFoundInDirectory;   /* Boolean to check if a file was found in this directory */
    private INode nextINode;                /* Stores the iNode of the next file in this directory */

    /**
     * Constructor to initialise a directory with a given file and initialise relevant instance variables.
     * @param file The file the user wishes to view the directory listing for.
     */
    public Directory(Ext2File file) {
        super(file.getVolume());
        this.file = file;
        this.dirDataBuffer = file.getDirDataBuffer();
        this.superBlock = file.getVolume().getSuperblock();
        this.fileFoundInDirectory = false;
    }

    /**
     * Method to retrieve an array of strings - each string represents a directory/file in the listing.
     * Each directory contains relevant information to that directory.
     * The output is presented in a Unix-like format.
     *
     * @return An array of directory strings.
     */
    public List<String> getFileInfo() {

        // Create array of strings to store individual 'row' strings
        int currentLength = 0;

        List<String> directoryStrings = new ArrayList<String>();

        while (currentLength < dirDataBuffer.limit()) {

            // Add next 'row' to directory string
            directoryStrings.add(this.getDirRowAsString(currentLength));

            // Add length to find next entry 'row'
            currentLength += this.getShortFromBytes(currentLength + 4, dirDataBuffer);
        }
        return directoryStrings;
    }


    public String getDirRowAsString(int offset) {

        // Obtain iNode information at current row
        INode currentINode = getINodeFromRow(offset);

        byte[] INodeBytes = currentINode.getINodeInfoBytes();

        // Obtain file name given the filename length
        byte[] filenameBytes = new byte[this.getByte(offset + 6, dirDataBuffer)];
        for (int i = 0; i < filenameBytes.length; i++) {
            filenameBytes[i] = this.getByte((offset + (8+i)), dirDataBuffer);
        }
        String filenameString = new String(filenameBytes);

        // Find the file you're supposed to search for in the current directory listing
        if (filenameString.equals(file.getNextDirectoryString()))
            this.nextINode = currentINode;

        // Obtain user ID (root etc.)
        String users = "";
        users += (currentINode.getUserID() == 0) ? "root  " : Integer.toString(currentINode.getUserID()) + "  ";   // User ID of owner
        users += (currentINode.getGroupID() == 0) ? "root" : Integer.toString(currentINode.getGroupID());         // Group ID of owner

        // Obtain file name given the filename length
        byte[] fileNameBytes = new byte[this.getByte(6 + offset,  dirDataBuffer)];
        for (int i = 0; i < fileNameBytes.length; i++) {
            fileNameBytes[i] = this.getByte(8+i+offset, dirDataBuffer);
        }
        String filenameStr = new String(fileNameBytes);

        // Unix-style directory listing for a given iNode
        String rowString = currentINode.getFileModeAsString() + "  " + String.format("%2s", Integer.toString(currentINode.getNumHardLinks())) + "  " + users + "  " 
                            + String.format("%12s", Long.toString(currentINode.getTotalFileSize())) + "  " + currentINode.getLastModifiedTime() + " " + filenameStr + "\n"; 

        return rowString;
    }

    public INode getINodeFromRow(int offset) {

        int iNodeNumber = this.getIntFromBytes(offset, dirDataBuffer);

        int tablePointerIndex = getTablePointerForiNode(iNodeNumber, superBlock.getiNodesPerGroup(), superBlock.getTotaliNodes());

        return (new INode(iNodeNumber, superBlock.getiNodeTablePointers()[tablePointerIndex], tablePointerIndex, superBlock));
    }

    /** 
     * Checks if a file was found successfully in the directory currently being searched in.
     * @return true if a file was found in this directory.
     */
    public boolean fileWasFoundInDirectory() {
        return this.fileFoundInDirectory;
    }

    /**
     * Returns the iNode of the next row in the directory listing.
     * @return The iNode instance of the next row.
     */
    public INode getNextINode() {
        return this.nextINode;
    }
}