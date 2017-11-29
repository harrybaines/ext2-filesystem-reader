package coursework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SuperBlock extends DataBlock {

    private int totalInodes;
    private int totalBlocks;
    private int blockSize;
    private int blocksPerGroup;
    private int iNodesPerGroup;
    private String magicNumber;
    private int iNodeSize;
    private String volumeLbl;

    private int[] iNodeTablePointers;

    public SuperBlock(Volume vol) {
        super(vol);
        this.setSuperblockValues();

        getSuperblockBytes();
    }

    public void getSuperblockBytes() {

        // Obtain superblock bytes
        byte[] block = this.read(1 * this.getBlockSize(), this.getBlockSize());
        ByteBuffer byteBlockBuffer = ByteBuffer.wrap(block);
        byteBlockBuffer.order(ByteOrder.LITTLE_ENDIAN);

        iNodeTablePointers = this.getAllINodeTblPointers();
        
        // Obtain hex and ASCII values of bytes in superblock
        //Helper.dumpHexBytes(block);
    }

    /**
     * Obtains all the superblock fields using block group 0.
     * Each superblock field can then be accessed when required.
     */
    private void setSuperblockValues() {

        this.blockSize = 1024 * (int) Math.pow(2, this.getByteBuffer().getInt(1024 + 24));
        this.totalInodes = this.getByteBuffer().getInt(blockSize + 0);
        this.totalBlocks = this.getByteBuffer().getInt(blockSize + 4);
        this.blocksPerGroup = this.getByteBuffer().getInt(blockSize + 32);
        this.iNodesPerGroup = this.getByteBuffer().getInt(blockSize + 40);
        this.magicNumber = String.format("0x%02X", this.getByteBuffer().getInt(blockSize + 56));
        this.iNodeSize = this.getByteBuffer().getInt(blockSize + 88);
        this.volumeLbl = "";

        for (int i = 0; i < 16; i++) {
            int asciiInt = this.getByteBuffer().get(blockSize + 120 + i);
            volumeLbl += (char) asciiInt;
        }
    }

    /**
     * Retrieves an array of all iNode table pointers from the group descriptors.
     * The method uses block group 0 to access all group descriptors.
     *
     * @return The array of all integer iNode table pointers.
     */
    private int[] getAllINodeTblPointers() {

        // Obtains array of iNode table pointers from all group descriptors
        int numBlockGroups = (int) Math.ceil((double) this.getTotalBlocks() / (double) this.getBlocksPerGroup());

        GroupDescriptor[] groupDescs = new GroupDescriptor[numBlockGroups];
        int[] iNodeTablePointers = new int[numBlockGroups];

        // Finds the group descriptors using block group 0 (1024 bytes after superblock)
        byte[] groupDescBytes = this.read(2 * this.getBlockSize(), numBlockGroups * GroupDescriptor.GROUP_DESCRIPTOR_SIZE);
        ByteBuffer groupDescBuffer = ByteBuffer.wrap(groupDescBytes);
        groupDescBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int currentDesc = 0;

        // Create new GroupDescriptor instances to obtain iNode table pointers
        while (currentDesc < numBlockGroups) {
            groupDescs[currentDesc] = new GroupDescriptor(groupDescBuffer, currentDesc, this);
            iNodeTablePointers[currentDesc] = groupDescs[currentDesc].getINodeTblPointer();
            currentDesc++;
        }
        return iNodeTablePointers;
    }

    /**
     * Obtains the list of all iNode table pointers found in the group descriptors.
     * @return List of iNode table pointers.
     */
    public int[] getiNodeTablePointers() {
        return this.iNodeTablePointers;
    }

    public int getTotaliNodes() {
        return this.totalInodes;
    }

    public int getTotalBlocks() {
        return this.totalBlocks;
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int getBlocksPerGroup() {
        return this.blocksPerGroup;
    }

    public int getiNodesPerGroup() {
        return this.iNodesPerGroup;
    }

    public String getMagicNumber() {
        return this.magicNumber;
    }

    public int getiNodeSize() {
        return this.iNodeSize;
    }

    public String getVolumeLbl() {
        return this.volumeLbl;
    }

    public int getiNodeTableSize() {
        return (this.getiNodesPerGroup() * this.getiNodeSize() / (this.getBlockSize()));
    }
}