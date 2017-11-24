package coursework;

import java.io.*;

import java.util.List;
import java.util.ArrayList;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Ext2File extends DataBlock {

    private String fileString;          // Filename of the file the user wishes to open from the volume
    private SuperBlock superBlock;      // Reference to the superblock

    private int[] iNodeTablePointers;

    private ByteBuffer dirDataBuffer;

    private String curDirString;

    private int charCount;

    private INode iNodeForFileToOpen;

    private String fileName;

    /**
     * Constructor used to represent a file in the given volume.
     * The file can then be read and output to the user and can also view important info.
     *
     * @param vol The volume represented by the file opened.
     * @param fileString The name of the file to open in the volume.
     */
    public Ext2File(Volume vol, String fileString) {
        super(vol);
        this.fileString = fileString;  

        // Print superblock information
        superBlock = new SuperBlock(vol);
        superBlock.printSuperblockInfo();

        // Array containing all iNode table pointers
        iNodeTablePointers = getAllINodeTblPointers();

        charCount = 0;

        System.out.println("File String: " + fileString);
        if (openFile())
            System.out.println("File opened successfully!\n\n");
        // else
        //     System.out.println("Finished searching - a file has potentially been found.\n\n");
    }

    public ByteBuffer getDirDataBuffer() {
        return this.dirDataBuffer;
    }

    public int[] getiNodeTablePointers() {
        return this.iNodeTablePointers;
    }

    public SuperBlock getSuperblock() {
        return this.superBlock;
    }

    public String getFileString() {
        return this.fileString;
    }

    public String getNextDirectoryString() {
        return this.curDirString;
    }

    public byte[] getDirBytes(int iNodeNumber) {

        // Obtains all bytes relevant to a given
        int tablePointerIndex = getTablePointerForiNode(iNodeNumber, superBlock.getiNodesPerGroup(), superBlock.getTotaliNodes());
        INode iNode = new INode(iNodeNumber, iNodeTablePointers[tablePointerIndex], tablePointerIndex, superBlock);
        iNode.printINodeInfo();

        byte[] rootDataBlocks = iNode.getDataBlocksFromDirectPointers();
        return rootDataBlocks;
    }

    /**
     * Simple method to print the contents of a file in ASCII format.
     * A string is returned containing the full file contents.
     * @return The string of characters in the file.
     */
    public String printFileContents(byte[] bytes) {

        String asciiString = "";

        for (byte b : bytes) {

            // Obtain ASCII equivalent of given byte in array
            int asciiInt = b & 0xFF;
            
            if (asciiInt >= 1 && asciiInt < 256)
                asciiString += (char)asciiInt;
        }
        return asciiString;
    }

    // public long size() {
    //     return this.size;
    // }

    public byte[] readFile(long startByte, long length) {

        if (length < 0 || startByte < 0)
            return new byte[0];

        byte[] byteArray = new byte[(int)length];

        if (iNodeForFileToOpen != null) {

            // Find and read all datablocks for the file, if found successfully
            int iNodeNumber = iNodeForFileToOpen.getINodeNumber();
            int tablePointerIndex = getTablePointerForiNode(iNodeNumber, superBlock.getiNodesPerGroup(), superBlock.getTotaliNodes());
            INode iNode = new INode(iNodeNumber, iNodeTablePointers[tablePointerIndex], tablePointerIndex, superBlock);
            byte[] iNodeInfoBytes = iNode.getINodeInfoBytes();
            iNode.printINodeInfo();

            ByteBuffer iNodebyteBlockBuffer = ByteBuffer.wrap(iNodeInfoBytes);
            iNodebyteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);

            // Obtain iNode for all direct pointers if they exist
            List<Byte> dataBytes = new ArrayList<Byte>();

            byte[] dataBlocksFromPointers = iNodeForFileToOpen.getDataBlocksFromDirectPointers();

            // Transfer all found bytes from data blocks array into buffer
            for (byte b : dataBlocksFromPointers)
                dataBytes.add(b);

            int index = 0;
            for (int i = (int) startByte; i < byteArray.length; i++) {
                if (i >= dataBlocksFromPointers.length) {
                    System.out.println("----------\nThe length you provided is too large - all data found has been printed.\n");
                    break;
                }
                byteArray[i] = dataBlocksFromPointers[i];
            }

        }
        else {
            System.out.println("File not found/selected - unable to open file specified!");
            byteArray = new byte[0];
        }

        return byteArray;
    }

    public String getCurDirectoryString() {

        // Obtain individual dircetory names from fileString
        curDirString = "";
        String fileString = getFileString();
        int beginCount = 0;
        int slashCount = 0;

        while (true) {

            if (charCount >= fileString.length()) 
                break;

            if (fileString.charAt(charCount) == '/') {
                slashCount++;
                beginCount++;
                if (slashCount == 2) {
                    break;
                }
            }

            if (fileString.charAt(charCount) != '/')
                curDirString += fileString.charAt(charCount);

            charCount++;

            beginCount++;

        }

        return curDirString;
    }


    public boolean openFile() {

        // Get directory bytes pointed to by iNode 2 direct pointer
        byte[] rootDataBlocks = getDirBytes(2);
        System.out.println("Directory referenced by iNode 2:");
        Helper.dumpHexBytes(rootDataBlocks);
        dirDataBuffer = ByteBuffer.wrap(rootDataBlocks);    
        dirDataBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Finds the directory specified in the file path string
        while ((curDirString = getCurDirectoryString()) != "") {

            System.out.println("Current directory string to look through: " + curDirString + "\n");

            Directory d = new Directory(this);
            d.getFileInfo();

            // Check to see if current directory using the file path string actually exists!
            INode nextINode = d.getNextINode();

            // INode exists and points to a directory
            if (nextINode != null && nextINode.getFileModeAsString().charAt(0) != '-') {
                rootDataBlocks = getDirBytes(d.getNextINode().getINodeNumber());
                System.out.println("Directory referenced by iNode " + d.getNextINode().getINodeNumber() + ":");
                Helper.dumpHexBytes(rootDataBlocks);
                dirDataBuffer = ByteBuffer.wrap(rootDataBlocks);
                dirDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
            }
            // INode exists and points to a file
            else if (nextINode != null) {
                iNodeForFileToOpen = nextINode;
                return false;
            }
        }

        return true;
    }

    /**
     * Retrieves an array of all iNode table pointers from the group descriptors.
     * The method uses block group 0 to access all group descriptors.
     *
     * @return The array of all integer iNode table pointers.
     */
    private int[] getAllINodeTblPointers() {

        // Obtains array of iNode table pointers from all group descriptors
        int numBlockGroups = (int) Math.ceil((double) superBlock.getTotalBlocks() / (double) superBlock.getBlocksPerGroup());

        GroupDescriptor[] groupDescs = new GroupDescriptor[numBlockGroups];
        int[] iNodeTablePointers = new int[numBlockGroups];

        // Finds the group descriptors using block group 0 (1024 bytes after superblock)
        byte[] groupDescBytes = this.read(2 * superBlock.getBlockSize(), numBlockGroups * GroupDescriptor.GROUP_DESCRIPTOR_SIZE);
        ByteBuffer groupDescBuffer = ByteBuffer.wrap(groupDescBytes);
        groupDescBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int currentDesc = 0;

        while (currentDesc < numBlockGroups) {
            groupDescs[currentDesc] = new GroupDescriptor(groupDescBuffer, currentDesc, superBlock);
            iNodeTablePointers[currentDesc] = groupDescs[currentDesc].getINodeTblPointer();
            currentDesc++;
        }

        System.out.println("----------");
        for (int i = 0; i < iNodeTablePointers.length; i++)
            System.out.println("iNode table pointer " + i + ": " + iNodeTablePointers[i]);
        System.out.println("----------");

        return iNodeTablePointers;
    }
}
