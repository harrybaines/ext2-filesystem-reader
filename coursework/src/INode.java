package coursework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class INode extends DataBlock {

    private ByteBuffer iNodeBuffer;

    private int iNodeTblPointer;
    private int groupNum;
    private int iNodeNumber;
    private SuperBlock superBlock;

    private int iNodeOffset;
    private byte[] iNodeBytes;

    private int fileMode;

    private int indirectBlockSize;

    public INode(int iNodeNumber, int iNodeTblPointer, int groupNum, SuperBlock superBlock) {
        super(superBlock.getVolume());
        this.iNodeNumber = iNodeNumber;
        this.iNodeTblPointer = iNodeTblPointer;
        this.groupNum = groupNum;
        this.superBlock = superBlock;

        iNodeOffset = (iNodeTblPointer * superBlock.getBlockSize()) + (((iNodeNumber-1) - (groupNum * superBlock.getiNodesPerGroup())) * superBlock.getiNodeSize());

        iNodeBytes = this.read(iNodeOffset, superBlock.getiNodeSize());

        // Wrap in buffer for later reading
        iNodeBuffer = ByteBuffer.wrap(iNodeBytes);
        iNodeBuffer.order(ByteOrder.LITTLE_ENDIAN);

        indirectBlockSize = superBlock.getBlockSize() / 4;
    }

    public int getINodeNumber() {
        return this.iNodeNumber;
    }

    /** 
     * Method to obtain all iNode fields.
     * @return Array of bytes containing the iNode fields.
     */
    public byte[] getINodeInfoBytes() {
        return this.iNodeBytes;
    }

    /**
     * Retrieves an array of bytes referenced by the direct pointers in this iNode.
     * @return Array of bytes for the data blocks being pointed to.
     */
    public byte[] getDataBlocksFromPointers() {

        int indirectionLevel = 0;

        List<Byte> allDataBlocks = new ArrayList<Byte>();
        List<Integer> directPointers = this.getDirectPointers();

        // System.out.println("Direct Pointers for iNode " + iNodeNumber + ":");
        // Helper.printPointers(directPointers);

        // Transfer all found data from direct pointers into 'master' list
        for (byte b : this.getDataBlocks(directPointers))
            allDataBlocks.add(b);

        // Find indirect pointers and search through blocks to find data blocks
        int indirectPointer = this.getIndirectPointer();                // Initial indirect pointer in iNode
        int doubleIndirectPointer = this.getDoubleIndirectPointer();    // Initial double-indirect pointer in iNode
        int tripleIndirectPointer = this.getTripleIndirectPointer();    // Initial triple-indirect pointer in iNode

        // Level 1 of indirection - SINGLE INDIRECT
        if (indirectPointer != 0) {

            indirectionLevel = 1;

            // Obtains initial list of indirect pointers to begin indirection traversal
            List<Integer> indirectTableBlockPointers = this.getIndirectTableBlockPointers(indirectPointer);

            // Helper.printPointers(indirectTableBlockPointers);

            // Transfer all found data from indirect pointers into 'master' list
            for (byte b : this.getDataBlocks(indirectTableBlockPointers))
                allDataBlocks.add(b);
        }

        // Level 2 of indirection - DOUBLE INDIRECT
        if (doubleIndirectPointer != 0) {

            indirectionLevel = 2;

            // Obtains initial list of indirect pointers to begin indirection traversal
            List<Integer> indirectTableBlockPointers = this.getIndirectTableBlockPointers(doubleIndirectPointer);
            
            // Obtains all non-0 pointers from 3 levels of indirection
            List<Integer> pointersFromIndirectionLevel = this.getPointersByIndirectionLevel(indirectionLevel, indirectTableBlockPointers);
            
            // Helper.printPointers(indirectTableBlockPointers);
            // Helper.printPointers(pointersFromIndirectionLevel);

            // Transfer all found data from indirect pointers into 'master' list
            for (byte b : this.getDataBlocks(pointersFromIndirectionLevel))
                allDataBlocks.add(b);
        }

        // Level 3 of indirection - TRIPLE INDIRECT
        if (tripleIndirectPointer != 0) {

            indirectionLevel = 3;
           
            // Obtains initial list of indirect pointers to begin indirection traversal
            List<Integer> indirectTableBlockPointers = this.getIndirectTableBlockPointers(tripleIndirectPointer);
            
            // Obtains all non-0 pointers from 3 levels of indirection
            List<Integer> pointersFromIndirectionLevel = this.getPointersByIndirectionLevel(indirectionLevel, indirectTableBlockPointers);

            // Helper.printPointers(indirectTableBlockPointers);
            // Helper.printPointers(pointersFromIndirectionLevel);

            // Transfer all found data from indirect pointers into 'master' list
            for (byte b : this.getDataBlocks(pointersFromIndirectionLevel))
                allDataBlocks.add(b);
        }

        // Transfer dynamic array list into array to return
        byte[] byteArray = new byte[allDataBlocks.size()];
        for (int i = 0; i < allDataBlocks.size(); i++)
            byteArray[i] = allDataBlocks.get(i);

        return byteArray;
    }

    public List<Integer> getPointersByIndirectionLevel(int indirectionLevel, List<Integer> indirectTableBlockPointers) {

        int nextBlockPointer = 0;

        // Stores all pointers found that aren't 0
        List<Integer> blockPointers = new ArrayList<Integer>();

        // Iterate over first block of pointers
        for (int i : indirectTableBlockPointers) {
            
            if (i != 0) {
                // Get all pointers in the block it points to (get level 2)
                nextBlockPointer = i;
                List<Integer> level2Pointers = this.getIndirectTableBlockPointers(nextBlockPointer);
                
                if (indirectionLevel >= 2) {

                    // Iterate over 2nd block of pointers (level 2)
                    for (int j : level2Pointers) {

                        // Get all pointers in the block it points to (get level 3)
                        if (j != 0) {

                            // Add data to array if at level 2, otherwise ignore
                            if (indirectionLevel == 2)
                                blockPointers.add(j);

                            nextBlockPointer = j;

                            List<Integer> level3Pointers = this.getIndirectTableBlockPointers(nextBlockPointer);
                    
                            if (indirectionLevel == 3) {
                                // Iterate over final block of pointers (level 3)
                                for (int k : level3Pointers) {

                                    // Get pointers to data from level 3
                                    if (k != 0)
                                        blockPointers.add(k);
                                }
                            }
                            else
                                break;                            
                        }
                    }
                }
                else
                    break;
            }
        }
        return blockPointers;
    }

    public List<Byte> getDataBlocks(List<Integer> pointers) {

        // Create array of data blocks found from indirect pointers
        List<Byte> byteList = new ArrayList<Byte>();

        for (int i : pointers) {
            if (i != 0) {
                byte[] dataBlocksArray = this.read(i * superBlock.getBlockSize(), superBlock.getBlockSize());
                for (int curBlock = 0; curBlock < dataBlocksArray.length; curBlock++) {
                    byteList.add(dataBlocksArray[curBlock]);
                }
            }
        }
        return byteList;
    }

    /**
     * Outputs all fields relevant to a given iNode.
     */
    public void printINodeInfo() {
        System.out.println("\n----------");
        System.out.println("iNode " + iNodeNumber + " information: ");
        System.out.println("----------");
        System.out.println("File mode:                              0x" + String.format("%02X ", getFileMode()));
        System.out.println("User ID:                                "   + getUserID());
        System.out.println("File Size (bytes):                      "   + getLowerFileSize());
        System.out.println("Last Access Time:                       "   + getLastAccessTime());
        System.out.println("Creation Time:                          "   + getCreationTime());
        System.out.println("Last Modified Time:                     "   + getLastModifiedTime());
        System.out.println("Deleted Time:                           "   + getDeletedTime());
        System.out.println("Group ID of owner:                      "   + getGroupID());
        System.out.println("Number of hard links referencing file:  "   + getNumHardLinks());
        //System.out.println("Pointer to first block:                 "   + getDirectPointers()[0]); // PRINT ALL POINTERS!
        System.out.println("----------\n");
    }

    /**
     * Retrieves the file mode for the given iNode as a string.
     * @return A string representation of the file mode.
     */
    public short getFileMode() {
        return this.getShortFromBytes(0, iNodeBuffer);
    }

    /**
     * Retrieves the file mode field for the iNode and returns a string representation.
     * Used in Unix-like directory listings, displaying permissions, directory info etc.
     *
     * @return String representation of the file mode.
     */
    public String getFileModeAsString() {

        // Obtain short file mode
        short fileMode = getFileMode();

        // File mode hex codes
        int[] hexCodes = { 0x0100, 0x0080, 0x0040, 0x0020, 0x0010, 
                          0x0008, 0x0004, 0x0002, 0x0001 };
        
        //User/Group/Others can Read/Write/Execute
        char[] permissions = { 'r', 'w', 'x' };

        // Obtain file mode read/write/execute permissions string
        String fileInfo = "";
        fileInfo += ((fileMode & 0x4000) > 0) ? "d" : "-";  // Directory or File

        for (int i = 0; i < hexCodes.length; i++)
            fileInfo += ((fileMode & hexCodes[i]) > 0) ? permissions[i % permissions.length] : "-";

        return fileInfo;
    }

    /**
     * Retrieves the lower 16 bits of the user ID of the owner.
     * @return The user ID.
     */
    public short getUserID() {
        return (this.getShortFromBytes(2, iNodeBuffer));
    }

    /**
     * Retrieves the lower 32 bits of the file size, in bytes.
     * @return Lower 32 bit size of file in bytes.
     */
    public int getLowerFileSize() {
        return (this.getIntFromBytes(4, iNodeBuffer));
    }

    /**
     * Retrieves the last access time for the directory as a string.
     * @return String representation of last access time.
     */
    public String getLastAccessTime() {
        Date accessDate = new Date( (long) this.getIntFromBytes(8, iNodeBuffer) * 1000 );
        String formattedTime = new SimpleDateFormat("MMM dd HH:mm").format(accessDate);
        return (formattedTime);
    }

    /**
     * Retrieves the creation time for the directory as a string.
     * @return String representation of creation time.
     */
    public String getCreationTime() {
        Date creationDate = new Date( (long) this.getIntFromBytes(12, iNodeBuffer) * 1000 );
        String formattedTime = new SimpleDateFormat("MMM dd HH:mm").format(creationDate);
        return (formattedTime);
    }

    /**
     * Retrieves the last modified time for the directory as a string.
     * @return String representation of last modified time.
     */
    public String getLastModifiedTime() {
        Date modifiedDate = new Date( (long) this.getIntFromBytes(16, iNodeBuffer) * 1000 );
        String formattedTime = new SimpleDateFormat("MMM dd HH:mm").format(modifiedDate);
        return (formattedTime);
    }

    /**
     * Retrieves the deleted time for the directory as a string.
     * @return String representation of deleted time.
     */
    public String getDeletedTime() {
        int delTime = this.getIntFromBytes(20, iNodeBuffer);
        String deletedTime = (delTime == 0) ? "-" : new Date( (long) delTime * 1000 ).toString();
        return (deletedTime);
    }

    /**
     * Retrieves the lower 16 bits of the group ID of the owner. 
     * @return The group ID.
     */
    public int getGroupID() {
        return (this.getShortFromBytes(24, iNodeBuffer));
    }

    /**
     * Retrieves the number of hard links referencing the file.
     * @return The number of hard links.
     */
    public int getNumHardLinks() {
        return (this.getShortFromBytes(26, iNodeBuffer));
    }

    /**
     * Retrieves the 12 4-byte integer direct data block pointers.
     * @return An array of the 12 direct data block pointers.
     */
    public List<Integer> getDirectPointers() {
        int count = 0;
        List<Integer> directPointers = new ArrayList<Integer>();
        while (count < 12) {
            directPointers.add(this.getIntFromBytes(40 + (4*count), iNodeBuffer));
            count++;
        }
        return (directPointers);
    }

    public List<Integer> getIndirectTableBlockPointers(int blockNum) {

        int count = 0;
        byte[] indirectTableBytes = this.read(blockNum*superBlock.getBlockSize(), superBlock.getBlockSize());
        ByteBuffer indirectBuffer = ByteBuffer.wrap(indirectTableBytes);
        indirectBuffer.order(ByteOrder.LITTLE_ENDIAN);

        List<Integer> indirectPointers = new ArrayList<Integer>();

        while (count < indirectBlockSize) {
            indirectPointers.add(this.getIntFromBytes(4*count, indirectBuffer));
            count++;
        }

        return indirectPointers;
    }

    /**
     * Retrieves the 4-byte integer indirect data block pointer from the iNode.
     * @return The single-indirect data block pointer.
     */
    public int getIndirectPointer() {
        return (this.getIntFromBytes(88, iNodeBuffer));
    }

    /**
     * Retrieves the double-indirect integer data block pointer.
     * @return The double-indirect data block pointer.
     */
    public int getDoubleIndirectPointer() {
        return (this.getIntFromBytes(92, iNodeBuffer));
    }

    /**
     * Retrieves the triple-indirect integer data block pointer.
     * @return The triple-indirect data block pointer.
     */
    public int getTripleIndirectPointer() {
        return (this.getIntFromBytes(96, iNodeBuffer));
    }

    /**
     * Retrieves the upper 32 bits of the file size, in bytes.
     * @return Upper 32 bit size of file in bytes.
     */
    public int getUpperFileSize() {
        return (this.getIntFromBytes(140, iNodeBuffer));
    }
}