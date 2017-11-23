package coursework;

public class Driver {

    private static final String pathName = "/two-cities";

    // API user will use
    public Driver() {

        Volume vol = new Volume("ext2fs");
        Ext2File file = new Ext2File(vol, pathName);

        // PARTIALLY WORKING - NEED TO OPEN FILE ONCE FOUND, NOT JUST STOP SEARCHING THROUGH DIRECTORIES
        Directory d = new Directory(file);
        d.printDirectoryInfo();

        // NEED TO CHANGE - FILE SIZE
        byte buf[] = file.readFile(0L, 2048);

        // WORK ON THIS
        System.out.format("%s\n\n", ((buf.length == 0) ? "--- nothing found ---" : "File Contents:\n----------\n" + new String(buf))); 
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