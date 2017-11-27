package coursework;

public class Driver {

    // API user will use
    public Driver() {

        // Create new volume instance for 'ext2fs'
        Volume vol = new Volume("ext2fs");

        Thread[] threads = new Thread[4];

        for (Thread t : threads)
            t = new Thread();
        
        // Create new file instances
        Ext2File twoCities = new Ext2File(vol, "/two-cities");
        Ext2File deepDownFile = new Ext2File(vol, "/deep/down/in/the/filesystem/there/lived/a/file");
        Ext2File doubleStart = new Ext2File(vol, "/files/dbl-ind-s");
        Ext2File tripleStart = new Ext2File(vol, "/files/trpl-ind-s");

        // Initialise file as a directory (directories = files) and display directory contents

        //twoCities.printDirectoryInfo();

        new Directory(twoCities).printDirectoryInfo();
        new Directory(deepDownFile).printDirectoryInfo();
        new Directory(doubleStart).printDirectoryInfo();
        new Directory(tripleStart).printDirectoryInfo();

        // NEED TO CHANGE - FILE SIZE
        byte twoCitiesBuf[] = twoCities.readFile(0L, 2048);
        byte deepDownBuf[] = deepDownFile.readFile(0L, 20);
        byte doubleStartBuf[] = doubleStart.readFile(0L, 21);
        byte tripleStartBuf[] = tripleStart.readFile(0L, 21);

        // WORK ON THIS
        System.out.format("%s\n\n", ((twoCitiesBuf.length == 0) ? "--- nothing found ---" : "\nFile Contents for '" + twoCities.getFileName() + "':\n----------\n" + new String(twoCitiesBuf))); 
        System.out.format("%s\n\n", ((deepDownBuf.length == 0) ? "--- nothing found ---" : "\nFile Contents for '" + deepDownFile.getFileName() + "':\n----------\n" + new String(deepDownBuf))); 
        System.out.format("%s\n\n", ((doubleStartBuf.length == 0) ? "--- nothing found ---" : "\nFile Contents for '" + doubleStart.getFileName() + "':\n----------\n" + new String(doubleStartBuf))); 
        System.out.format("%s\n\n", ((tripleStartBuf.length == 0) ? "--- nothing found ---" : "\nFile Contents for '" + tripleStart.getFileName() + "':\n----------\n" + new String(tripleStartBuf))); 
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