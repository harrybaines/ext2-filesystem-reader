package coursework;

import java.util.List;
import java.util.ArrayList;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Title: Ext2File
 *
 * This class represents a file within a given volume.
 * For the ext2 filesystem, this class represents a file within the filesystem.
 * This class is an extension of the DataBlock which provides further functionality for reading blocks and obtaining bytes.
 *
 * @author Harry Baines
 * @see DataBlock
 */
public class Ext2File extends DataBlock {

    private String filePathString;      /* The full file path string to this file */
    private String fileName;            /* File name string for this ext2 file */
    private Directory dir;              /* Stores a reference to the directory this file is stored in */

    private ByteBuffer dirDataBuffer;   /* Buffer to store all bytes relevant to the current directory */
    private SuperBlock superBlock;      /* Stores a reference to the super block for file system information */

    private String curDirString;        /* Stores the name of the current directory under consideration */
    private int charCount;              /* Used to split the current directory string into parts */

    private INode iNodeForFileToOpen;   /* Stores the iNode for the file which is to be opened */
    private boolean isDirectory;        /* Boolean which checks if the file is a directory or a regular file */

    /**
     * Constructor used to represent a file in the given volume and initialise relevant instance variables.
     * The file can then be read and output to the user and can also view important info.
     *
     * @param vol The volume represented by the file opened.
     * @param filePathString The name of the file to open in the volume.
     */
    public Ext2File(Volume vol, String filePathString) {
        super(vol);
        this.filePathString = filePathString;

        charCount = 0;
        superBlock = vol.getSuperblock();

        // Open this new file
        this.openFile();
    }

    /** 
     * Method which attempts to open a file specified by the user in the fileString.
     * If successful, the user will be notified the file was successfully opened.
     *
     * @return True if the file was successfully opened, false otherwise.
     */
    public void openFile() {

        // Get directory bytes pointed to by iNode 2 direct pointer
        byte[] rootDataBlocks = getDirBytes(2);
        dirDataBuffer = ByteBuffer.wrap(rootDataBlocks);    
        dirDataBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Finds the directory specified in the file path string
        while ((curDirString = getCurDirectoryString()) != "") {

            dir = new Directory(this);
            dir.getFileInfo();

            // Check to see if current directory using the file path string actually exists!
            INode nextINode = dir.getNextINode();

            // INode exists and points to a directory
            if (nextINode != null && nextINode.getFileModeAsString().charAt(0) != '-') {
                rootDataBlocks = getDirBytes(dir.getNextINode().getINodeNumber());
                dirDataBuffer = ByteBuffer.wrap(rootDataBlocks);
                dirDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
            }
            // INode exists and points to a file
            else if (nextINode != null)
                iNodeForFileToOpen = nextINode;
        }   
    }

    public void printDirectoryInfo() {
        
        System.out.println("\n--------------------");
        System.out.println("Directory Listing for " + getFileString() + ":\n");

        // Print out directory contents of a directory
        if (iNodeForFileToOpen != null && iNodeForFileToOpen.getFileModeAsString().charAt(0) == 'd') {
            for (String row : dir.getFileInfo())
                System.out.println(row);
        }
        // If not a directory, print contents of directory the file exists in
        else {
            Directory fileDir = new Directory(this);
            for (String row : fileDir.getFileInfo())
                System.out.println(row);
        }
        System.out.println("--------------------\n");
    }

    /** 
     * Method to obtain an array containing the bytes in the current directory under consideration.
     * @param iNodeNumber The iNode number which references the directory.
     * @return The array of bytes in the current directory.
     */
    public byte[] getDirBytes(int iNodeNumber) {

        // Obtains all bytes relevant to a given
        int tablePointerIndex = getTablePointerForiNode(iNodeNumber, superBlock.getiNodesPerGroup(), superBlock.getTotaliNodes());
        INode iNode = new INode(iNodeNumber, superBlock.getiNodeTablePointers()[tablePointerIndex], tablePointerIndex, superBlock);
        return iNode.getDataBlocksFromPointers();
    }

    /**
     * Simple method to print the contents of a file in ASCII format.
     * A string is returned containing the full file contents.
     *
     * @param bytes The array of bytes containing file info to be 
     * @return The string of characters in the file.
     */
    public void printFileContents(byte[] bytes, String name) {

        String fileContentsString = "";
        if (bytes.length == 0)
            fileContentsString += "--- nothing found ---";
        else
            fileContentsString += "\nFile Contents for '" + name + "':\n--------------------\n" + new String(bytes);

        System.out.println(fileContentsString + "\n"); 
    }

    /**
     * Method to read the file the user specified in the file string.
     * The method will read bytes from startByte up to the length they provide.
     * An array of bytes will be returned containing all bytes in the file.
     *
     * @param startByte The byte to start reading from in the file.
     * @param length The length of the file the user wishes to read in bytes.
     * @return An array of bytes relevant to the file opened.
     */
    public byte[] readFile(long startByte, long length) {

        if (length < 0 || startByte < 0)
            return new byte[0];

        byte[] byteArray = new byte[(int)length];

        if (iNodeForFileToOpen != null) {

            // Find and read all datablocks for the file, if found successfully
            int iNodeNumber = iNodeForFileToOpen.getINodeNumber();
            int tablePointerIndex = getTablePointerForiNode(iNodeNumber, this.getVolume().getSuperblock().getiNodesPerGroup(), this.getVolume().getSuperblock().getTotaliNodes());
            INode iNode = new INode(iNodeNumber, this.getVolume().getSuperblock().getiNodeTablePointers()[tablePointerIndex], tablePointerIndex, superBlock);

            // Obtain iNode for all direct pointers if they exist
            List<Byte> dataBytes = new ArrayList<Byte>();


            byte[] dataBlocksFromPointers = iNodeForFileToOpen.getDataBlocksFromPointers();

            // Transfer all found bytes from data blocks array into buffer
            for (byte b : dataBlocksFromPointers)
                dataBytes.add(b);

            for (int i = (int)startByte; i < byteArray.length; i++) {
                if (i >= dataBlocksFromPointers.length)
                    break;
                byteArray[i] = dataBlocksFromPointers[i];
            }

        }
        else {
            System.out.println("File not found/selected - unable to open file specified!");
            byteArray = new byte[0];
        }

        return byteArray;
    }

    /**
     * Method to obtain the current directory name under consideration as a string.
     * @return The current directory name as a string.
     */
    public String getCurDirectoryString() {

        // Obtain individual directory names from filePathString
        curDirString = "";
        String fileString = getFileString();
        int slashCount = 0;

        // Iterate over the fileString to obtain individual directory names
        while (true) {

            if (charCount >= fileString.length()) 
                break;

            // Break if a forward slash is met
            if (fileString.charAt(charCount) == '/') {
                slashCount++;
                if (slashCount == 2)
                    break;
            }

            // Append character to current directory name string
            if (fileString.charAt(charCount) != '/')
                curDirString += fileString.charAt(charCount);

            charCount++;
        }
        this.fileName = curDirString;
        return curDirString;
    }

    /**
     * Returns the size of this ext2 file in bytes.
     * @return The file size.
     */
    public long size() {
        return this.iNodeForFileToOpen.getTotalFileSize();
    }

    /**
     * Returns the name of this ext2 file as a string.
     * @return The name of this file.
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Obtains the byte buffer for the current directory.
     * @return Byte buffer reference.
     */
    public ByteBuffer getDirDataBuffer() {
        return this.dirDataBuffer;
    }

    /**
     * Obtains the filename string the user passed in and wishes to open.
     * @return Filename string.
     */
    public String getFileString() {
        return this.filePathString;
    }

    /**
     * Obtains the name of the next directory to search in as a string.
     * @return The directory name as a string.s
     */
    public String getNextDirectoryString() {
        return this.curDirString;
    }
}