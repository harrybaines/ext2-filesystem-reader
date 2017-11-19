package coursework;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Driver {
    

    // magic line - (int) fileInBytes[i] & 0xFF;


    public Driver() {

        // API user will use
        Volume vol = new Volume("ext2fs");
        Ext2File file = new Ext2File(vol, "placeholder");
        //byte buf[ ] = f.read(0L, f.size);
        //System.out.format ("%s\n", new String(buf));

        // Obtain superblock (1024 byte size)
        // Print super block information
        SuperBlock superBlock = new SuperBlock(vol);
        superBlock.printSuperblockInfo();

        byte[] block = file.read(1 * superBlock.getBlockSize(), superBlock.getBlockSize());
        ByteBuffer byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("Byte 12 in decimal: " + file.getByte(12, byteBlockBuffer));
        
        // Obtain hex and ASCII values of bytes
        Helper.dumpHexBytes(block);

        

        // Finds the iNode table pointer from group descriptor
        block = file.read(2 * superBlock.getBlockSize(), 32);
        byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int iNodeTblPointer = file.getIntFromBytes(8, byteBlockBuffer);
        System.out.println("Group Descriptor: ");
        Helper.dumpHexBytes(block);
        System.out.println("iNodeTblPointer: " + iNodeTblPointer + " (Block number of first inode table block)\n");

        // Block containing iNode 2 - 1 iNode offset into the iNode table
        block = file.read(iNodeTblPointer * superBlock.getBlockSize() + superBlock.getiNodeSize(), superBlock.getiNodeSize());
        byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("Block containing iNode 2: ");
        Helper.dumpHexBytes(block);

        // Obtains and prints out data relevant data for iNode 2
        // Obtain iNode 2 info
        int fileModeForINode = file.getShortFromBytes(0, byteBlockBuffer);
        int userID = file.getShortFromBytes(2, byteBlockBuffer);
        int fileSize = file.getIntFromBytes(4, byteBlockBuffer);
        String lastAccessTime = new Date( (long) file.getIntFromBytes(8, byteBlockBuffer) * 1000 ).toString();
        String creationTime = new Date( (long) file.getIntFromBytes(12, byteBlockBuffer) * 1000 ).toString();
        
        String lastModifiedTime = new Date( (long) file.getIntFromBytes(16, byteBlockBuffer) * 1000 ).toString();
        Date modifiedDate = new Date( (long) file.getIntFromBytes(16, byteBlockBuffer) * 1000 );
        String formattedDate = new SimpleDateFormat("MMM dd HH:mm").format(modifiedDate);

        int delTime = file.getIntFromBytes(20, byteBlockBuffer);
        String deletedTime = (delTime == 0) ? "-" : new Date( (long) delTime * 1000 ).toString();

        int groupIDOfOwner = file.getShortFromBytes(24, byteBlockBuffer);
        int numHardLinks = file.getShortFromBytes(26, byteBlockBuffer);
        int firstBlockPointer = file.getIntFromBytes(40, byteBlockBuffer);

        // Prints iNode 2 information
        System.out.println("----------");
        System.out.println("iNode 2 Information:");
        System.out.println("----------");
        System.out.println("File mode:                              0x" + String.format("%02X ", fileModeForINode));
        System.out.println("User ID:                                "   + userID);
        System.out.println("File Size (bytes):                      "   + fileSize);
        System.out.println("Last Access Time:                       "   + lastAccessTime);
        System.out.println("Creation Time:                          "   + creationTime);
        System.out.println("Last Modified Time:                     "   + formattedDate);
        System.out.println("Deleted Time:                           "   + deletedTime);
        System.out.println("Group ID of owner:                      "   + groupIDOfOwner);
        System.out.println("Number of hard links referencing file:  "   + numHardLinks);
        System.out.println("Pointer to first block:                 "   + firstBlockPointer);
        System.out.println("----------\n");


        // Accessing data block in file referenced by iNode 2
        block = file.read(firstBlockPointer * superBlock.getBlockSize(), superBlock.getBlockSize());
        byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("Data block found using iNode 2 pointer (Root Directory): ");
        Helper.dumpHexBytes(block);


        // Obtain file mode read/write/execute permissions string
        String fileInfo = "";
        fileInfo += ((fileModeForINode & 0x4000) > 0) ? "d" : "-";  // Directory
        fileInfo += ((fileModeForINode & 0x0100) > 0) ? "r" : "-";  // User Read
        fileInfo += ((fileModeForINode & 0x0080) > 0) ? "w" : "-";  // User Write
        fileInfo += ((fileModeForINode & 0x0040) > 0) ? "x" : "-";  // User Execute
        fileInfo += ((fileModeForINode & 0x0020) > 0) ? "r" : "-";  // Group Read
        fileInfo += ((fileModeForINode & 0x0010) > 0) ? "w" : "-";  // Group Write
        fileInfo += ((fileModeForINode & 0x0008) > 0) ? "x" : "-";  // Group Execute
        fileInfo += ((fileModeForINode & 0x0004) > 0) ? "r" : "-";  // Others Read
        fileInfo += ((fileModeForINode & 0x0002) > 0) ? "w" : "-";  // Others Write
        fileInfo += ((fileModeForINode & 0x0001) > 0) ? "x" : "-";  // Others Execute

        // Obtain user ID (root etc.)
        String users = "";
        users += (userID == 0) ? "root " : Integer.toString(userID);                 // User ID of owner
        users += (groupIDOfOwner == 0) ? "root" : Integer.toString(groupIDOfOwner);  // Group ID of owner

        // Obtain file name given the filename length
        byte[] fileNameBytes = new byte[file.getByte(6, byteBlockBuffer)];
        for (int i = 0; i < fileNameBytes.length; i++) {
            fileNameBytes[i] = file.getByte(8+i, byteBlockBuffer);
        }
        String filenameStr = new String(fileNameBytes);

        // Unix-style directory listing for iNode 2
        String directoryListing = fileInfo + " " + users + " " + Integer.toString(numHardLinks) + " " + Integer.toString(fileSize) 
                            + " " + formattedDate + " " + filenameStr; 

        System.out.println("----------");
        System.out.println("Directory Listing for Root Directory (using iNode 2):");
        System.out.println("----------");
        System.out.println(directoryListing+"\n");


        // Prints row/entry of a directory
        int currentLength = 0;

        while (currentLength < superBlock.getBlockSize()) {

            // Print details about current 'row' in the directory
            System.out.println("----------");
            System.out.println("iNode number: " + file.getIntFromBytes(currentLength + 0, byteBlockBuffer));
            System.out.println("length: " + file.getShortFromBytes(currentLength + 4, byteBlockBuffer));
            System.out.println("name len: " + file.getByte(currentLength + 6, byteBlockBuffer));
            System.out.println("file type: " + file.getByte(currentLength + 7, byteBlockBuffer));
            
            // Obtain file name given the filename length
            byte[] filenameBytes = new byte[file.getByte(currentLength + 6, byteBlockBuffer)];
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
        block = file.read(iNodeTblPointer * superBlock.getBlockSize() + 11 * superBlock.getiNodeSize(), superBlock.getiNodeSize());
        ByteBuffer iNode12byteBlockBuffer = ByteBuffer.wrap(block);
        iNode12byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("iNode 12 info: ");
        Helper.dumpHexBytes(block);

        int iNode12DataBlockPointer1 = file.getIntFromBytes(40, iNode12byteBlockBuffer);
        block = file.read(iNode12DataBlockPointer1 * superBlock.getBlockSize(), superBlock.getBlockSize());
        byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("Data block 1 found using iNode 12: ");
        Helper.dumpHexBytes(block);

        int iNode12DataBlockPointer2 = file.getIntFromBytes(44, iNode12byteBlockBuffer);
        block = file.read(iNode12DataBlockPointer2 * superBlock.getBlockSize(), superBlock.getBlockSize());
        byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("Data block 2 found using iNode 12: ");
        Helper.dumpHexBytes(block);

        
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