package coursework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Directory extends DataBlock {
    
    private ByteBuffer directoryByteBuffer;
    private SuperBlock superBlock;

    public Directory(ByteBuffer directoryByteBuffer, SuperBlock superBlock) {
        super(superBlock.getVolume());
        this.directoryByteBuffer = directoryByteBuffer;
        this.superBlock = superBlock;
    }

    // public String getNextRowInDirectory(int offsetLength, ByteBuffer byteBlockBuffer) {

        
    //     System.out.println("Block containing iNode " + iNodeNumber + " (need this to get fields for this iNode):");
    //     Helper.dumpHexBytes(block);


    //     System.out.println("length: " + this.getShortFromBytes(offsetLength + 4, byteBlockBuffer));
    //     System.out.println("name len: " + this.getByte(offsetLength + 6, byteBlockBuffer));
    //     System.out.println("file type: " + this.getByte(offsetLength + 7, byteBlockBuffer));
        
        
    //     // Obtain file name given the filename length
    //     byte[] filenameBytes = new byte[this.getByte(offsetLength + 6, byteBlockBuffer)];
    //     for (int i = 0; i < filenameBytes.length; i++) {
    //         filenameBytes[i] = this.getByte((offsetLength + (8+i)), byteBlockBuffer);
    //     }
    //     String filenameString = new String(filenameBytes);
    //     System.out.println("filename: " + filenameString);
    //     System.out.println("----------\n");


    //     // Obtain user ID (root etc.)
    //     String users = "";
    //     users += (userID == 0) ? "root " : Integer.toString(userID);                 // User ID of owner
    //     users += (groupIDOfOwner == 0) ? "root" : Integer.toString(groupIDOfOwner);  // Group ID of owner

    //     // Obtain file name given the filename length
    //     byte[] fileNameBytes = new byte[this.getByte(6,  byteBlockBuffer)];
    //     for (int i = 0; i < fileNameBytes.length; i++) {
    //         fileNameBytes[i] = this.getByte(8+i, byteBlockBuffer);
    //     }
    //     String filenameStr = new String(fileNameBytes);

    //     // Unix-style directory listing for iNode 2
    //     String directoryString = /*fileInfo + " " + */users + " " + Integer.toString(numHardLinks) + " " + Integer.toString(fileSize) 
    //                         + " " + formattedDate + " " + filenameStr + "\n"; 

    //     return directoryString;        
    // }


    // /**
    //  * Method to retrieve an array of strings - each string represents a directory/file in the listing.
    //  * Each directory contains relevant information to that directory.
    //  * The output is presented in a Unix-like format.
    //  *
    //  * @return An array of directory strings.
    //  */
    // public String getDirectoryInfo() {

    //     // Obtain first direct pointer from iNode 2
    //     int firstBlockPointer = this.getIntFromBytes(40, directoryByteBuffer);

    //     // Accessing data block in file referenced by iNode 2 (lost+found, big-dir etc)
    //     byte[] block = this.read(firstBlockPointer * superBlock.getBlockSize(), superBlock.getBlockSize());
    //     ByteBuffer iNode2DataBlock = ByteBuffer.wrap(block);
    //     iNode2DataBlock.order(ByteOrder.LITTLE_ENDIAN);
    //     System.out.println("Data block found using iNode 2 pointer (Root Directory): ");
    //     Helper.dumpHexBytes(block);

        
    //     // Prints row/entry of a directory referenced by iNode 2
    //     String directoryString = "";
    //     int currentLength = 0;

    //     while (currentLength < superBlock.getBlockSize()) {

    //         directoryString += this.getNextRowInDirectory(currentLength, iNode2DataBlock);

    //         // Add length to find next entry 'row'
    //         currentLength += this.getShortFromBytes(currentLength + 4, iNode2DataBlock);
    //     }
        
    //     System.out.println("----------");
    //     System.out.println("Directory Listing for Root Directory (using iNode 2):");
    //     System.out.println("----------");
    //     System.out.println(directoryString+"\n");

    //     return directoryString;
    // }
}