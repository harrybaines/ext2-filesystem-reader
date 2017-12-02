package ext2;

import java.nio.ByteBuffer;
import java.util.List;

public class Helper {

    private String hexBytesString;

    public Helper() {
        this.hexBytesString = "";
    }

    public void printPointers(List<Integer> pointers) {
        System.out.println("----------");
        for (int i : pointers)
           System.out.println("pointer: " + i);
        System.out.println("----------\n");
    }

    public String getHexBytesString(byte[] bytes) {
        this.dumpHexBytes(bytes, false);
        return this.hexBytesString;
    }

    /**
     * This method displays a block as a set of hex and corresponding ASCII values.
     * ASCII codes are printed alongside the hex codes.
     * The method also handles having too few bytes!
     * 
     * @param bytes The array of bytes to output in hex format.
     */
    public void dumpHexBytes(byte[] bytes, boolean print) {
                
        int count = 0;
        int byteCount = 0;
        this.hexBytesString = "";

        System.out.println("\n----------\n\033[1mByte Count: " + bytes.length + "\033[0m");
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
            
            if (print)
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
            hexBytesString += hexString + asciiString;

            if (print)
                System.out.print(asciiString);
        }

        if (print)
            System.out.println("----------\n");
    }

    /** 
     * Method to print all information containined within the superblock.
     * The superblock holds characteristics of the filesystem.
     * Further information is printed, which is derived from the superblock fields.
     */
    public void printSuperblockInfo(SuperBlock superBlock) {
        System.out.println("\n--------------------\n\033[1mSuperblock Information:\033[0m\n--------------------");
        System.out.println(superBlock.getSuperBlockInfo() + "--------------------\n");
    }

    /** 
     * Method to print further information derived from the super block data.
     * The superblock holds characteristics of the filesystem.
     * Further information is printed, which is derived from the superblock fields.
     */
    public void printFurtherSuperBlockInfo(SuperBlock superBlock) {
        System.out.println("--------------------\n\033[1mFurther Information:\033[0m\n--------------------");
        System.out.println(superBlock.getFurtherSuperBlockInfo() + "--------------------\n");
    }

    /**
     * Outputs all fields relevant to a given iNode.
     */
    public void printINodeInfo(INode iNode) {
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
        System.out.println("----------\n");
    }
}