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

	public static final int INODE_TBL_POINTER_OFFSET = 8;		/* Offset, in bytes, to find the iNode table pointer in the group descriptor */
	public static final int GROUP_DESCRIPTOR_SIZE = 32;			/* The total size of each group descriptor in the file system */

	private ByteBuffer groupDescBuffer;							/* Byte buffer to store bytes for each group descriptor instance */
	private int groupNum;										/* The group number for which this group descriptor references */
	
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
	}

	/**
	 * Retrieves an iNode table pointer from the group descriptor.
	 * The group descriptor contains descriptors for all block groups.
	 * i.e. 3 block groups = 3 descriptors, hence 3 iNode table pointers.
	 *
	 * @return The iNode table pointer for this block group number.
	 */
	public int getINodeTblPointer() {
		int pointerPos = INODE_TBL_POINTER_OFFSET + (this.groupNum * GROUP_DESCRIPTOR_SIZE); 
		return (this.groupDescBuffer.getInt(pointerPos));
	}
}