package ext2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Title: Ext2Reader
 *
 * This class provides an implmentation of a custom GUI to read information from a given volume.
 * The user can view various file system information and can dynamically view different directories and file contents.
 *
 * @author Harry Baines
 * @see JFrame
 */
public class Ext2Reader extends JFrame implements ActionListener {
        
    public static final int WIDTH = 600;        /* Width of the window */
    public static final int HEIGHT = 700;       /* Height of the window */

    private Volume vol;                         /* Reference to the volume which this reader is based */
    private Helper h;                           /* Helper instance for dumping hex bytes */
    private GroupDescriptor[] groupDescriptors; /* Array of group descriptors to view info for */

    private JPanel mainPanel;                   /* Main content panel for window */
    private JPanel topPanel;                    /* Top section of main panel */
    private JPanel middlePanel;                 /* Middle section of main panel */

    private JLabel titleLbl;                    /* Title of window label */
    private JLabel entryLbl;                    /* Label for user entry */
    private JTextField userEntry;               /* Entry box for user */
    private JButton viewDirBtn;                 /* Button to view a directory listing */      
    private JButton viewFileBtn;                /* Button to view file contents */
    private JTextArea textArea;                 /* Output to view file contents and directory listings */
    private String areaString;                  /* String containing contents to output in text area */
    private JScrollPane scrollPane;             /* User can scroll down output if necessary */

    private GridBagConstraints c;               /* Custom constraints for component placement */
    
    private JLabel bottomLbl;                   /* Name label at bottom of window */
    private JMenuBar menuBar;                   /* Custom drop down menu bar */
    private JMenu menu;                         /* Menu on top bar */
    private JMenu submenu;                      /* Sub-menu on top bar */
    private JMenuItem superBlockItem;           /* User can view super block information */
    private JMenuItem furtherItem;              /* User can view further file system information */
    private JMenuItem groupDescItem;            /* User can view all group descriptor fields */
    private JMenuItem quitItem;                 /* User can quit the program */

    private JCheckBoxMenuItem hexAscciItem;     /* User can change output to view hex and ASCII */
    private JMenuItem viewRootItem;             /* User can view the root directory */
    private JMenuItem viewRootAsHexItem;        /* User can view the root directory in hex and ASCII format */
    private JMenuItem resetItem;                /* User can reset input and output fields */
    private boolean viewHexAscii;               /* Monitors if user is currently viewing in hex and ASCII format */

    /**
     * Constructor to initialise all the components on the UI and present the window to the user.
     * @param vol The reference to the volume upon which this frame will read data.
     */
    public Ext2Reader(Volume vol) {

        // Initialise instance variables
        this.vol = vol;
        this.viewHexAscii = false;
        this.h = new Helper();
        this.groupDescriptors = Ext2Reader.this.vol.getGroupDescriptors();

        // Initialise panels
        mainPanel = new JPanel(new BorderLayout());
        topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        middlePanel = new JPanel(new GridBagLayout());
        middlePanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 10));

        mainPanel.add("Center", topPanel);
        mainPanel.add("South", middlePanel);

        // Initialise components
        c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 0.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 20;
        c.ipadx = 20;
        c.insets = new Insets(5,5,5,5);
        titleLbl = new JLabel("Ext2 FileSystem Reader", SwingConstants.CENTER);
        titleLbl.setForeground(Color.BLUE);
        titleLbl.setFont(new Font("Tahoma", Font.ITALIC, 30));
        topPanel.add(titleLbl, c);

        // Middle panel - user entry area
        c.weightx = 1;
        c.weighty = 0.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        c.ipady = 20;
        c.ipadx = 50;
        c.insets = new Insets(0,0,5,0);
        entryLbl = new JLabel("Enter the pathname for the file below:", SwingConstants.CENTER);
        entryLbl.setFont(new Font("Tahoma", Font.BOLD, 18));
        topPanel.add(entryLbl, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 2;
        c.ipady = 40;
        c.ipadx = 50;
        c.insets = new Insets(0,5,10,5);
        userEntry = new JTextField(10);
        userEntry.setHorizontalAlignment(SwingConstants.CENTER);
        userEntry.setFont(new Font("Helvetica", Font.PLAIN, 24));
        topPanel.add(userEntry, c);

        // Output area
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 3;
        c.ipady = 20;
        c.ipadx = 20;
        c.insets = new Insets(5,5,25,5);
        viewDirBtn = new JButton("View Directory");
        viewDirBtn.setFont(new Font("Arial Narrow", Font.BOLD, 16));
        viewDirBtn.addActionListener(this);
        topPanel.add(viewDirBtn, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 3;
        c.ipady = 20;
        c.ipadx = 20;
        c.insets = new Insets(5,5,25,5);
        viewFileBtn = new JButton("View File Contents");
        viewFileBtn.setFont(new Font("Arial Narrow", Font.BOLD, 16));
        viewFileBtn.addActionListener(this);
        topPanel.add(viewFileBtn, c);

        // Create the model and add elements
        textArea = new JTextArea(areaString);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scrollPane = new JScrollPane(textArea);

        // Custom component placement
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 6;
        c.ipady = 300;
        c.ipadx = 400;
        c.insets = new Insets(5,5,10,5);
        middlePanel.add(scrollPane, c);

        // Bottom panel
        bottomLbl = new JLabel("SCC.211 Harry Baines 2017", SwingConstants.CENTER);
        bottomLbl.setFont(new Font("Serif", Font.ITALIC, 14));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 7;
        c.ipady = 10;
        c.insets = new Insets(0,0,10,0);
        middlePanel.add(bottomLbl, c);

        // Menu bar - file menu
        menuBar = new JMenuBar();
        menu = new JMenu("File");
        menuBar.add(menu);

        // Submenu in file menu
        submenu = new JMenu("Ext2 Info");
        superBlockItem = new JMenuItem(new Action("SuperBlock"));
        groupDescItem = new JMenuItem(new Action("Group Descriptor Info"));
        furtherItem = new JMenuItem(new Action("Further Information"));
        submenu.add(superBlockItem);
        submenu.add(groupDescItem);
        submenu.add(furtherItem);
        menu.add(submenu);
        menu.addSeparator();
        quitItem = new JMenuItem(new Action("Quit"));
        menu.add(quitItem);

        // View menu
        menu = new JMenu("View");
        hexAscciItem = new JCheckBoxMenuItem(new Action("View File Contents As Hex/ASCII"));
        menu.add(hexAscciItem);
        menu.addSeparator();
        viewRootItem = new JMenuItem(new Action("View Root Directory"));
        menu.add(viewRootItem);
        menuBar.add(menu);
        menu.addSeparator();
        viewRootAsHexItem = new JMenuItem(new Action("View Root As Hex/ASCII"));
        menu.add(viewRootAsHexItem);
        menuBar.add(menu);

        // Build second menu in the menu bar
        menu = new JMenu("Tools");
        resetItem = new JMenuItem(new Action("Reset Entry"));
        menu.add(resetItem);
        menuBar.add(menu);
        this.setJMenuBar(menuBar);

        // Final window details
        topPanel.setBackground(new Color(221, 255, 204));
        middlePanel.setBackground(new Color(221, 255, 204));
        this.add(mainPanel);
        this.setTitle("Ext2Reader");
        this.setSize(WIDTH, HEIGHT);
        this.setLocation(WIDTH/2, HEIGHT/8);
        this.setResizable(false);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Display root upon load
        viewRootItem.doClick();
        this.requestFocusInWindow();
    }

    /**
     * Called when a UI component is clicked.
     * @param e The action event recorded.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        // Reset entry fields
        textArea.selectAll();
        textArea.replaceSelection("");

        // View a directory based on user entry
        if (e.getSource() == viewDirBtn) {
            this.createNewFile(userEntry.getText(), true, viewHexAscii);
        }

        // View file contents
        else if (e.getSource() == viewFileBtn) {
            if (userEntry.getText().equals("/") && viewHexAscii) 
                this.createNewFile(userEntry.getText(), false, true);
            else
                this.createNewFile(userEntry.getText(), false, viewHexAscii);
        }
        textArea.setCaretPosition(0);
        this.requestFocusInWindow();
    }

    /**
     * Method to append the file information specified in the user entry field, to the output.
     * The user can choose to view a directory or the contents of the file they specified.
     * Also, the user can view the output in a regular format or in hex and ASCII format.
     *
     * @param filePath The path to the file the user entered.
     * @param clickedDirectory Monitors whether the user wants to view directory contents.
     * @param viewHexAscii Specifies output type.
     */
    public void createNewFile(String filePath, boolean clickedDirectory, boolean viewHexAscii) {

        if (filePath.equals("") || filePath.charAt(0) != '/')
            JOptionPane.showMessageDialog(null, "Please enter a valid path (ensure your path begins with a '/').", "Entry Error", JOptionPane.ERROR_MESSAGE);
        else {

            // Create new file instance if file path is valid
            Ext2File fileChosen = new Ext2File(vol, filePath);
            if (clickedDirectory) {
                // Append directory listing to output
                for (String s : fileChosen.getFileInfoList())
                    textArea.append(s);
            }
            else {
                if (fileChosen.isDirectory() && !viewHexAscii)
                    JOptionPane.showMessageDialog(null, "Can't view file contents, this is a directory. Try viewing as hex and ASCII.", "File Content Error", JOptionPane.ERROR_MESSAGE);
                else {

                    // Read file contents
                    byte fileBuf[] = fileChosen.read(0L, fileChosen.getSize());

                    // View file contents as hex and ASCII if specified
                    if (viewHexAscii) {
                        if (fileChosen.isDirectory() || filePath.equals("/")) {
                            areaString = h.getHexBytesString(fileChosen.getDirDataBuffer().array());
                        }
                        else
                            areaString = h.getHexBytesString(fileBuf);
                        textArea.append(areaString);
                        textArea.setCaretPosition(0);
                    }

                    // View regular file contents
                    else {

                        if (fileChosen.getiNodeForFileToOpen() != null) {
                            String stringToPrint = "";
                            for (int i = 0; i < fileBuf.length; i++)
                                if (fileBuf[i] != 0)
                                    stringToPrint += (char) fileBuf[i];

                            textArea.append(new String(stringToPrint));
                        }
                        else
                            JOptionPane.showMessageDialog(null, "Couldn't view file contents. Try viewing as hex and ASCII.", "File Content Error", JOptionPane.ERROR_MESSAGE);
                    }   
                }
            }
        }
    }

    /**
     * Title: Action
     *
     * This inner class provides a simple implementation for detecting UI clicks in the menu bar.
     *
     * @author Harry Baines
     * @see AbstractAction
     */
    private class Action extends AbstractAction {

        /**
         * Constructor to initialise an action with a name.
         * @param name The name of tha action.
         */
        public Action(String name) {
            super(name);
        }

        /**
         * Called when a UI component is clicked under this action.
         * @param e The action event recorded.
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            // View super block information
            if (e.getSource() == superBlockItem) {
                String superBlockString = "SuperBlock Information:\n----------\n"+Ext2Reader.this.vol.getSuperblock().getSuperBlockInfo()+"----------";
                UIManager.put("OptionPane.messageFont", new Font("Monospaced", Font.PLAIN, 14));
                JOptionPane.showMessageDialog(null, superBlockString, "SuperBlock Info", JOptionPane.PLAIN_MESSAGE);
            }

            // View further file system information
            else if (e.getSource() == furtherItem) {
                String furtherString = "Further File System Information:\n----------\n"+Ext2Reader.this.vol.getSuperblock().getFurtherSuperBlockInfo()+"----------";
                UIManager.put("OptionPane.messageFont", new Font("Monospaced", Font.PLAIN, 14));
                JOptionPane.showMessageDialog(null, furtherString, "Further Info", JOptionPane.PLAIN_MESSAGE);
            }

            // View group descriptor info
            else if (e.getSource() == groupDescItem) {

                String groupDescString = "";

                // Obtain each field and add to output string
                for (int i = 0; i < groupDescriptors.length; i++) {
                    groupDescString += "Group Descriptor " + i + ":\n----------\n";
                    groupDescString += "Block Bitmap Pointer: " + Integer.toString(groupDescriptors[i].getBlockBitmapPointer()) + "\n";
                    groupDescString += "iNode Bitmap Pointer: " + Integer.toString(groupDescriptors[i].getiNodeBitmapPointer()) + "\n";
                    groupDescString += "iNode Table Pointer: " + Integer.toString(groupDescriptors[i].getINodeTblPointer()) + "\n";
                    groupDescString += "Free Block Count: " + Short.toString(groupDescriptors[i].getFreeBlockCount()) + "\n";
                    groupDescString += "Free iNode Count: " + Short.toString(groupDescriptors[i].getFreeiNodeCount()) + "\n";
                    groupDescString += "Used Dirs Count: " + Short.toString(groupDescriptors[i].getUsedDirsCount()) + "\n----------\n";
                }

                UIManager.put("OptionPane.messageFont", new Font("Monospaced", Font.PLAIN, 14));
                JOptionPane.showMessageDialog(null, groupDescString, "Group Descriptor Info", JOptionPane.PLAIN_MESSAGE);
            }

            // View contents as hex and ASCII
            else if (e.getSource() == hexAscciItem) {
                if (!viewHexAscii) 
                    viewHexAscii = true;
                else
                    viewHexAscii = false;
            }

            // View root directory
            else if (e.getSource() == viewRootItem) {
                userEntry.setText("/");
                textArea.setText("");
                Ext2Reader.this.createNewFile("/", true, false);
            }

            // View root directory as hex/ASCII
            else if (e.getSource() == viewRootAsHexItem) {
                userEntry.setText("/");
                textArea.setText("");
                Ext2Reader.this.createNewFile("/", false, true);
            }

            // Reset entry box
            else if (e.getSource() == resetItem) {
                userEntry.setText("");
                textArea.setText("");
            }

            // Quit program
            else if (e.getSource() == quitItem) {
                System.exit(0);
            }
        }
    }
}