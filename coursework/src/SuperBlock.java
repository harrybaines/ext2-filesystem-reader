package coursework;

import java.nio.ByteBuffer;

public class SuperBlock extends DataBlock {

	private int totalInodes;
	private int totalBlocks;
	private int blockSize;
	private int blocksPerGroup;
	private int iNodesPerGroup;
	private String magicNumber;
	private int iNodeSize;
	private String volumeLbl;

	public SuperBlock(Volume vol) {
		super(vol);
		this.setSuperblockValues();
	}

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
            int asciiInt = this.getByteBuffer().get(blockSize + 120 + i) & 0xFF;
            volumeLbl += (char) asciiInt;
        }
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

	/** 
     * Method to print all information containined within the superblock.
     * The superblock holds characteristics of the filesystem.
     */
    public void printSuperblockInfo() {
        System.out.println("--------------------");
        System.out.println("Superblock Information:");
        System.out.println("--------------------");
        System.out.println("Total number of inodes:      " + this.getTotaliNodes());
        System.out.println("Total number of blocks:      " + this.getTotalBlocks());
        System.out.println("Block size (bytes):          " + this.getBlockSize());
        System.out.println("No. of blocks per group:     " + this.getBlocksPerGroup());
        System.out.println("No. of inodes per group:     " + this.getiNodesPerGroup());
        System.out.println("Magic number:                " + this.getMagicNumber());
        System.out.println("Size of each inode (bytes):  " + this.getiNodeSize());
        System.out.println("Volume label (disk name):    " + this.getVolumeLbl());
        System.out.println("--------------------\n");
    }


}