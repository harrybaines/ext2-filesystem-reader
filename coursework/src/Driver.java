package coursework;

public class Driver {

    // API user will use
    public Driver() {

        long startTime = System.currentTimeMillis();

        // Create new volume instance for 'ext2fs'
        Volume vol = new Volume("ext2fs");
        
        // Create new file instances
        Ext2File twoCities = new Ext2File(vol, "/two-cities");
        Ext2File deepDownFile = new Ext2File(vol, "/deep/down/in/the/filesystem/there/lived/a/file");
        Ext2File dirStart = new Ext2File(vol, "/files/dir-s");
        Ext2File dirEnd = new Ext2File(vol, "/files/dir-e");
        Ext2File indStart = new Ext2File(vol, "/files/ind-s");
        Ext2File indEnd = new Ext2File(vol, "/files/ind-e");
        Ext2File doubleStart = new Ext2File(vol, "/files/dbl-ind-s");
        Ext2File doubleEnd = new Ext2File(vol, "/files/dbl-ind-e");
        Ext2File tripleStart = new Ext2File(vol, "/files/trpl-ind-s");
        Ext2File tripleEnd = new Ext2File(vol, "/files/trpl-ind-e");
        Ext2File lostFound = new Ext2File(vol, "/lost+found");
        Ext2File bigDir = new Ext2File(vol, "/big-dir");

        // Initialise file as a directory (directories = files) and display directory contents

        // WORK ON THIS
        //twoCities.printDirectoryInfo();

        new Directory(twoCities).printDirectoryInfo();
        new Directory(deepDownFile).printDirectoryInfo();
        new Directory(dirStart).printDirectoryInfo();
        new Directory(lostFound).printDirectoryInfo();
        new Directory(bigDir).printDirectoryInfo();

        // FIX FILE SIZES
        byte twoCitiesBuf[] = twoCities.readFile(0L, 2048);
        byte deepDownBuf[] = deepDownFile.readFile(0L, 20);
        byte dirStartBuf[] = dirStart.readFile(0L, 21);
        byte dirEndBuf[] = dirEnd.readFile(0L, 21);
        byte indStartBuf[] = indStart.readFile(0L, 21);
        byte indEndBuf[] = indEnd.readFile(0L, 21);
        byte doubleStartBuf[] = doubleStart.readFile(0L, 21);
        byte doubleEndBuf[] = doubleEnd.readFile(0L, 21);
        byte tripleStartBuf[] = tripleStart.readFile(0L, 21);
        byte tripleEndBuf[] = tripleEnd.readFile(0L, 21);

        // Read file contents using pre-built print method
        twoCities.printFileContents(twoCitiesBuf, twoCities.getFileName());
        deepDownFile.printFileContents(deepDownBuf, deepDownFile.getFileName());
        dirStart.printFileContents(dirStartBuf, dirStart.getFileName());
        dirEnd.printFileContents(dirEndBuf, dirEnd.getFileName());
        indStart.printFileContents(indStartBuf, indStart.getFileName());
        indEnd.printFileContents(indEndBuf, indEnd.getFileName());
        doubleStart.printFileContents(doubleStartBuf, doubleStart.getFileName());
        doubleEnd.printFileContents(doubleEndBuf, doubleEnd.getFileName());
        tripleStart.printFileContents(tripleStartBuf, tripleStart.getFileName());
        tripleEnd.printFileContents(tripleEndBuf, tripleEnd.getFileName());

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