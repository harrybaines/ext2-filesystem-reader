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

    private int fileMode;

    public INode(int iNodeNumber, int iNodeTblPointer, int groupNum, SuperBlock superBlock) {
        super(superBlock.getVolume());
        this.iNodeNumber = iNodeNumber;
        this.iNodeTblPointer = iNodeTblPointer;
        this.groupNum = groupNum;
        this.superBlock = superBlock;
    }

    /** 
     * Method to obtain a particular iNode's fields.
     * @return Array of bytes containing the iNode fields.
     */
    public byte[] getINodeInfoBytes() {

        int iNodeOffset = (iNodeTblPointer * superBlock.getBlockSize()) + (((iNodeNumber-1) - (groupNum * superBlock.getiNodesPerGroup())) * superBlock.getiNodeSize());

        byte[] iNodeBytes = this.read(iNodeOffset, superBlock.getiNodeSize());

        // Wrap in buffer for later reading
        iNodeBuffer = ByteBuffer.wrap(iNodeBytes);
        iNodeBuffer.order(ByteOrder.LITTLE_ENDIAN);

        return iNodeBytes;
    }

    /**
     * Retrieves an array of bytes referenced by the direct pointers in this iNode.
     * @return Array of bytes for the data blocks being pointed to.
     */
    public byte[] getDataBlocksFromDirectPointers() {

        int[] directPointers = this.getDirectPointers();

        List<Byte> byteList = new ArrayList<Byte>();

        for (int i = 0; i < directPointers.length; i++) {
            if (directPointers[i] != 0) {
                byte[] dataBlocksArray = this.read(directPointers[i] * superBlock.getBlockSize(), superBlock.getBlockSize());
                for (int curBlock = 0; curBlock < dataBlocksArray.length; curBlock++) {
                    byteList.add(dataBlocksArray[curBlock]);
                }
            }
        }

        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++)
            byteArray[i] = byteList.get(i);

        return byteArray;
    }

    /**
     * Outputs all fields relevant to a given iNode.
     */
    public void printINodeInfo() {
        System.out.println("----------");
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
        System.out.println("Pointer to first block:                 "   + getDirectPointers()[0]);
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
        return(deletedTime);
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
    public int[] getDirectPointers() {
        int[] directPointers = new int[12];
        for (int i = 0; i < directPointers.length; i++)
            directPointers[i] = this.getIntFromBytes(40 + (4*i), iNodeBuffer);
        return (directPointers);
    }

    /**
     * Retrieves the indirect integer data block pointer.
     * @return The indirect data block pointer.
     */
    public int getIndirectPointer() {
        return (this.getIntFromBytes(4, iNodeBuffer));
    }

    /**
     * Retrieves the double-indirect integer data block pointer.
     * @return The double-indirect data block pointer.
     */
    public int getDoubleIndirectPointer() {
        return (this.getIntFromBytes(4, iNodeBuffer));
    }

    /**
     * Retrieves the triple-indirect integer data block pointer.
     * @return The triple-indirect data block pointer.
     */
    public int getTripleIndirectPointer() {
        return (this.getIntFromBytes(4, iNodeBuffer));
    }

    /**
     * Retrieves the upper 32 bits of the file size, in bytes.
     * @return Upper 32 bit size of file in bytes.
     */
    public int getUpperFileSize() {
        return (this.getIntFromBytes(140, iNodeBuffer));
    }
}