package coursework;

import java.io.*;

import java.util.List;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Ext2File extends DataBlock {

    private String fileString;          // Filename of the file the user wishes to open from the volume
    private SuperBlock superBlock;      // Reference to the superblock

    private int[] iNodeTablePointers;

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

        System.out.println("File String: " + fileString);
        openFile();
    }

    public byte[] getRootDirBytes() {

        // Obtains all bytes relevant to iNode2 (root directory)
        int iNodeNumber = 2;
        int tablePointerIndex = getTablePointerForiNode(iNodeNumber, superBlock.getiNodesPerGroup(), superBlock.getTotaliNodes());
        INode iNode2 = new INode(iNodeNumber, iNodeTablePointers[tablePointerIndex], tablePointerIndex, superBlock);
        iNode2.printINodeInfo();

        byte[] rootDataBlocks = iNode2.getDataBlocksFromDirectPointers();
        return rootDataBlocks;
    }

    

    public void openFile() {

        // Get directory bytes pointed to by iNode 2 direct pointer
        byte[] rootDataBlocks = getRootDirBytes();
        System.out.println("Root Directory (referenced by iNode 2):");
        Helper.dumpHexBytes(rootDataBlocks);


        // Obtain iNode 1722 info and directory it points to
        int iNodeNumber = 1722;
        int tablePointerIndex = getTablePointerForiNode(iNodeNumber, superBlock.getiNodesPerGroup(), superBlock.getTotaliNodes());
        INode iNode1722 = new INode(iNodeNumber, iNodeTablePointers[tablePointerIndex], tablePointerIndex, superBlock);
        iNode1722.printINodeInfo();

        byte[] rootDataBlocks1722 = iNode1722.getDataBlocksFromDirectPointers();
        System.out.println("Root Directory (referenced by iNode 1722):");
        Helper.dumpHexBytes(rootDataBlocks1722);




        ByteBuffer dirDataBuffer = ByteBuffer.wrap(rootDataBlocks);
        dirDataBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Read iNode 2 using iNode table pointer from the superblock
        Directory d = new Directory(dirDataBuffer, iNodeTablePointers, superBlock);
        List<String> directoryStrings = d.getFileInfo();
        
        System.out.println("\n----------");
        System.out.println("Directory Listing for Root Directory (using iNode 2):");
        System.out.println("----------");
        for (String row : directoryStrings)
            System.out.println(row);
        System.out.println("----------\n");






        // EXAMPLE - finding data block referenced by iNode 12 for two-cities
        iNodeNumber = 12;
        tablePointerIndex = getTablePointerForiNode(iNodeNumber, superBlock.getiNodesPerGroup(), superBlock.getTotaliNodes());
        INode iNode12 = new INode(iNodeNumber, iNodeTablePointers[tablePointerIndex], tablePointerIndex, superBlock);
        byte[] iNode12InfoBytes = iNode12.getINodeInfoBytes();
        iNode12.printINodeInfo();

        ByteBuffer iNode12byteBlockBuffer = ByteBuffer.wrap(iNode12InfoBytes);
        iNode12byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);


        // Obtain iNode 12 first direct pointer
        int iNode12DataBlockPointer1 = this.getIntFromBytes(40, iNode12byteBlockBuffer);
        byte[] block1 = this.read(iNode12DataBlockPointer1 * superBlock.getBlockSize(), superBlock.getBlockSize());

        // Obtain iNode 12 second direct pointer
        int iNode12DataBlockPointer2 = this.getIntFromBytes(44, iNode12byteBlockBuffer);
        byte[] block2 = this.read(iNode12DataBlockPointer2 * superBlock.getBlockSize(), superBlock.getBlockSize());

        // Print all file contents for iNode 12
        System.out.println("File contents pointed to by iNode 12:");
        System.out.println("----------");
        System.out.println(this.printFileContents(block1) + this.printFileContents(block2) + "\n");
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