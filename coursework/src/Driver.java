package coursework;

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
        Volume vol = new Volume("ext2fs");
        Helper.printSuperblockInfo(vol.getSuperblock());
        
        /* CREATE NEW FILE INSTANCES */
        // 1. Create new file
        // 2. Print directory information
        // 3. Read file contents
        // 4. Display file contents
        Ext2File twoCities = new Ext2File(vol, "/two-cities");
        twoCities.printDirectoryInfo();
        byte twoCitiesBuf[] = twoCities.read(0L, twoCities.getSize());
        twoCities.printFileContents(twoCitiesBuf);


        twoCities.seek(20);
        twoCities.printFileContents(twoCitiesBuf);


        Ext2File deepDownFile = new Ext2File(vol, "/deep/down/in/the/filesystem/there/lived/a/file");
        deepDownFile.printDirectoryInfo();
        byte deepDownBuf[] = deepDownFile.read(0L, deepDownFile.getSize());
        Helper.dumpHexBytes(deepDownBuf);
        deepDownFile.printFileContents(deepDownBuf);


        Ext2File dirStart = new Ext2File(vol, "/files/dir-s");
        dirStart.printDirectoryInfo();
        byte dirStartBuf[] = dirStart.read(0L, dirStart.getSize());
        dirStart.printFileContents(dirStartBuf);


        Ext2File dirEnd = new Ext2File(vol, "/files/dir-e");
        byte dirEndBuf[] = dirEnd.read(0L, dirEnd.getSize());
        dirEnd.printFileContents(dirEndBuf);


        Ext2File indStart = new Ext2File(vol, "/files/ind-s");
        byte indStartBuf[] = indStart.read(0L, indStart.getSize());
        indStart.printFileContents(indStartBuf);


        Ext2File indEnd = new Ext2File(vol, "/files/ind-e");
        byte indEndBuf[] = indEnd.read(0L, indEnd.getSize());
        indEnd.printFileContents(indEndBuf);


        Ext2File doubleStart = new Ext2File(vol, "/files/dbl-ind-s");
        byte doubleStartBuf[] = doubleStart.read(0L, 1000);
        doubleStart.printFileContents(doubleStartBuf);


        Ext2File doubleEnd = new Ext2File(vol, "/files/dbl-ind-e");
        byte doubleEndBuf[] = doubleEnd.read(0L, 1000);
        doubleEnd.printFileContents(doubleEndBuf);


        Ext2File tripleStart = new Ext2File(vol, "/files/trpl-ind-s");
        byte tripleStartBuf[] = tripleStart.read(0L, 1000);
        tripleStart.printFileContents(tripleStartBuf);


        Ext2File tripleEnd = new Ext2File(vol, "/files/trpl-ind-e");
        byte tripleEndBuf[] = tripleEnd.read(0L, 1000);
        tripleEnd.printFileContents(tripleEndBuf);


        Ext2File lostFound = new Ext2File(vol, "/lost+found");
        lostFound.printDirectoryInfo();
        byte lostFoundBuf[] = lostFound.read(0L, 1000);
        lostFound.printFileContents(lostFoundBuf);

        Ext2File bigDir = new Ext2File(vol, "/big-dir");
        //bigDir.printDirectoryInfo();


        System.out.println("\033[1mTime to open all files: " + (System.currentTimeMillis() - startTime) + "ms\033[0m\n");
    }

    /**
     * Main method to begin the driver program.
     * 
     * @param args Unused.
     */
    public static void main(String[] args) {
        new Driver();
    }
}