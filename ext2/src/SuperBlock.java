package ext2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Name: SuperBlock
 * 
 * This class represents the super block in the file system.
 * This super block contains all the relevant information about the file system and multiple copies are made throughout the file system.
 * The key fields are stored in this class and can be accessed via the relevant accessor methods.
 *
 * @author Harry Baines
 * @see DataBlock
 */
public class SuperBlock extends DataBlock {

    public static final int NUM_INODE_OFFSET = 0;             /* Offset, in bytes, in super block for number of iNodes */
    public static final int NUM_BLOCKS_OFFSET = 4;            /* Offset, in bytes, in super block for number of blocks */
    public static final int BLOCK_SIZE_OFFSET = 24;           /* Offset, in bytes, in super block for block size */
    public static final int BLOCKS_PER_GROUP_OFFSET = 32;     /* Offset, in bytes, in super block for blocks per block group */
    public static final int INODES_PER_GROUP_OFFSET = 40;     /* Offset, in bytes, in super block for iNodes per block group */
    public static final int MAGIC_NUM_OFFSET = 56;            /* Offset, in bytes, in super block for magic number */
    public static final int INODE_SIZE_OFFSET = 88;           /* Offset, in bytes, in super block for iNode size */
    public static final int VOLUME_LBL_OFFSET = 120;          /* Offset, in bytes, in super block for volume label */

    private int totaliNodes;                                  /* The total number of iNodes in the file system */
    private int totalBlocks;                                  /* The total number of blocks in the file system */
    private int blockSize;                                    /* The size of each block in the file system, in bytes */
    private int blocksPerGroup;                               /* The number of blocks per block group in the file system */
    private int iNodesPerGroup;                               /* The number of iNodes per block group in the file system */
    private String magicNumber;                               /* The magic number which uniquely identifies the file system type */
    private int iNodeSize;                                    /* The size of each iNode in the file system, in bytes */
    private String volumeLbl;                                 /* The volume label relevant to this file system, as a string */

    private ByteBuffer byteBuffer;                            /* Byte buffer reference for setting super block values */
    private int[] iNodeTablePointers;                         /* Array of all the iNode table pointers - the super block fields aid in it's construction */

    /**
     * Constructor to initialise a super block in a given volume.
     * @param vol The volume in which the super block is located.
     */
    public SuperBlock(Volume vol) {
        super(vol);
        this.byteBuffer = this.getVolume().getByteBuffer();
        this.setSuperblockValues();
        this.iNodeTablePointers = this.getAllINodeTblPointers();
    }

    /**
     * Obtains all the superblock fields using block group 0.
     * Each superblock field can then be accessed when required using relevant accessor methods.
     */
    private void setSuperblockValues() {

        // Initialise all super block fields
        this.blockSize      = 1024 * (int) Math.pow(2, this.byteBuffer.getInt(1024 + BLOCK_SIZE_OFFSET));
        this.totaliNodes    = this.getIntFromBytes(this.blockSize + NUM_INODE_OFFSET, this.byteBuffer);
        this.totalBlocks    = this.byteBuffer.getInt(this.blockSize + NUM_BLOCKS_OFFSET);
        this.blocksPerGroup = this.byteBuffer.getInt(this.blockSize + BLOCKS_PER_GROUP_OFFSET);
        this.iNodesPerGroup = this.byteBuffer.getInt(this.blockSize + INODES_PER_GROUP_OFFSET);
        this.magicNumber    = String.format("0x%02X", this.byteBuffer.getShort(this.blockSize + MAGIC_NUM_OFFSET));
        this.iNodeSize      = this.byteBuffer.getInt(this.blockSize + INODE_SIZE_OFFSET);
        this.volumeLbl      = "";

        // Initialise volume label
        for (int i = 0; i < 16; i++)
            this.volumeLbl += (char) this.byteBuffer.get(this.blockSize + VOLUME_LBL_OFFSET + i);
    }

    /**
     * Retrieves an array of all iNode table pointers from the group descriptors.
     * The method uses block group 0 to access all group descriptors and uses relevant fields in this class to find the pointers.
     *
     * @return The array of all integer iNode table pointers.
     */
    private int[] getAllINodeTblPointers() {

        // Obtains array of iNode table pointers from all group descriptors
        int numBlockGroups = (int) Math.ceil((double) this.totalBlocks / (double) this.blocksPerGroup);

        // Array of iNode table pointers
        int[] iNodeTablePointers = new int[numBlockGroups];

        // Finds the group descriptors using block group 0 (1024 bytes after superblock)
        byte[] groupDescBytes = this.readBlock(2 * this.getBlockSize(), numBlockGroups * GroupDescriptor.GROUP_DESCRIPTOR_SIZE);
        ByteBuffer groupDescBuffer = ByteBuffer.wrap(groupDescBytes);
        groupDescBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int currentDesc = 0;

        // Create new GroupDescriptor instances to obtain iNode table pointers
        while (currentDesc < numBlockGroups) {
            iNodeTablePointers[currentDesc] = new GroupDescriptor(groupDescBuffer, currentDesc, this).getINodeTblPointer();
            currentDesc++;
        }
        return iNodeTablePointers;
    }

    /**
     * Returns a String containing all relevant information calclulated from all the super block fields.
     * @return A String of super block information.
     */
    public String getSuperBlockInfo() {
        String superBlockString = "";
        superBlockString += "Total number of iNodes:      "  + this.getTotaliNodes() + "\n";
        superBlockString += "Total number of blocks:      "  + this.getTotalBlocks() + "\n";
        superBlockString += "Block size (bytes):          "  + this.getBlockSize() + "\n";
        superBlockString += "No. of blocks per group:     "  + this.getBlocksPerGroup() + "\n";
        superBlockString += "No. of iNodes per group:     "  + this.getiNodesPerGroup() + "\n";
        superBlockString += "Magic number:                "  + this.getMagicNumber() + "\n";
        superBlockString += "Size of each iNode (bytes):  "  + this.getiNodeSize() + "\n";
        superBlockString += "Volume label (disk name):    '" + this.getVolumeLbl() + "'\n";
        return superBlockString;
    }

    /**
     * Returns a String containing further information calclulated from the super block fields.
     * @return A String of further information from the super block.
     */
    public String getFurtherSuperBlockInfo() {
        String furtherInfo = "";
        furtherInfo += "iNode table size (blocks):   " + this.getiNodeTableSize() + "\n";
        furtherInfo += "iNode table size (bytes):    " + this.getiNodeTableSize() * this.getBlockSize() + "\n";
        furtherInfo += "Total no. of block groups:   " + (int) Math.ceil((double)this.getTotalBlocks() / this.getBlocksPerGroup()) + "\n";
        furtherInfo += "Number of group descriptors: " + (int) Math.ceil((double)this.getTotalBlocks() / this.getBlocksPerGroup()) + "\n";
        furtherInfo += "Total volume size (bytes):   " + this.getTotalBlocks() * this.getBlockSize() + "\n";
        return furtherInfo;
    }

    /**
     * Obtains the list of all iNode table pointers found in the group descriptors.
     * @return List of iNode table pointers.
     */
    public int[] getiNodeTablePointers() {
        return this.iNodeTablePointers;
    }

    /**
     * Obtains the size of each iNode table in the file system.
     * @return The iNode table size.
     */
    public int getiNodeTableSize() {
        return (this.getiNodesPerGroup() * this.getiNodeSize() / (this.getBlockSize()));
    }

    /**
     * Obtains the total number of iNodes in the file system.
     * @return The number of iNodes.
     */
    public int getTotaliNodes() {
        return this.totaliNodes;
    }

    /**
     * Obtains the total number of blocks in the file system.
     * @return The number of blocks.
     */
    public int getTotalBlocks() {
        return this.totalBlocks;
    }

    /**
     * Obtains the block size for each block in the file system.
     * @return The block size.
     */
    public int getBlockSize() {
        return this.blockSize;
    }

    /**
     * Obtains the total number of blocks per block group in the file system.
     * @return The number of blocks per block group.
     */
    public int getBlocksPerGroup() {
        return this.blocksPerGroup;
    }

    /**
     * Obtains the total number of iNodes per block group in the file system.
     * @return The number of iNodes per block group.
     */
    public int getiNodesPerGroup() {
        return this.iNodesPerGroup;
    }

    /**
     * Obtains the magic number.
     * @return The masgic number as a String.
     */
    public String getMagicNumber() {
        return this.magicNumber;
    }

    /**
     * Obtains the size of each iNode in the file system.
     * @return The size of each iNode.
     */
    public int getiNodeSize() {
        return this.iNodeSize;
    }

    /**
     * Obtains the volume label.
     * @return The volume label as a String.
     */
    public String getVolumeLbl() {
        return this.volumeLbl;
    }
}