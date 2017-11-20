package coursework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GroupDescriptor extends DataBlock {

	public static final int INODE_TBL_POINTER_OFFSET = 8;
	public static final int GROUP_DESCRIPTOR_SIZE = 32;

	private ByteBuffer groupDescBuffer;
	private int groupNum;
	private SuperBlock superBlock;
	
	public GroupDescriptor(ByteBuffer groupDescBuffer, int groupNum, SuperBlock superBlock) {
		super(superBlock.getVolume());
		this.groupDescBuffer = groupDescBuffer;
		this.groupNum = groupNum;
		this.superBlock = superBlock;
	}

	/**
	 * Retrieves an iNode table pointer from the group descriptor.
	 * The group descriptor contains descriptors for all block groups.
	 * i.e. 3 block groups = 3 descriptors, hence 3 iNode table pointers.
	 *
	 * @return The iNode table pointer for the current block group number.
	 */
	public int getINodeTblPointer() {

		int pointerPos = INODE_TBL_POINTER_OFFSET + (groupNum * GROUP_DESCRIPTOR_SIZE); 

		int iNodeTblPointer = this.getIntFromBytes(pointerPos, groupDescBuffer);

		return iNodeTblPointer;

	}
}