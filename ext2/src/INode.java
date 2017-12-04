package ext2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.List;
import java.util.ArrayList;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Name: iNode
 * 
 * This class represents an iNode in the filesystem.
 * Every file can be located by it's respective iNode and contains metadata about the file.
 * The iNode contains pointers to the blocks which are represented by the iNode.
 *
 * @author Harry Baines
 * @see DataBlock
 */
public class INode extends DataBlock {

    public static final int[] fileModeCodes = { 0x4000, 0x0100, 0x0080, 0x0040, 0x0020, 
                                                0x0010, 0x0008, 0x0004, 0x0002, 0x0001 };   /* File mode hex codes */

    public static final char[] permissions = { 'r', 'w', 'x' };    /* User/Group/Others can Read/Write/Execute */

    public static final int FILE_MODE_OFFSET       = 0;     /* Offset, in bytes, for the file mode */
    public static final int USER_ID_LOWER_OFFSET   = 2;     /* Offset, in bytes, for the lower 16 bits of the user ID */
    public static final int FILE_SIZE_LOWER_OFFSET = 4;     /* Offset, in bytes, for the lower 16 bits of the file size */
    public static final int LAST_ACCESS_OFFSET     = 8;     /* Offset, in bytes, for the last access time */
    public static final int CREATION_OFFSET        = 12;    /* Offset, in bytes, for the creation time */
    public static final int LAST_MODIFIED_OFFSET   = 16;    /* Offset, in bytes, for the last modified time */
    public static final int DELETED_OFFSET         = 20;    /* Offset, in bytes, for the deleted time */   
    public static final int GROUP_ID_LOWER_OFFSET  = 24;    /* Offset, in bytes, for the lower 16 bits of the group ID */
    public static final int HARD_LINKS_OFFSET      = 26;    /* Offset, in bytes, for the number of hard links */
    public static final int DIRECT_POINTERS_OFFSET = 40;    /* Offset, in bytes, for the direct pointers */   
    public static final int INDIRECT_OFFSET        = 88;    /* Offset, in bytes, for the indirect pointer */
    public static final int DBL_INDIRECT_OFFSET    = 92;    /* Offset, in bytes, for the double indirect pointer */
    public static final int TRPL_INDIRECT_OFFSET   = 96;    /* Offset, in bytes, for the triple indirect pointer */
    public static final int FILE_SIZE_UPPER_OFFSET = 108;   /* Offset, in bytes, for the upper 32 bits of the file size */
        
    private ByteBuffer iNodeBuffer;                         /* Byte buffer to store the bytes in the iNode */
    private byte[] iNodeBytes;                              /* Array to store all bytes in the iNode */

    private SuperBlock superBlock;                          /* The super block reference to obtain file system characteristics */
    private int indirectBlockSize;                          /* The size of each indirect block containing block pointers */
    private int iNodeTblPointer;                            /* Pointer to an iNode table pointer */

    private int groupNum;                                   /* The group number this iNode belongs in */
    private int iNodeNumber;                                /* The number for this iNode */
    private int iNodeOffset;                                /* The offset, in bytes, that the iNode is at in the filsystem */
    private int levelToReach;
    private List<Integer> blockPointers;

    /**
     * Constructor to initialise an iNode with the relevant fields.
     *
     * @param iNodeNumber The number of this iNode.
     * @param iNodeTblPointer The pointer to the iNode table in which this iNode belongs.
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

        // Set the indirect block size
        indirectBlockSize = superBlock.getBlockSize() / Integer.BYTES;
    }

    /**
     * Retrieves an array of bytes referenced by the direct pointers in this iNode.
     * If the indirection pointer fields are not empty, a recursive method is called to obtain data block pointers.
     *
     * @return Array of bytes for the data blocks being pointed to.
     */
    public byte[] getDataBlocksFromPointers() {

        int indirectionLevel = 0;

        // Get all data blocks using just direct pointers from this iNode
        List<Byte> allDataBlocks = this.getDataBlocks(this.getDirectPointers());

        // Find indirect pointers and search through blocks to find data blocks
        int singleIndirectPointer = this.getIndirectPointer();                    // Initial indirect pointer in iNode
        int doubleIndirectPointer = this.getDoubleIndirectPointer();              // Initial double-indirect pointer in iNode
        int tripleIndirectPointer = this.getTripleIndirectPointer();              // Initial triple-indirect pointer in iNode

        // Single indirection
        if (singleIndirectPointer != 0)
            this.addDataBlocksForInitialPointer(singleIndirectPointer, 1, allDataBlocks);

        // Double indirection
        if (doubleIndirectPointer != 0)
            this.addDataBlocksForInitialPointer(doubleIndirectPointer, 2, allDataBlocks);

        // Triple indirection
        if (tripleIndirectPointer != 0)
            this.addDataBlocksForInitialPointer(tripleIndirectPointer, 3, allDataBlocks);

        // Transfer dynamic array list into array to return
        byte[] byteArray = new byte[allDataBlocks.size()];
        for (int i = 0; i < allDataBlocks.size(); i++)
            byteArray[i] = allDataBlocks.get(i);

        return byteArray;
    }

    /**
     * Method which adds data blocks to the passed dynamic list of bytes.
     * This method first calls getPointersByIndirectionLevel() based on the level passed.
     * Then using these pointers, the relevant data blocks are added to the dynamic list.
     *
     * @param indirectPointer The initial indirect pointer that points to the first block of indirect pointers.
     * @param indLevel The level of indirection the method should traverse up to.
     * @param allDataBlocks The dynamic list of data bytes from data blocks.
     */
    private void addDataBlocksForInitialPointer(int indirectPointer, int indLevel, List<Byte> allDataBlocks) {

        this.levelToReach = indLevel;

        // Stores all pointers found that aren't 0
        blockPointers = new ArrayList<Integer>();

        // Obtains all non-0 pointers from 'indLevel' levels of indirection, given an initial indirect pointer 
        List<Integer> pointersFromIndirectionLevel = this.getPointersByIndirectionLevel(1, this.getIndirectBlockPointers(indirectPointer));

        // Transfer all found data from indirect pointers into 'master' list
        for (byte b : this.getDataBlocks(pointersFromIndirectionLevel))
            allDataBlocks.add(b);
    }

    /**
     * Method to traverse the file system given the an initial level of indirection.
     * This method is recursive and is given an initial list of indirection pointers to begin the traversal.
     *
     * @param indirectionLevel The levels of indirection to traverse.
     * @param indirectTableBlockPointers The initial dynamic list of pointers to begin traversal.
     * @return The dynamic list of pointers to blocks found from the traversal.
     */
    private List<Integer> getPointersByIndirectionLevel(int indirectionLevel, List<Integer> indirectTableBlockPointers) {

        // Iterate over block of pointers at given level of recursion
        for (int p : indirectTableBlockPointers) {

            // If pointer points to data
            if (p != 0) {

                indirectionLevel++;

                // Add data block pointer to final list
                if (indirectionLevel > levelToReach) 
                    blockPointers.add(p);

                // Recurse until final indirection level is met
                else
                    getPointersByIndirectionLevel(indirectionLevel, this.getIndirectBlockPointers(p));
            }   
        }
        return blockPointers;
    }

    /**
     * Obtains a dynamic list of data blocks given a set of block pointers.
     *
     * @param pointers Dynamic list of pointers that point to data blocks.
     * @return Dynamic byte list from data blocks.
     */
    private List<Byte> getDataBlocks(List<Integer> pointers) {

        // Create array of data blocks found from indirect pointers
        List<Byte> byteList = new ArrayList<Byte>();

        // Iterate over pointers provided to retrieve data blocks
        for (int i : pointers) {
            if (i != 0) {
                byte[] dataBlocksArray = this.readBlock(i * superBlock.getBlockSize(), superBlock.getBlockSize());
                for (int curBlock = 0; curBlock < dataBlocksArray.length; curBlock++)
                    byteList.add(dataBlocksArray[curBlock]);
            }
        }
        return byteList;
    }

    /**
     * Retrieves the 12 4-byte integer direct data block pointers, directly from the iNode.
     * @return An array of the 12 direct data block pointers.
     */
    private List<Integer> getDirectPointers() {

        List<Integer> directPointers = new ArrayList<Integer>();

        int count = 0;
        while (count < 12) {
            directPointers.add(iNodeBuffer.getInt(DIRECT_POINTERS_OFFSET + (Integer.BYTES * count)));
            count++;
        }
        return directPointers;
    }

    /**
     * Method to retrieve a dynamic list of indirect block pointers given a block number.
     * 
     * @param blockNum The number of the block to get indirect pointers from.
     * @return The dynamic list of indirect block pointers.
     */
    private List<Integer> getIndirectBlockPointers(int blockNum) {
        
        List<Integer> indirectPointers = new ArrayList<Integer>();

        // Obtains bytes for the indirect table of block pointers
        byte[] indirectTableBytes = this.readBlock(blockNum * superBlock.getBlockSize(), superBlock.getBlockSize());
        ByteBuffer indirectBuffer = ByteBuffer.wrap(indirectTableBytes);
        indirectBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Iterate over the indirect table block size
        int count = 0;
        while (count < indirectBlockSize) {
            indirectPointers.add(indirectBuffer.getInt(Integer.BYTES * count));
            count++;
        }
        return indirectPointers;
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
        String fileInfo = ((fileMode & fileModeCodes[0]) > 0) ? "d" : "-";  

        // Iterate over possible file mode hex codes
        for (int i = 1; i < fileModeCodes.length; i++)
            fileInfo += ((fileMode & fileModeCodes[i]) > 0) ? permissions[(i-1) % permissions.length] : "-";

        return fileInfo;
    }

    /**
     * Retrieves the file mode for the given iNode as a string.
     * @return A string representation of the file mode.
     */
    public short getFileMode() {
        return (iNodeBuffer.getShort(FILE_MODE_OFFSET));
    }

    /**
     * Retrieves the lower 16 bits of the user ID of the owner.
     * @return The user ID.
     */
    public short getUserID() {
        return (iNodeBuffer.getShort(USER_ID_LOWER_OFFSET));
    }

    /**
     * Retrieves the lower 32 bits of the file size, in bytes.
     * @return Lower 32 bit size of file in bytes.
     */
    public int getLowerFileSize() {
        return (iNodeBuffer.getInt(FILE_SIZE_LOWER_OFFSET));
    }

    /**
     * Retrieves the last access time for the directory as a string.
     * @return String representation of last access time.
     */
    public String getLastAccessTime() {
        Date accessDate = new Date( (long) iNodeBuffer.getInt(LAST_ACCESS_OFFSET) * 1000 );
        String formattedTime = new SimpleDateFormat("MMM dd HH:mm").format(accessDate);
        return formattedTime;
    }

    /**
     * Retrieves the creation time for the directory as a string.
     * @return String representation of creation time.
     */
    public String getCreationTime() {
        Date creationDate = new Date( (long) iNodeBuffer.getInt(CREATION_OFFSET) * 1000 );
        String formattedTime = new SimpleDateFormat("MMM dd HH:mm").format(creationDate);
        return formattedTime;
    }

    /**
     * Retrieves the last modified time for the directory as a string.
     * @return String representation of last modified time.
     */
    public String getLastModifiedTime() {
        Date modifiedDate = new Date( (long) iNodeBuffer.getInt(LAST_MODIFIED_OFFSET) * 1000 );
        String formattedTime = new SimpleDateFormat("MMM dd HH:mm").format(modifiedDate);
        return formattedTime;
    }

    /**
     * Retrieves the deleted time for the directory as a string.
     * @return String representation of deleted time.
     */
    public String getDeletedTime() {
        int delTime = iNodeBuffer.getInt(DELETED_OFFSET);
        String deletedTime = (delTime == 0) ? "-" : new Date( (long) delTime * 1000 ).toString();
        return deletedTime;
    }

    /**
     * Retrieves the lower 16 bits of the group ID of the owner. 
     * @return The group ID.
     */
    public int getGroupID() {
        return (iNodeBuffer.getShort(GROUP_ID_LOWER_OFFSET));
    }

    /**
     * Retrieves the number of hard links referencing the file.
     * @return The number of hard links.
     */
    public int getNumHardLinks() {
        return (iNodeBuffer.getShort(HARD_LINKS_OFFSET));
    }

    /**
     * Retrieves the 4-byte integer indirect data block pointer from the iNode.
     * @return The single-indirect data block pointer.
     */
    public int getIndirectPointer() {
        return (iNodeBuffer.getInt(INDIRECT_OFFSET));
    }

    /**
     * Retrieves the double-indirect integer data block pointer.
     * @return The double-indirect data block pointer.
     */
    public int getDoubleIndirectPointer() {
        return (iNodeBuffer.getInt(DBL_INDIRECT_OFFSET));
    }

    /**
     * Retrieves the triple-indirect integer data block pointer.
     * @return The triple-indirect data block pointer.
     */
    public int getTripleIndirectPointer() {
        return (iNodeBuffer.getInt(TRPL_INDIRECT_OFFSET));
    }

    /**
     * Retrieves the upper 32 bits of the file size, in bytes.
     * @return Upper 32 bit size of file in bytes.
     */
    public int getUpperFileSize() {
        return (iNodeBuffer.getInt(FILE_SIZE_UPPER_OFFSET));
    }

    /**
     * Combines the upper and lower 32 bit file sizes into 1 64 bit file size.
     * @return The size of the file referenced by this iNode.
     */
    public long getTotalFileSize() {
        return ( (long) this.getUpperFileSize() << 32 | this.getLowerFileSize() & 0xFFFFFFFFL );
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