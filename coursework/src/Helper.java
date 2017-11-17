package coursework;

import java.nio.ByteBuffer;

public abstract class Helper {

    /** 
     * Method to print all information containined within the superblock.
     * The superblock holds characteristics of the filesystem.
     */
    public static void printSuperblockInfo(ByteBuffer byteBuffer) {

        System.out.println("--------------------");
        System.out.println("Superblock Information:");
        System.out.println("--------------------");
        System.out.println("Total number of inodes:      " + byteBuffer.getInt(0));
        System.out.println("Total number of blocks:      " + byteBuffer.getInt(4));
        System.out.println("Block size (bytes):          " + 1024 * (int) Math.pow(2, byteBuffer.getInt(24)));
        System.out.println("No. of blocks per group:     " + byteBuffer.getInt(32));
        System.out.println("No. of inodes per group:     " + byteBuffer.getInt(40));
        System.out.println("Magic number:                " + String.format("0x%02X ", byteBuffer.getInt(56)));
        System.out.println("Size of each inode (bytes):  " + byteBuffer.getInt(88));
        
        String volumeString = "";
        for (int i = 0; i < 16; i++) {
            int asciiInt = byteBuffer.get(120+i) & 0xFF;
            volumeString += (char) asciiInt;
        }
        System.out.println("Volume label (disk name):    " + volumeString);
        System.out.println("--------------------\n");
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

        System.out.println("\nByte Count: " + bytes.length);
        System.out.println("----------");
        
        while (count < bytes.length) {

            StringBuilder sb = new StringBuilder();
            String asciiString = "";
            
            for (int i = 0; i < 16; i++) {
            
                // Obtain hex equivalent of given byte in array     
                sb.append(String.format("%02X ", bytes[count]));
                
                if (i == 7 || i == 15)
                    sb.append("| ");

                count++;
            }
            
            System.out.print(sb.toString());

            for (int i = 0; i < 16; i++) {

                // Obtain ASCII equivalent of given byte in array
                int asciiInt = bytes[(count-16) + i] & 0xFF;
                
                if (asciiInt > 32 && asciiInt < 127)
                    asciiString += (char)asciiInt;
                else
                    asciiString += "_";
                
                if (i == 7 || i == 15) {
                    asciiString += " | ";
                }
            
            }
            
            asciiString += "\n";
            System.out.print(asciiString);
        }

        System.out.println("----------\n");
    }
}