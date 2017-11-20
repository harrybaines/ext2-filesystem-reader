package coursework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Directory extends DataBlock {
    
    private ByteBuffer directoryByteBuffer;
    private int[] iNodeTablePointers;
    private SuperBlock superBlock;

    public Directory(ByteBuffer directoryByteBuffer, int[] iNodeTablePointers, SuperBlock superBlock) {
        super(superBlock.getVolume());
        this.directoryByteBuffer = directoryByteBuffer;
        this.iNodeTablePointers = iNodeTablePointers;
        this.superBlock = superBlock;
    }

    /**
     * Method to retrieve an array of strings - each string represents a directory/file in the listing.
     * Each directory contains relevant information to that directory.
     * The output is presented in a Unix-like format.
     *
     * @return An array of directory strings.
     */
    public String getFileInfo() {

        // Prints row/entry of a directory referenced by iNode 2
        String directoryString = "";
        int currentLength = 0;

        while (currentLength < directoryByteBuffer.limit()) {

            // Add next 'row' to directory string
            directoryString += this.getRowAsString(currentLength);

            // Add length to find next entry 'row'
            currentLength += this.getShortFromBytes(currentLength + 4, directoryByteBuffer);
        }
        
        System.out.println("----------");
        System.out.println("Directory Listing for Root Directory (using iNode 2):");
        System.out.println("----------");
        System.out.println(directoryString+"\n");

        return directoryString;
    }

    public INode getINodeFromRow(int offset) {
        int iNodeNumber = this.getIntFromBytes(offset, directoryByteBuffer);

        int tablePointerNum = iNodeNumber / superBlock.getiNodesPerGroup();

        return (new INode(iNodeNumber, iNodeTablePointers[tablePointerNum], tablePointerNum, superBlock));
    }

    public String getRowAsString(int offset) {

        INode currentINode = getINodeFromRow(offset);
        currentINode.getINodeInfoBytes();

        System.out.println("length: " + this.getShortFromBytes(offset + 4, directoryByteBuffer));
        System.out.println("name len: " + this.getByte(offset + 6, directoryByteBuffer));
        System.out.println("file type: " + this.getByte(offset + 7, directoryByteBuffer));
        

        // Obtain file name given the filename length
        byte[] filenameBytes = new byte[this.getByte(offset + 6, directoryByteBuffer)];
        for (int i = 0; i < filenameBytes.length; i++) {
            filenameBytes[i] = this.getByte((offset + (8+i)), directoryByteBuffer);
        }
        String filenameString = new String(filenameBytes);
        System.out.println("filename: " + filenameString);
        System.out.println("----------\n");

        // Obtain user ID (root etc.)
        String users = "";
        users += (currentINode.getUserID() == 0) ? "root " : Integer.toString(currentINode.getUserID()) + " ";   // User ID of owner
        users += (currentINode.getGroupID() == 0) ? "root" : Integer.toString(currentINode.getGroupID());  // Group ID of owner

        // Obtain file name given the filename length
        byte[] fileNameBytes = new byte[this.getByte(6 + offset,  directoryByteBuffer)];
        for (int i = 0; i < fileNameBytes.length; i++) {
            fileNameBytes[i] = this.getByte(8+i+offset, directoryByteBuffer);
        }
        String filenameStr = new String(fileNameBytes);

        // Unix-style directory listing for iNode 2
        String rowString = currentINode.getFileModeAsString() + " " + users + " " + Integer.toString(currentINode.getNumHardLinks()) + " " 
                            + Integer.toString(currentINode.getLowerFileSize()) + " " + currentINode.getLastModifiedTime() + " " + filenameStr + "\n"; 

        return rowString;
    }
}