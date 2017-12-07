package ext2;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

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
    private Directory dir;              /* Stores a reference to the directory this file is stored in */
    private long position;              /* Reference to the current position in the file (for reading) */

    private ByteBuffer dirDataBuffer;   /* Buffer to store all bytes relevant to the current directory */
    private SuperBlock superBlock;      /* Stores a reference to the super block for file system information */
    private List<String> fileInfoList;  /* Dynamic array of strings to store each 'row' in a directory listing */

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
        
        // Initialise instance variables
        this.position = 0L;
        this.charCount = 0;
        this.fileInfoList = new ArrayList<String>();
        this.superBlock = vol.getSuperblock();

        // Open this new file
        this.openFile();
    }


    /********             /******** 
        ***** API METHODS *****
    /********             ********/

    /**
     * Method to read the file the user specified in the file string.
     * The method will read bytes from startByte up to the length they provide.
     * An array of bytes will be returned containing all bytes in the file.
     * If the file is sparse, a sequence of 0s will be returned alongisde the file data.
     *
     * @param startByte The byte to start reading from in the file.
     * @param length The length of the file the user wishes to read in bytes.
     * @throws IndexOutOfBoundsException
     * @return An array of bytes relevant to the file opened.
     */
    public byte[] read(long startByte, long length) throws IndexOutOfBoundsException {

        // Contains useful info from file
        byte[] joinedArray = new byte[0];

        // If file exists
        if (this.iNodeForFileToOpen != null) {

            if (this.iNodeForFileToOpen.getTotalFileSize() == 0) {
                System.out.println("----------\nEmpty file! \n----------");
                return joinedArray;
            }

            // Error checking, throw exception if necessary
            if (startByte < 0 || startByte >= this.iNodeForFileToOpen.getTotalFileSize()) {
                System.out.println("----------\nCouldn't read data at that position! \n----------");
                throw new IndexOutOfBoundsException();
            }

            // Find and read all datablocks for the file, if found successfully
            byte[] dataBlocksFromPointers = this.iNodeForFileToOpen.getDataBlocksFromPointers();

            // Extract file data
            List<Byte> dataList = new ArrayList<Byte>();

            // Uses length provided to obtain file bytes - adds 0s for holes
            int count = 0;
            while (count < length) {
                if (count >= dataBlocksFromPointers.length || (char) dataBlocksFromPointers[count] == '0')
                    dataList.add((byte) 0);
                else {
                    byte b = dataBlocksFromPointers[count];
                    dataList.add(b);
                }
                count++;
            }
                            
            // Finally, transfer bytes to array to return
            int index = 0;
            joinedArray = new byte[dataList.size()];
            for (byte b : dataList) {
                joinedArray[index] = b;
                index++;
            }
        }
        else
            System.out.println(this.filePathString + " - couldn't read this file - either a directory or doesn't exist.");

        return joinedArray;
    }

    /**
     * Method to read the file the user specified in the file string.
     * This method is the same as the other read method in the API, however only requires a length to read.
     * This method therefore calls the corresponding read method at the current position in the file.
     *
     * @param length The length of the file the user wishes to read in bytes.
     * @throws IndexOutOfBoundsException
     * @return An array of bytes relevant to the file opened.
     */
    public byte[] read(long length) throws IndexOutOfBoundsException {
        if (this.position > this.iNodeForFileToOpen.getTotalFileSize()) {
            System.out.println("----------\nCouldn't read data at that position! \n----------");
            throw new IndexOutOfBoundsException();
        }
        return (this.read(this.position, length));
    }

    /**
     * Method to allow the user to change the position in the file (for reading). 
     * @param position The position in the file the user wishes to move to.
     */
    public void seek(long position) {
        this.position = position;
    }

    /**
     * Method to return the current position in the file (i.e. byte offset from the start of the file).
     * The position will be 0 when the file is first opened and will advance by the number of bytes read
     * with every call to one of the read methods.
     *
     * @return The current position in the file.
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * Returns the size of this ext2 file in bytes.
     * @return The file size.
     */
    public long getSize() {
        return (this.iNodeForFileToOpen != null ? this.iNodeForFileToOpen.getTotalFileSize() : 0);
    }

    /**
     * Method which prints the directory information for this file.
     */
    public void printDirectoryInfo() {
        
        System.out.println("------------------------------------------------------------");
        System.out.println("\033[1mDirectory Listing for " + this.filePathString + ": \033[0m\n");

        // Print out directory contents of a directory
        for (String row : this.getFileInfoList())
            System.out.print(row);
        System.out.println("------------------------------------------------------------");
    }

     /**
     * Simple method to print the contents of a file specified in the byte array in a regular format.
     * @param bytes The array of bytes containing file info to be printed.
     */
    public void printFileContents(byte[] bytes) {

        System.out.println(filePathString + " is " + ((isDirectory == true) ? "a directory." : "a file."));

        String fileContentsString = "----------\n\033[1mFile Contents for '" + filePathString + "':\033[0m\n----------\n";
        if (bytes.length == 0)
            fileContentsString += "----------\nNo file contents for " + this.filePathString + "\n----------\n";
        else if (!isDirectory) {
            String toPrint = "";
            for (byte b : bytes) {
                if (b == 0)
                    fileContentsString += "0";
                else
                    fileContentsString += (char) b;
            }

            fileContentsString += "\n----------\n";
        }
        System.out.println(fileContentsString + "\n"); 
    }


    /********                 /******** 
        ***** FURTHER METHODS *****
    /********                 ********/

    /** 
     * Method which attempts to open a file specified by the user in the fileString.
     * If successful, the user will be notified the file was successfully opened.
     */
    private void openFile() {

        // Get directory bytes pointed to by iNode 2 direct pointers
        byte[] rootDataBlocks = getDirBytes(2);
        dirDataBuffer = ByteBuffer.wrap(rootDataBlocks);    
        dirDataBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Finds the directory specified in the file path string
        while ((curDirString = getCurDirectoryString()) != "") {

            // Traverse sub-directory from the root
            dir = new Directory(this);
            fileInfoList = dir.getFileInfo();

            // Check to see if current directory using the file path string actually exists!
            INode nextINode = dir.getNextINode();

            // INode exists and points to a directory
            if (nextINode != null && nextINode.getFileModeAsString().charAt(0) != '-') {
                rootDataBlocks = getDirBytes(dir.getNextINode().getINodeNumber());
                dirDataBuffer = ByteBuffer.wrap(rootDataBlocks);
                dirDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
                isDirectory = true;
            }
            // INode exists and points to a file - store the iNode for this file for later reference
            else if (nextINode != null) {
                iNodeForFileToOpen = nextINode;
                isDirectory = false;
            }
        }   
    }

    /** 
     * Method to obtain an array containing the bytes in the current directory under consideration.
     * @param iNodeNumber The iNode number which references the directory.
     * @return The array of bytes in the current directory.
     */
    private byte[] getDirBytes(int iNodeNumber) {

        // Obtains all bytes relevant to a given
        int tablePointerIndex = this.getVolume().getTablePointerForiNode(iNodeNumber, superBlock.getiNodesPerGroup(), superBlock.getTotaliNodes());
        INode iNode = new INode(iNodeNumber, this.getVolume().getiNodeTablePointers()[tablePointerIndex], tablePointerIndex, superBlock);
        return iNode.getDataBlocksFromPointers();
    }

    /**
     * Method to obtain the current directory name under consideration as a string.
     * @return The current directory name as a string.
     */
    private String getCurDirectoryString() {

        // Obtain individual directory names from filePathString
        curDirString = "";
        int slashCount = 0;

        // Iterate over the fileString to obtain individual directory names
        while (true) {

            if (charCount >= this.filePathString.length()) 
                break;

            // Break if a forward slash is met
            if (this.filePathString.charAt(charCount) == '/')
                if (++slashCount == 2)
                    break;

            // Append character to current directory name string
            if (this.filePathString.charAt(charCount) != '/')
                curDirString += this.filePathString.charAt(charCount);

            charCount++;
        }
        return curDirString;
    }

    /**
     * Returns the list of strings for all files names in a given directory.
     * @return The list of file name strings.
     */
    public List<String> getFileInfoList() {

         // Print out directory contents of a directory
        if (iNodeForFileToOpen != null && iNodeForFileToOpen.getFileModeAsString().charAt(0) == 'd')
            return fileInfoList;

        // If not a directory, print contents of directory this file exists in
        else
            return (new Directory(this).getFileInfo());
    }

    /**
     * Returns true if this file is a directory, false otherwise.
     * @return True for directory, false for regular file.
     */
    public boolean isDirectory() {
        return this.isDirectory;
    }

    /**
     * Obtains the byte buffer for the current directory.
     * @return Byte buffer reference.
     */
    public ByteBuffer getDirDataBuffer() {
        return this.dirDataBuffer;
    }

    /**
     * Returns the iNode that points to this file.
     * @return iNode for this file.
     */
    public INode getiNodeForFileToOpen() {
        return this.iNodeForFileToOpen;
    }

    /**
     * Obtains the name of the next directory to search in as a string.
     * @return The directory name as a string.s
     */
    public String getNextDirectoryString() {
        return this.curDirString;
    }
}