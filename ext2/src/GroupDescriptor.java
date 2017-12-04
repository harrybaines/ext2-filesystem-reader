package ext2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Name: GroupDescriptor
 * 
 * This class represents a group descriptor in the file system.
 * There are 'n' group descriptors in the file system.
 * Each block group in the file system is represented by its corresponding group descriptor.
 * Group descriptors can be used to obtain the iNode table pointer to find the iNode table from the block group.
 *
 * @author Harry Baines
 * @see DataBlock
 */
public class GroupDescriptor extends DataBlock {

	public static final int BLOCK_BITMAP_POINTER_OFFSET = 0;	/* Offset, in bytes, for the block bitmap pointer */
	public static final int INODE_BITMAP_POINTER_OFFSET = 4;	/* Offset, in bytes, for the iNode bitmap pointer */
	public static final int INODE_TBL_POINTER_OFFSET = 8;		/* Offset, in bytes, for the iNode table pointer */
	public static final int FREE_BLOCK_COUNT_OFFSET = 12;		/* Offset, in bytes, for the free block count */
	public static final int FREE_INODE_COUNT_OFFSET = 14;		/* Offset, in bytes, for the free iNode count */
	public static final int USED_DIRS_COUNT_OFFSET = 16;		/* Offset, in bytes, for the used dirs count */
	public static final int GROUP_DESCRIPTOR_SIZE = 32;			/* The total size of each group descriptor in the file system */

	private ByteBuffer groupDescBuffer;							/* Byte buffer to store bytes for each group descriptor instance */
	private int groupNum;										/* The group number for which this group descriptor references */
	
	private int blockBitmapPointer;								/* Block bitmap pointer field */
	private int iNodeBitmapPointer;								/* iNode bitmap pointer field */
	private int iNodeTblPointer;								/* iNode table pointer field */
	private short freeBlockCount;								/* Free block count field */
	private short freeiNodeCount;								/* Free iNode count field */
	private short usedDirsCount;								/* Used directories count field */

	/**
	 * Constructor to initialise a group descriptor.
	 * Each group descriptor has it's own iNode table pointer to reference the iNode table.
	 *
	 * @param groupDescBuffer The byte buffer to hold all bytes for this group descriptor.
	 * @param groupNum The group number which corresponds to this group descriptor.
	 * @param superBlock The reference to the super block for file system information.
	 */
	public GroupDescriptor(ByteBuffer groupDescBuffer, int groupNum, SuperBlock superBlock) {
		super(superBlock.getVolume());
		this.groupDescBuffer = groupDescBuffer;
		this.groupNum = groupNum;

		// Initialise iNode table pointer for this group descriptor
		this.initGroupDescFields();
	}

	/**
	 * Method to initialise each field in this group descriptor.
	 */
	public void initGroupDescFields() {
		this.blockBitmapPointer = this.groupDescBuffer.getInt(BLOCK_BITMAP_POINTER_OFFSET + (this.groupNum * GROUP_DESCRIPTOR_SIZE));
		this.iNodeBitmapPointer = this.groupDescBuffer.getInt(INODE_BITMAP_POINTER_OFFSET + (this.groupNum * GROUP_DESCRIPTOR_SIZE));
		this.iNodeTblPointer    = this.groupDescBuffer.getInt(INODE_TBL_POINTER_OFFSET + (this.groupNum * GROUP_DESCRIPTOR_SIZE));
		this.freeBlockCount     = this.groupDescBuffer.getShort(FREE_BLOCK_COUNT_OFFSET + (this.groupNum * GROUP_DESCRIPTOR_SIZE));
		this.freeiNodeCount     = this.groupDescBuffer.getShort(FREE_INODE_COUNT_OFFSET + (this.groupNum * GROUP_DESCRIPTOR_SIZE));
		this.usedDirsCount      = this.groupDescBuffer.getShort(USED_DIRS_COUNT_OFFSET + (this.groupNum * GROUP_DESCRIPTOR_SIZE));
	}

	/**
	 * Retrieves the block bitmap pointer field from this group descriptor.
	 * @return The block bitmap pointer field.
	 */
	public int getBlockBitmapPointer() {
		return this.blockBitmapPointer;
	}

	/**
	 * Retrieves the iNode bitmap pointer field from this group descriptor.
	 * @return The iNode bitmap pointer field.
	 */
	public int getiNodeBitmapPointer() {
		return this.iNodeBitmapPointer;
	}

	/**
	 * Retrieves an iNode table pointer from the group descriptor.
	 * The group descriptor contains descriptors for all block groups.
	 * i.e. 3 block groups = 3 descriptors, hence 3 iNode table pointers.
	 *
	 * @return The iNode table pointer for this block group number.
	 */
	public int getINodeTblPointer() {
		return this.iNodeTblPointer;
	}

	/**
	 * Retrieves the free block count field from this group descriptor.
	 * @return The free block count field.
	 */
	public short getFreeBlockCount() {
		return this.freeBlockCount;
	}

	/**
	 * Retrieves the free iNode count field from this group descriptor.
	 * @return The free iNode count field.
	 */
	public short getFreeiNodeCount() {
		return this.freeiNodeCount;
	}

	/**
	 * Retrieves the used directories count field from this group descriptor.
	 * @return The used directories count field.
	 */
	public short getUsedDirsCount() {
		return this.usedDirsCount;
	}
}