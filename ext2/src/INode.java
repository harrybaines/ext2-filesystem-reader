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

    /* CONSTANTS */
    public static final int[] fileModeCodes = { 0x4000, 0x0100, 0x0080, 0x0040, 0x0020, 
                                                0x0010, 0x0008, 0x0004, 0x0002, 0x0001 };   /* File mode hex codes */

    public static final char[] permissions = { 'r', 'w', 'x' };    /* User/Group/Others can Read/Write/Execute */

    /* OFFSETS */
    public static final int FILE_MODE_OFFSET       = 0;     /* Offset, in bytes, for the file mode */
    public static final int USER_ID_LOWER_OFFSET   = 2;     /* Offset, in bytes, for the lower 16 bits of the user ID */
    public static final int FILE_SIZE_LOWER_OFFSET = 4;     /* Offset, in bytes, for the lower 16 bits of the file size */
    public static final int LAST_ACCESS_OFFSET     = 8;     /* Offset, in bytes, for the last access time */
    public static final int CREATION_OFFSET        = 12;    /* Offset, in bytes, for the creation time */
    public static final int LAST_MODIFIED_OFFSET   = 16;    /* Offset, in bytes, for the last modified time */
    public static final int DELETED_OFFSET         = 20;    /* Offset, in bytes, for the deleted time */   
    public static final int GROUP_ID_LOWER_OFFSET  = 24;    /* Offset, in bytes, for the lower 16 bits of the group ID */
    public static final int HARD_LINKS_OFFSET      = 26;    /* Offset, in bytes, for the number of hard links */
    public static final int NUM_512_BLOCKS_OFFSET  = 28;    /* Offset, in bytes, for the number of 512 byte blocks */
    public static final int DIRECT_POINTERS_OFFSET = 40;    /* Offset, in bytes, for the direct pointers */   
    public static final int INDIRECT_OFFSET        = 88;    /* Offset, in bytes, for the indirect pointer */
    public static final int DBL_INDIRECT_OFFSET    = 92;    /* Offset, in bytes, for the double indirect pointer */
    public static final int TRPL_INDIRECT_OFFSET   = 96;    /* Offset, in bytes, for the triple indirect pointer */
    public static final int FILE_SIZE_UPPER_OFFSET = 108;   /* Offset, in bytes, for the upper 32 bits of the file size */
    public static final int BYTE_BLOCK_SIZE        = 512;   /* Total number of bytes for the number of 512 byte blocks field */

    /* INODE FIELDS */
    private short fileMode;                                 /* File mode field */
    private short userIDLower;                              /* Lower 16 bits of user ID field */
    private int fileSizeLower;                              /* Lower 16 bits of file size field */
    private String lastAccessTime;                          /* Last access time field */
    private String creationTime;                            /* Creation time field */
    private String lastModifiedTime;                        /* Last modified field */
    private String deletedTime;                             /* Deleted time field */
    private short groupIDLower;                             /* Lower 16 bits of group ID field */
    private short numHardLinks;                             /* Number of hard links field */
    private int num512ByteBlocks;                           /* Number of 512 byte blocks field */
    private List<Integer> directPointers;                   /* List of direct pointers field */
    private int singleIndirectP;                            /* Single indirect pointer field */
    private int doubleIndirectP;                            /* Double indirect pointer field */
    private int tripleIndirectP;                            /* Triple indirect pointer field */
    private int fileSizeUpper;                              /* Upper 16 bits of file size field */
    private long totalFileSize;                             /* Total file size */
    private int allocatedBlocks;                            /* Number of allocated bytes to file this iNode points to */
    private int unallocatedBlocks;                          /* Number of unused blocks for bytes in the file this iNode points to */
    private int zeros;                                      /* The total number of 0s allocated to this file if it's sparse */
    private int usedByteSize;                               /* The total number of bytes used so far in the file this iNode points to */

    private ByteBuffer iNodeBuffer;                         /* Byte buffer to store the bytes in the iNode */
    private byte[] iNodeBytes;                              /* Array to store all bytes in the iNode */

    private SuperBlock superBlock;                          /* The super block reference to obtain file system characteristics */
    private int indirectBlockSize;                          /* The size of each indirect block containing block pointers */
    private int iNodeTblPointer;                            /* Pointer to an iNode table pointer */

    private int groupNum;                                   /* The group number this iNode belongs in */
    private int iNodeNumber;                                /* The number for this iNode */
    private int iNodeOffset;                                /* The offset, in bytes, that the iNode is at in the filsystem */
    private int levelToReach;                               /* Final level of indirection to reach to obtain data block pointers */
    private List<Integer> blockPointers;                    /* Dynamic list of final data block pointers */

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
        this.iNodeOffset = this.calculateiNodeByteOffset(iNodeTblPointer, iNodeNumber, groupNum);

        // Create array of bytes for this iNode
        iNodeBytes = this.readBlock(iNodeOffset, superBlock.getiNodeSize());

        // Wrap in buffer for later reading
        iNodeBuffer = ByteBuffer.wrap(iNodeBytes);
        iNodeBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Set the indirect block size
        indirectBlockSize = superBlock.getBlockSize() / Integer.BYTES;

        // Initialise all the iNode fields
        this.initINodeFields();
    }

    /**
     * Method to initialise all the relevant iNode fields upon instantiation of this iNode.
     */
    private void initINodeFields() {
        
        this.fileMode         = iNodeBuffer.getShort(FILE_MODE_OFFSET);
        this.userIDLower      = iNodeBuffer.getShort(USER_ID_LOWER_OFFSET);
        this.fileSizeLower    = iNodeBuffer.getInt(FILE_SIZE_LOWER_OFFSET);

        this.lastAccessTime   = new SimpleDateFormat("MMM dd HH:mm").format(new Date((long) iNodeBuffer.getInt(LAST_ACCESS_OFFSET) * 1000));
        this.creationTime     = new SimpleDateFormat("MMM dd HH:mm").format(new Date((long) iNodeBuffer.getInt(CREATION_OFFSET) * 1000));
        this.lastModifiedTime = new SimpleDateFormat("MMM dd HH:mm").format(new Date((long) iNodeBuffer.getInt(LAST_MODIFIED_OFFSET) * 1000));
        this.deletedTime      = (iNodeBuffer.getInt(DELETED_OFFSET) == 0) ? "-" : new Date((long) iNodeBuffer.getInt(DELETED_OFFSET) * 1000).toString();

        this.groupIDLower     = iNodeBuffer.getShort(GROUP_ID_LOWER_OFFSET);
        this.numHardLinks     = iNodeBuffer.getShort(HARD_LINKS_OFFSET);
        this.num512ByteBlocks = iNodeBuffer.getInt(NUM_512_BLOCKS_OFFSET);

        this.directPointers   = this.getDirectPointers();
        this.singleIndirectP  = iNodeBuffer.getInt(INDIRECT_OFFSET);
        this.doubleIndirectP  = iNodeBuffer.getInt(DBL_INDIRECT_OFFSET);
        this.tripleIndirectP  = iNodeBuffer.getInt(TRPL_INDIRECT_OFFSET);
        this.fileSizeUpper    = iNodeBuffer.getInt(FILE_SIZE_UPPER_OFFSET);
        this.totalFileSize    = ((long) this.getUpperFileSize() << 32 | this.getLowerFileSize() & 0xFFFFFFFFL);
    }

    /**
     * Method to calculate the offset in bytes where the iNode is located in the filesystem.
     *
     * @param iNodeTblPointer The pointer to the iNode table in which this iNode belongs.
     * @param iNodeNumber The number of this iNode.
     * @param groupNum The block group in which this iNode belongs.
     * @return The offset in bytes in the filesystem for this iNode.
     */
    public int calculateiNodeByteOffset(int iNodeTblPointer, int iNodeNumber, int groupNum) {
        return ((iNodeTblPointer * superBlock.getBlockSize()) + (((iNodeNumber-1) - (groupNum * superBlock.getiNodesPerGroup())) * superBlock.getiNodeSize()));
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
        List<Byte> allDataBlocks = this.getDataBlocks(this.directPointers);

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
            if (b != 0)
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
                for (int curByte = 0; curByte < dataBlocksArray.length; curByte++) {
                    byteList.add(dataBlocksArray[curByte]);
                }
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

        // Transfer direct pointers to dynamic list
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
     * Method to set the total number of unused blocks for bytes in the file this iNode points to.
     * @param blocks The number of unused blocks.
     */
    public void setUnusedBlocks(int blocks) {
        this.unallocatedBlocks = blocks;
    }

    /**
     * Method to obtain the total number of unused blocks for bytes in the file this iNode points to.
     * @return The total number of unused blocks.
     */
    public int getUnusedBlocks() {
        return this.unallocatedBlocks;
    }

    /**
     * Method to set the total number of blocks allocated for bytes in the file this iNode points to.
     * @param blocks The number of allocated blocks.
     */
    public void setAllocatedBlocks(int blocks) {
        this.allocatedBlocks = blocks;
    }

    /**
     * Method to obtain the total number of blocks allocated for bytes in the file this iNode points to.
     * @return The total number of allocated blocks.
     */
    public int getAllocatedBlocks() {
        return this.allocatedBlocks;
    }

    /**
     * Method to set the total number of bytes used in the file this iNode points to.
     * @param size The total number of bytes used.
     */
    public void setUsedByteSize(int size) {
        this.usedByteSize = size;
    }

    /**
     * Method to obtain the total number of bytes used in the file this iNode points to.
     * @return The total number of bytes used.
     */
    public int getUsedByteSize() {
        return this.usedByteSize;
    }

    /**
     * Method to obtain the total number of 0s allocated for this file if it is sparse.
     * @return The total number of 0s.
     */
    public int getZeroCount() {
        return this.zeros;
    }

    /**
     * Method to set the total number of 0s that have been allocated to this file.
     * @param zeros The total number of 0s.
     */
    public void setZeroCount(int zeros) {
        this.zeros = zeros;
    }

    /**
     * Retrieves the file mode field for the iNode and returns a string representation.
     * Used in Unix-like directory listings, displaying permissions, directory info etc.
     *
     * @return String representation of the file mode.
     */
    public String getFileModeAsString() {

        // Obtain short file mode
        short fileMode = this.getFileMode();

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
        return this.fileMode;
    }

    /**
     * Retrieves the lower 16 bits of the user ID of the owner.
     * @return The user ID.
     */
    public short getUserID() {
        return this.userIDLower;
    }

    /**
     * Retrieves the lower 32 bits of the file size, in bytes.
     * @return Lower 32 bit size of file in bytes.
     */
    public int getLowerFileSize() {
        return this.fileSizeLower;
    }

    /**
     * Retrieves the last access time for the directory as a string.
     * @return String representation of last access time.
     */
    public String getLastAccessTime() {
        return this.lastAccessTime;
    }

    /**
     * Retrieves the creation time for the directory as a string.
     * @return String representation of creation time.
     */
    public String getCreationTime() {
        return this.creationTime;
    }

    /**
     * Retrieves the last modified time for the directory as a string.
     * @return String representation of last modified time.
     */
    public String getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    /**
     * Retrieves the deleted time for the directory as a string.
     * @return String representation of deleted time.
     */
    public String getDeletedTime() {
        return this.deletedTime;
    }

    /**
     * Retrieves the lower 16 bits of the group ID of the owner. 
     * @return The group ID.
     */
    public int getGroupID() {
        return this.groupIDLower;
    }

    /**
     * Retrieves the number of hard links referencing the file.
     * @return The number of hard links.
     */
    public int getNumHardLinks() {
        return this.numHardLinks;
    }

    /**
     * Retrieves the number of 512 byte blocks that make up this file.
     * @return The number of 512 byte blocks.
     */
    public int getNum512ByteBlocks() {
        return this.num512ByteBlocks;
    }

    /**
     * Retrieves the 4-byte integer indirect data block pointer from the iNode.
     * @return The single-indirect data block pointer.
     */
    public int getIndirectPointer() {
        return this.singleIndirectP;
    }

    /**
     * Retrieves the double-indirect integer data block pointer.
     * @return The double-indirect data block pointer.
     */
    public int getDoubleIndirectPointer() {
        return this.doubleIndirectP;
    }

    /**
     * Retrieves the triple-indirect integer data block pointer.
     * @return The triple-indirect data block pointer.
     */
    public int getTripleIndirectPointer() {
        return this.tripleIndirectP;
    }

    /**
     * Retrieves the upper 32 bits of the file size, in bytes.
     * @return Upper 32 bit size of file in bytes.
     */
    public int getUpperFileSize() {
        return this.fileSizeUpper;
    }

    /**
     * Combines the upper and lower 32 bit file sizes into 1 64 bit file size.
     * @return The size of the file referenced by this iNode.
     */
    public long getTotalFileSize() {
        return this.totalFileSize;
    }

    /**
     * Method to retrieve the number of this iNode.
     * @return The iNode number.
     */
    public int getINodeNumber() {
        return this.iNodeNumber;
    }

    /** 
     * Method to obtain all iNode fields in the form of a byte array.
     * @return Array of bytes containing the iNode fields.
     */
    public byte[] getINodeInfoBytes() {
        return this.iNodeBytes;
    }
}