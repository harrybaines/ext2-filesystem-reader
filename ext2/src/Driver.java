package ext2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Name: Driver
 *
 * This class allows the user to open several files that are contained within the given volume they provide.
 * In this example, the user opens many files located within the ext2 filesystem.
 * The user can open these files and view their contents if they are regular files.
 * Also, the user can view a directory listing for a file or a directory they choose.
 * Usage: can use: /xxx to find a file from the root, or can use: /root/xxx where xxx is a file.
 *
 * @author Harry Baines
 */
public class Driver {

    /** 
     * Constructor to initialise new files within the volume the user provides.
     */
    public Driver() {

        long startTime = System.currentTimeMillis();

        // Create new volume instance for 'ext2fs' and print the super block info
        Volume vol = new Volume("volumes/ext2fs");

        // Create new helper class for debugging
        Helper h = new Helper();
        h.printSuperblockInfo(vol.getSuperblock());
        h.printFurtherSuperBlockInfo(vol.getSuperblock());


        /* CREATE NEW FILE INSTANCES */
        // 1. Create new file
        // 2. Print directory information
        // 3. Read file contents + display file contents
        Ext2File root = new Ext2File(vol, "/root");
        root.printDirectoryInfo();

        Ext2File twoCities = new Ext2File(vol, "/two-cities");
        twoCities.printDirectoryInfo();
        twoCities.seek(20);
        byte twoCitiesBuf[] = twoCities.read(twoCities.getPosition(), twoCities.getSize());
        twoCities.printFileContents(twoCitiesBuf);

        Ext2File deepDownFile = new Ext2File(vol, "/deep/down/in/the/filesystem/there/lived/a/file");
        deepDownFile.printDirectoryInfo();
        byte deepDownBuf[] = deepDownFile.read(0L, deepDownFile.getSize());
        h.dumpHexBytes(deepDownBuf);
        deepDownFile.printFileContents(deepDownBuf);

        Ext2File dirStart = new Ext2File(vol, "/files/dir-s");
        dirStart.printDirectoryInfo();
        dirStart.printFileContents(dirStart.read(0L, dirStart.getSize()));

        Ext2File indEnd = new Ext2File(vol, "/files/ind-e");
        indEnd.printFileContents(indEnd.read(0L, indEnd.getSize()));

        Ext2File doubleEnd = new Ext2File(vol, "/files/dbl-ind-e");
        byte doubleEndBuf[] = doubleEnd.read(0L, 21);
        doubleEnd.printFileContents(doubleEndBuf);
        h.dumpHexBytes(doubleEndBuf);

        Ext2File tripleStart = new Ext2File(vol, "/files/trpl-ind-s");
        tripleStart.printFileContents(tripleStart.read(0L, 23)); // SIZE ISSUE

        Ext2File tripleEnd = new Ext2File(vol, "/files/trpl-ind-e");
        tripleEnd.printFileContents(tripleEnd.read(0L, 20));

        Ext2File bigDir = new Ext2File(vol, "/big-dir");

        System.out.println("Time to open all files: " + (System.currentTimeMillis() - startTime) + "ms\n");

        // Create new GUI instance
        Ext2Reader e = new Ext2Reader(vol);
    }

    /**
     * Main method to begin the driver program.
     * @param args Unused.
     */
    public static void main(String[] args) {
        new Driver();
    }
}