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

        System.out.println("\nByte Count: " + bytes.length);
        System.out.println("----------");
        
        while (count < bytes.length) {

            StringBuilder sb = new StringBuilder();
            String asciiString = "";
            byteCount = 0;

            for (int i = 0; i < 16; i++) {

                // Add XX entries for too few bytes
                if (count >= bytes.length)
                    sb.append("XX ");
                else {
                    // Obtain hex equivalent of given byte in array     
                    sb.append(String.format("%02X ", bytes[count]));
                    byteCount++;
                }

                if (i == 7 || i == 15)
                    sb.append("| ");

                count++;
            }
            
            System.out.print(sb.toString());

            int asciiCount = 0;

            for (int i = 0; i < 16; i++) {

                if (asciiCount >= byteCount)
                    asciiString += " ";
                else {
                    // Obtain ASCII equivalent of given byte in array
                    int asciiInt = bytes[(count-16) + i] & 0xFF;
                    asciiString += (asciiInt > 32 && asciiInt < 127) ? (char) asciiInt : "_";                    
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
}