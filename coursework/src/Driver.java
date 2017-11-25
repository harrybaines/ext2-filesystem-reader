package coursework;

public class Driver {

    private static final String pathName = "/files/trpl-ind-e";

    // API user will use
    public Driver() {

        Volume vol = new Volume("ext2fs");
        Ext2File file = new Ext2File(vol, pathName);


        byte[] bytes = new byte[7];
        byte[] allBytes = vol.getFileInBytes();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = allBytes[i+1024];
        }
        System.out.println("LOLOL");
        Helper.dumpHexBytes(bytes);


        Directory d = new Directory(file);
        d.printDirectoryInfo();

        // NEED TO CHANGE - FILE SIZE
        byte buf[] = file.readFile(0L, 100000);

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