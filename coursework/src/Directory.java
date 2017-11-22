package coursework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Directory extends DataBlock {
    
    private Ext2File file;

    private ByteBuffer dirDataBuffer;
    private int[] iNodeTablePointers;
    private SuperBlock superBlock;

    private boolean fileFoundInDirectory;

    private INode nextINode;

    public Directory(Ext2File file) {
        super(file.getVolume());
        this.file = file;
        this.dirDataBuffer = file.getDirDataBuffer();
        this.iNodeTablePointers = file.getiNodeTablePointers();
        this.superBlock = file.getSuperblock();
        this.fileFoundInDirectory = false;
    }

    public boolean fileWasFoundInDirectory() {
        return this.fileFoundInDirectory;
    }

    public INode getNextINode() {
        return this.nextINode;
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
            directoryStrings.add(this.getRowAsString(currentLength));

            // Add length to find next entry 'row'
            currentLength += this.getShortFromBytes(currentLength + 4, dirDataBuffer);

        }
        return directoryStrings;
    }

    public void printDirectoryInfo() {

        List<String> directoryStrings = getFileInfo();
        
        System.out.println("\n----------");
        System.out.println("Directory Listing for Directory (using iNode "+nextINode.getINodeNumber()+"):");
        System.out.println("----------");
        for (String row : directoryStrings)
            System.out.println(row);
        System.out.println("----------\n");

    }


    public String getRowAsString(int offset) {

        // Obtain iNode information at current row
        INode currentINode = getINodeFromRow(offset);

        System.out.println("Currently looking at " + currentINode.getINodeNumber() + " in the directory listing.");
        currentINode.getINodeInfoBytes();

        // System.out.println("Current iNode: " + currentINode.getINodeNumber());

        // System.out.println("length: " + this.getShortFromBytes(offset + 4, dirDataBuffer));
        // System.out.println("name len: " + this.getByte(offset + 6, dirDataBuffer));
        // System.out.println("file type: " + this.getByte(offset + 7, dirDataBuffer));
        

        // Obtain file name given the filename length
        byte[] filenameBytes = new byte[this.getByte(offset + 6, dirDataBuffer)];
        for (int i = 0; i < filenameBytes.length; i++) {
            filenameBytes[i] = this.getByte((offset + (8+i)), dirDataBuffer);
        }
        String filenameString = new String(filenameBytes);
        // System.out.println("filename: " + filenameString);
        // System.out.println("----------");

        // Find the file you're supposed to search for in the current directory listing
        if (filenameString.equals(file.getNextDirectoryString()))
            this.nextINode = currentINode;

        // Obtain user ID (root etc.)
        String users = "";
        users += (currentINode.getUserID() == 0) ? "root " : Integer.toString(currentINode.getUserID()) + " ";   // User ID of owner
        users += (currentINode.getGroupID() == 0) ? "root" : Integer.toString(currentINode.getGroupID());        // Group ID of owner

        // Obtain file name given the filename length
        byte[] fileNameBytes = new byte[this.getByte(6 + offset,  dirDataBuffer)];
        for (int i = 0; i < fileNameBytes.length; i++) {
            fileNameBytes[i] = this.getByte(8+i+offset, dirDataBuffer);
        }
        String filenameStr = new String(fileNameBytes);

        // Unix-style directory listing for iNode 2
        String rowString = currentINode.getFileModeAsString() + " " + users + " " + Integer.toString(currentINode.getNumHardLinks()) + " " 
                            + Integer.toString(currentINode.getLowerFileSize()) + " " + currentINode.getLastModifiedTime() + " " + filenameStr; 

        return rowString;
    }

    public INode getINodeFromRow(int offset) {

        int iNodeNumber = this.getIntFromBytes(offset, dirDataBuffer);

        int tablePointerIndex = getTablePointerForiNode(iNodeNumber, superBlock.getiNodesPerGroup(), superBlock.getTotaliNodes());

        return (new INode(iNodeNumber, iNodeTablePointers[tablePointerIndex], tablePointerIndex, superBlock));
    }
}