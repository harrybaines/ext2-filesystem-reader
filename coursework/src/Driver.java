package coursework;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Driver {

    private static final int BLOCK_SIZE = 1024;
    

    // magic line - (int) fileInBytes[i] & 0xFF;


    public Driver() {

        // API user will use
        Volume vol = new Volume("ext2fs");
        Ext2File file = new Ext2File(vol, "placeholder");
        //byte buf[ ] = f.read(0L, f.size);
        //System.out.format ("%s\n", new String(buf));

        // Obtain superblock (1024 byte size)
        byte[] block = file.read(1 * BLOCK_SIZE, BLOCK_SIZE);
        ByteBuffer byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);

        System.out.println("Byte 12 in decimal: " + file.getByte(12, byteBlockBuffer));
        
        // Obtain hex and ASCII values of bytes
        Helper.dumpHexBytes(block);

        // Print super block information
        SuperBlock superBlock = new SuperBlock(vol);
        superBlock.printSuperblockInfo();

        // Finds the iNode table pointer from group descriptor
        block = file.read(2 * BLOCK_SIZE, 32);
        byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int iNodeTblPointer1 = file.getIntFromBytes(8, byteBlockBuffer);
        System.out.println("Group Descriptor: ");
        Helper.dumpHexBytes(block);
        System.out.println("iNodeTblPointer: " + iNodeTblPointer1 + " (Block number of first inode table block)\n");

        // Block containing iNode 2 - 1 iNode offset into the iNode table
        block = file.read(iNodeTblPointer1 * BLOCK_SIZE + (128), 128);
        byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int pointerToFirstDataBlock = file.getIntFromBytes(40, byteBlockBuffer);
        System.out.println("Block containing iNode 2: ");
        Helper.dumpHexBytes(block);
        System.out.println("Pointer to first data block (from iNode 2): " + pointerToFirstDataBlock + "\n");

        // Prints out data relevant data for iNode 2
        int fileModeForINode2 = file.getShortFromBytes(0, byteBlockBuffer);
        System.out.println("File mode for iNode 2: 0x" + String.format("%02X ", fileModeForINode2) + "\n");


        // Accessing data block in file referenced by iNode 2
        block = file.read(pointerToFirstDataBlock * BLOCK_SIZE, 1024);
        byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("Data block found using iNode 2 pointer: ");
        Helper.dumpHexBytes(block);

        // Prints row/entry of a directory
        int currentLength = 0;

        while (currentLength < BLOCK_SIZE) {

            System.out.println("----------");
            System.out.println("iNode number: " + file.getIntFromBytes(currentLength + 0, byteBlockBuffer));
            System.out.println("length: " + file.getShortFromBytes(currentLength + 4, byteBlockBuffer));
            System.out.println("name len: " + file.getByte(currentLength + 6, byteBlockBuffer));
            System.out.println("file type: " + file.getByte(currentLength + 7, byteBlockBuffer));
            
            int fileNameLength = file.getShortFromBytes(currentLength + 4, byteBlockBuffer) - 8;
            System.out.println("filename length: " + fileNameLength);

            // Obtain file name given the filename length
            byte[] filenameBytes = new byte[fileNameLength];
            for (int i = 0; i < filenameBytes.length; i++) {
                filenameBytes[i] = file.getByte((currentLength + (8+i)), byteBlockBuffer);
            }
            String filenameString = new String(filenameBytes);
            System.out.println("filename: " + filenameString);
            System.out.println("----------\n");

            // Add length to find next entry 'row'
            currentLength += file.getShortFromBytes(currentLength + 4, byteBlockBuffer);
        }

        // Example - finding data block referenced by iNode 12 for two-cities
        // block = file.read(iNodeTblPointer1 * BLOCK_SIZE + (12 * 128), 1024);
        // byteBlockBuffer = ByteBuffer.wrap(block);
        // byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // System.out.println("Data block found using iNode 12 for two-cities: ");
        // Helper.dumpHexBytes(block);

    }

    

    /**
     * Main method to begin the driver program.
     * 
     * @param args Unused.
     */
    public static void main(String[] args) {
        new Driver();
    }
}