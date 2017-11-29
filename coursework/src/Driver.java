package coursework;

/**
 * Name: Driver
 *
 * This class allows the user to open several files that are contained within the given volume they provide.
 * In this example, the user opens many files located within the ext2 filesystem.
 * The user can open these files and view their contents if they are regular files.
 * Also, the user can view a directory listing for a file or a directory they choose.
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
        
        // Create new file instances
        Ext2File twoCities = new Ext2File(vol, "/two-cities");
        Ext2File deepDownFile = new Ext2File(vol, "/deep/down/in/the/filesystem/there/lived/a/file");
        Ext2File dirStart = new Ext2File(vol, "/files");
        Ext2File dirEnd = new Ext2File(vol, "/files/dir-e");
        Ext2File indStart = new Ext2File(vol, "/files/ind-s");
        Ext2File indEnd = new Ext2File(vol, "/files/ind-e");
        Ext2File doubleStart = new Ext2File(vol, "/files/dbl-ind-s");
        Ext2File doubleEnd = new Ext2File(vol, "/files/dbl-ind-e");
        Ext2File tripleStart = new Ext2File(vol, "/files/trpl-ind-s");
        Ext2File tripleEnd = new Ext2File(vol, "/files/trpl-ind-e");
        Ext2File lostFound = new Ext2File(vol, "/lost+found");
        Ext2File bigDir = new Ext2File(vol, "/big-dir");

        // Prints the directory information relevant to a given file
        twoCities.printDirectoryInfo();
        deepDownFile.printDirectoryInfo();
        dirStart.printDirectoryInfo();
        lostFound.printDirectoryInfo();
        //bigDir.printDirectoryInfo();

        // Reads an array of bytes relevant to chosen file
        byte twoCitiesBuf[] = twoCities.readFile(0L, twoCities.size());
        byte deepDownBuf[] = deepDownFile.readFile(0L, deepDownFile.size());
        Helper.dumpHexBytes(deepDownBuf);

        byte dirStartBuf[] = dirStart.readFile(0L, dirStart.size());
        byte dirEndBuf[] = dirEnd.readFile(0L, dirEnd.size());
        byte indStartBuf[] = indStart.readFile(0L, indStart.size());
        byte indEndBuf[] = indEnd.readFile(0L, indEnd.size());
        byte doubleStartBuf[] = doubleStart.readFile(0L, doubleStart.size());
        byte doubleEndBuf[] = doubleEnd.readFile(0L, doubleEnd.size());
        byte tripleStartBuf[] = tripleStart.readFile(0L, tripleStart.size());
        byte tripleEndBuf[] = tripleEnd.readFile(0L, tripleEnd.size());

        // Read file contents using pre-built print method
        twoCities.printFileContents(twoCitiesBuf);
        deepDownFile.printFileContents(deepDownBuf);
        dirStart.printFileContents(dirStartBuf);
        dirEnd.printFileContents(dirEndBuf);
        indStart.printFileContents(indStartBuf);
        indEnd.printFileContents(indEndBuf);
        doubleStart.printFileContents(doubleStartBuf);
        doubleEnd.printFileContents(doubleEndBuf);
        // tripleStart.printFileContents(tripleStartBuf);
        // tripleEnd.printFileContents(tripleEndBuf);

        System.out.println("Time to open all files: " + (System.currentTimeMillis() - startTime) + "ms");
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