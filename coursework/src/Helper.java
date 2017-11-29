package coursework;

import java.nio.ByteBuffer;

import java.util.List;

public abstract class Helper {

    public static void printPointers(List<Integer> pointers) {
        System.out.println("----------");
        for (int i : pointers)
           System.out.println("pointer: " + i);
        System.out.println("----------\n");
    }

    /**
     * This method displays a block as a set of hex and corresponding ASCII values.
     * ASCII codes are printed alongside the hex codes.
     * The method also handles having too few bytes!
     * 
     * @param bytes The array of bytes to output in hex format.
     */
    public static void dumpHexBytes(byte[] bytes) {
                
        int count = 0;
        int byteCount = 0;

        System.out.println("\n----------\nByte Count: " + bytes.length);
        System.out.println("----------");
        
        while (count < bytes.length) {

            String hexString = "";
            String asciiString = "";
            byteCount = 0;

            for (int i = 0; i < 16; i++) {

                // Add XX entries for too few bytes
                if (count >= bytes.length)
                    hexString += "XX ";
                else {
                    // Obtain hex equivalent of given byte in array  
                    hexString += String.format("%02X ", bytes[count]);
                    byteCount++;
                }

                if (i == 7 || i == 15)
                    hexString += "| ";
                count++;
            }
            
            System.out.print(hexString);

            int asciiCount = 0;

            for (int i = 0; i < 16; i++) {

                if (asciiCount >= byteCount)
                    asciiString += " ";
                else {
                    // Obtain ASCII equivalent of given byte in array
                    int asciiInt = bytes[(count-16) + i] & 0xFFFF;
                    asciiString += (asciiInt >= 32 && asciiInt < 127) ? (char) asciiInt : "_";                    
                }

                if (i == 7 || i == 15)
                    asciiString += " | ";  
                asciiCount++;  
            }

            asciiString += "\n";
            System.out.print(asciiString);
        }

        System.out.println("----------");
    }

    /** 
     * Method to print all information containined within the superblock.
     * The superblock holds characteristics of the filesystem.
     * Further information is printed, which is derived from the superblock fields.
     */
    public static void printSuperblockInfo(SuperBlock superBlock) {
        System.out.println("\n--------------------");
        System.out.println("Superblock Information:");
        System.out.println("--------------------");
        System.out.println("Total number of inodes:      " + superBlock.getTotaliNodes());
        System.out.println("Total number of blocks:      " + superBlock.getTotalBlocks());
        System.out.println("Block size (bytes):          " + superBlock.getBlockSize());
        System.out.println("No. of blocks per group:     " + superBlock.getBlocksPerGroup());
        System.out.println("No. of inodes per group:     " + superBlock.getiNodesPerGroup());
        System.out.println("Magic number:                " + superBlock.getMagicNumber());
        System.out.println("Size of each inode (bytes):  " + superBlock.getiNodeSize());
        System.out.println("Volume label (disk name):    " + superBlock.getVolumeLbl());
        System.out.println("--------------------");
        System.out.println("Further Information:");
        System.out.println("----------");
        System.out.println("iNode table size (blocks):   " + superBlock.getiNodeTableSize());
        System.out.println("iNode table size (bytes):    " + superBlock.getiNodeTableSize() * superBlock.getBlockSize());
        System.out.println("iNodes per iNode table:      " + superBlock.getiNodesPerGroup() + "\n(see iNodes per group)");
        System.out.println("Total volume size (bytes):   " + superBlock.getTotalBlocks() * superBlock.getBlockSize());
        System.out.println("--------------------\n");
    }

    /**
     * Outputs all fields relevant to a given iNode.
     */
    public static void printINodeInfo(INode iNode) {
        System.out.println("\n----------");
        System.out.println("iNode " + iNode.getINodeNumber() + " information: ");
        System.out.println("----------");
        System.out.println("File mode:                              0x" + String.format("%02X ", iNode.getFileMode()));
        System.out.println("User ID:                                "   + iNode.getUserID());
        System.out.println("File Size (bytes):                      "   + iNode.getLowerFileSize());
        System.out.println("Last Access Time:                       "   + iNode.getLastAccessTime());
        System.out.println("Creation Time:                          "   + iNode.getCreationTime());
        System.out.println("Last Modified Time:                     "   + iNode.getLastModifiedTime());
        System.out.println("Deleted Time:                           "   + iNode.getDeletedTime());
        System.out.println("Group ID of owner:                      "   + iNode.getGroupID());
        System.out.println("Number of hard links referencing file:  "   + iNode.getNumHardLinks());
        //System.out.println("Pointer to first block:                 "   + getDirectPointers()[0]); // PRINT ALL POINTERS!
        System.out.println("----------\n");
    }
}