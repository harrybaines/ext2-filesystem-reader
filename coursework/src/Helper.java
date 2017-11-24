package coursework;

import java.nio.ByteBuffer;

public abstract class Helper {

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

                // add XX entries for too few bytes
            
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

                asciiString += (asciiInt > 32 && asciiInt < 127) ? (char) asciiInt : "_";
                
                if (i == 7 || i == 15)
                    asciiString += " | ";    
            }
            
            asciiString += "\n";
            System.out.print(asciiString);
        }

        System.out.println("----------");
    }
}