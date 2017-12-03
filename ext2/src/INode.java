package ext2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class INode extends DataBlock {

    private static final int[] fileModeCodes = { 0x0100, 0x0080, 0x0040, 0x0020, 0x0010,  /* File mode hex codes */
                                                0x0008, 0x0004, 0x0002, 0x0001 }; 

    private static final char[] permissions = { 'r', 'w', 'x' };    /* User/Group/Others can Read/Write/Execute */
                          
    private ByteBuffer iNodeBuffer;     /* Byte buffer to store the bytes in the iNode */
    private byte[] iNodeBytes;          /* Array to store all bytes in the iNode */

    private SuperBlock superBlock;      /* The super block reference to obtain file system characteristics */
    private int indirectBlockSize;      /* The size of each indirect block containing block pointers */
    private int iNodeTblPointer;        /* Pointer to an iNode table pointer */

    private int groupNum;               /* The group number this iNode belongs in */
    private int iNodeNumber;            /* The number for this iNode */
    private int iNodeOffset;            /* The offset, in bytes, the iNode is at in the filsystem */
    private int fileMode;               /* The file mode field for this iNode */

    /**
     * Constructor to initialise an iNode with the relevant fields.
     *
     * @param iNodeNumber The number of this iNode.
     * @param groupNum The group the iNode belongs to.
     * @param superBlock The reference to the super block for file system information.
     */
    public INode(int iNodeNumber, int iNodeTblPointer, int groupNum, SuperBlock superBlock) {

        super(superBlock.getVolume());

        // Initialise instance variables
        this.iNodeNumber = iNodeNumber;
        this.iNodeTblPointer = iNodeTblPointer;
        this.groupNum = groupNum;
        this.superBlock = superBlock;

        // Work out where iNode is in the file system (offset)
        iNodeOffset = (iNodeTblPointer * superBlock.getBlockSize()) + (((iNodeNumber-1) - (groupNum * superBlock.getiNodesPerGroup())) * superBlock.getiNodeSize());

        // Create array of bytes for this iNode
        iNodeBytes = this.readBlock(iNodeOffset, superBlock.getiNodeSize());

        // Wrap in buffer for later reading
        iNodeBuffer = ByteBuffer.wrap(iNodeBytes);
        iNodeBuffer.order(ByteOrder.LITTLE_ENDIAN);

        indirectBlockSize = superBlock.getBlockSize() / 4;
    }

    /**
     * Retrieves an array of bytes referenced by the direct pointers in this iNode.
     * @return Array of bytes for the data blocks being pointed to.
     */
    public byte[] getDataBlocksFromPointers() {

        int indirectionLevel = 0;

        // Get all data blocks using just direct pointers from this iNode
        List<Byte> allDataBlocks = this.getDataBlocks(this.getDirectPointers());

        // Find indirect pointers and search through blocks to find data blocks
        int indirectPointer = this.getIndirectPointer();                            // Initial indirect pointer in iNode
        int doubleIndirectPointer = this.getDoubleIndirectPointer();                // Initial double-indirect pointer in iNode
        int tripleIndirectPointer = this.getTripleIndirectPointer();                // Initial triple-indirect pointer in iNode

        // Level 1 of indirection (single indirection)
        if (indirectPointer != 0) {

            // Obtains initial list of indirect pointers to begin indirection traversal
            // Transfer all found data from indirect pointers into 'master' list
            for (byte b : this.getDataBlocks(this.getIndirectBlockPointers(indirectPointer)))
                allDataBlocks.add(b);
        }

        // Level 2 of indirection - (double indirection) MAKE MORE EFFICIENT!
        if (doubleIndirectPointer != 0) {

            // Obtains initial list of indirect pointers to begin indirection traversal            
            // Obtains all non-0 pointers from 3 levels of indirection 
            List<Integer> pointersFromIndirectionLevel = this.getPointersByIndirectionLevel(2, this.getIndirectBlockPointers(doubleIndirectPointer));

            // Transfer all found data from indirect pointers into 'master' list
            for (byte b : this.getDataBlocks(pointersFromIndirectionLevel))
                allDataBlocks.add(b);
        }

        // Level 3 of indirection - (triple indirection)
        if (tripleIndirectPointer != 0) {
           
            // Obtains initial list of indirect pointers to begin indirection traversal            
            // Obtains all non-0 pointers from 3 levels of indirection
            List<Integer> pointersFromIndirectionLevel = this.getPointersByIndirectionLevel(3, this.getIndirectBlockPointers(tripleIndirectPointer));

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

    /**
     * Method to traverse the file system given the level of indirection to traverse.
     * The method is given an initial list of indirection pointers to begin the traversal.
     *
     * @param indirectionLevel The levels of indirection to traverse.
     * @param indirectTableBlockPointers The initial dynamic list of pointers to begin traversal.
     * @return The dynamic list of pointers to blocks found from the traversal.
     */
    public List<Integer> getPointersByIndirectionLevel(int indirectionLevel, List<Integer> indirectTableBlockPointers) {

        // Stores all pointers found that aren't 0
        List<Integer> blockPointers = new ArrayList<Integer>();

        // Iterate over first block of pointers
        for (int i : indirectTableBlockPointers) {
            if (i != 0) {
                // Iterate over 2nd block of pointers (level 2) and get all pointers in the block it points to
                for (int j : this.getIndirectBlockPointers(i)) {
                    if (j != 0) {
                        // Add data to list if at level 2, otherwise ignore - find level 
                        if (indirectionLevel == 2)
                            blockPointers.add(j);
                        else if (indirectionLevel == 3) {
                            // Get level 3 block pointers and iterate over final block of pointers (level 3)
                            for (int k : this.getIndirectBlockPointers(j))
                                if (k != 0)
                                    blockPointers.add(k);
                        }
                    }
                }
            }   
        }
        return blockPointers;
    }

    /**
     * Obtains the data blocks given a set of block pointers.
     *
     * @param pointers Dynamic list of pointers that point to data blocks.
     * @return Dynamic byte list from data blocks.
     */
    public List<Byte> getDataBlocks(List<Integer> pointers) {

        // Create array of data blocks found from indirect pointers
        List<Byte> byteList = new ArrayList<Byte>();

        // Iterate over pointers provided to retrieve data blocks
        for (int i : pointers) {
            if (i != 0) {
                byte[] dataBlocksArray = this.readBlock(i * superBlock.getBlockSize(), superBlock.getBlockSize());
                for (int curBlock = 0; curBlock < dataBlocksArray.length; curBlock++) {
                    byteList.add(dataBlocksArray[curBlock]);
                }
            }
        }
        return byteList;
    }

    /**
     * Retrieves the 12 4-byte integer direct data block pointers, directly from the iNode.
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

    /**
     * Method to retrieve a dynamic list of indirect block pointers given a block number.
     * 
     * @param blockNum The number of the block to get indirect pointers from.
     * @return The dynamic list of indirect block pointers.
     */
    public List<Integer> getIndirectBlockPointers(int blockNum) {
        
        List<Integer> indirectPointers = new ArrayList<Integer>();

        // Obtains bytes for the indirect table of block pointers
        byte[] indirectTableBytes = this.readBlock(blockNum*superBlock.getBlockSize(), superBlock.getBlockSize());
        ByteBuffer indirectBuffer = ByteBuffer.wrap(indirectTableBytes);
        indirectBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Iterate over the indirect table block size
        int count = 0;
        while (count < indirectBlockSize) {
            indirectPointers.add(this.getIntFromBytes(4*count, indirectBuffer));
            count++;
        }

        return indirectPointers;
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

        // Obtain file mode read/write/execute permissions string - Directory or File
        String fileInfo = ((fileMode & 0x4000) > 0) ? "d" : "-";  

        // Iterate over possible file mode hex codes
        for (int i = 0; i < fileModeCodes.length; i++)
            fileInfo += ((fileMode & fileModeCodes[i]) > 0) ? permissions[i % permissions.length] : "-";

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
        return (this.getIntFromBytes(108, iNodeBuffer));
    }

    /**
     * Combines the upper and lower 32 bit file sizes into 1 64 bit file size.
     * @return The size of the file referenced by this iNode.
     */
    public long getTotalFileSize() {
        long totalSize = (long) this.getUpperFileSize() << 32 | this.getLowerFileSize() & 0xFFFFFFFFL;
        return totalSize;
    }

    /**
     * Method to retrieve the number of this iNode.
     * @return The iNode number.
     */
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
}