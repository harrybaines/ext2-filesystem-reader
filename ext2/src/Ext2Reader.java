package ext2;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class Ext2Reader extends JFrame implements ActionListener {
        
    private Volume vol;
    private Helper h;

    public static final int WIDTH = 600;
    public static final int HEIGHT = 700;

    private JButton viewDirBtn;
    private JButton viewFileBtn;
    private JTextField userEntry;
    private String areaString;
    private JTextArea textArea;

    private JScrollPane scrollPane;

    private JMenuItem superBlockItem;
    private JMenuItem furtherItem;
    private JMenuItem iNodeItem;

    private JMenuItem quitItem;

    private JCheckBoxMenuItem hexAscciItem;
    private JMenuItem viewRootItem;
    private JMenuItem viewRootAsHexItem;
    private JMenuItem resetItem;

    private boolean viewHexAscii;

    public Ext2Reader(Volume vol) {

        this.vol = vol;
        this.viewHexAscii = false;
        this.h = new Helper();

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(5,1,2,2));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JPanel middlePanel = new JPanel(new GridBagLayout());
        middlePanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 10));

        mainPanel.add("Center", topPanel);
        mainPanel.add("South", middlePanel);

        JLabel titleLbl = new JLabel("Ext2 FileSystem Reader", SwingConstants.CENTER);
        titleLbl.setForeground(Color.BLUE);
        titleLbl.setFont(new Font("Serif", Font.ITALIC, 30));
        topPanel.add(titleLbl);

        // Middle panel - user entry area
        JLabel entryLbl = new JLabel("Enter the pathname for the file below:", SwingConstants.CENTER);
        entryLbl.setFont(new Font("Serif", Font.BOLD, 20));
        topPanel.add(entryLbl);
        
        userEntry = new JTextField(10);
        userEntry.setHorizontalAlignment(SwingConstants.CENTER);
        userEntry.setFont(new Font("Serif", Font.BOLD, 25));
        topPanel.add(userEntry);

        viewDirBtn = new JButton("View Directory");
        viewDirBtn.setFont(new Font("Arial Narrow", Font.BOLD, 16));
        viewDirBtn.addActionListener(this);
        topPanel.add(viewDirBtn);

        viewFileBtn = new JButton("View File Contents");
        viewFileBtn.setFont(new Font("Arial Narrow", Font.BOLD, 16));
        viewFileBtn.addActionListener(this);
        topPanel.add(viewFileBtn);

        // Create the model and add elements
        textArea = new JTextArea(areaString);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scrollPane = new JScrollPane(textArea);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.ipady = 250;
        c.ipadx = 400;
        c.insets = new Insets(5,5,5,5);
        middlePanel.add(scrollPane, c);

        // Bottom panel
        JLabel bottomLbl = new JLabel("SCC.211 Harry Baines 2017", SwingConstants.CENTER);
        bottomLbl.setFont(new Font("Serif", Font.ITALIC, 14));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.ipady = 10;
        c.insets = new Insets(10,10,10,10);
        middlePanel.add(bottomLbl, c);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        // Submenu
        JMenu submenu = new JMenu("Ext2 Info");
        superBlockItem = new JMenuItem(new Action("SuperBlock"));
        furtherItem = new JMenuItem(new Action("Further Information"));
        iNodeItem = new JMenuItem(new Action("iNode Table Pointers"));
        submenu.add(superBlockItem);
        submenu.add(furtherItem);
        submenu.add(iNodeItem);
        menu.add(submenu);

        menu.addSeparator();
        quitItem = new JMenuItem(new Action("Quit"));
        menu.add(quitItem);

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
        this.add(mainPanel);
        this.setTitle("Ext2Reader");
        this.setSize(WIDTH, HEIGHT);
        this.setLocation(WIDTH/2, HEIGHT/8);
        this.setResizable(false);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        textArea.selectAll();
        textArea.replaceSelection("");
        if (e.getSource() == viewDirBtn) {
            this.createNewFile(userEntry.getText(), true, viewHexAscii);
        }
        else if (e.getSource() == viewFileBtn) {
            if (userEntry.getText().equals("/") && viewHexAscii) 
                this.createNewFile(userEntry.getText(), false, true);
            else
                this.createNewFile(userEntry.getText(), false, viewHexAscii);
        }
        textArea.setCaretPosition(0);
    }

    public void createNewFile(String filePath, boolean clickedDirectory, boolean viewHexAscii) {

        if (filePath.equals("") || filePath.charAt(0) != '/')
            JOptionPane.showMessageDialog(null, "Please enter a valid path.", "Entry Error", JOptionPane.ERROR_MESSAGE);
        else {
            Ext2File fileChosen = new Ext2File(vol, filePath);
            if (clickedDirectory) {
                for (String s : fileChosen.getFileInfoList())
                    textArea.append(s);
            }
            else {
                if (fileChosen.isDirectory() && !viewHexAscii)
                    JOptionPane.showMessageDialog(null, "Can't view file contents, this is a directory. Try viewing as hex and ASCII.", "File Content Error", JOptionPane.ERROR_MESSAGE);
                else {
                    byte fileBuf[] = fileChosen.read(0L, fileChosen.getSize());

                    // View file contents as hex and ASCII
                    if (viewHexAscii) {
                        if (fileChosen.isDirectory() || filePath.equals("/"))
                            areaString = h.getHexBytesString(fileChosen.getDirDataBuffer().array());
                        else
                            areaString = h.getHexBytesString(fileBuf);
                        textArea.append(areaString);
                        textArea.setCaretPosition(0);
                    }

                    // View regular file contents
                    else {
                        areaString = new String(fileBuf);
                        textArea.append(areaString);
                    }   
                }
            }
        }
    }

    private class Action extends AbstractAction {

        public Action(String name) {
            super(name);
        }

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

            // View iNode table pointers
            else if (e.getSource() == iNodeItem) {
                String pointersString = "";
                int[] pointers = Ext2Reader.this.vol.getSuperblock().getiNodeTablePointers();

                for (int i = 0; i < pointers.length; i++)
                    pointersString += "iNode Table Pointer " + i + ": Block " + Integer.toString(pointers[i]) + "\n";

                String iNodeString = "iNode Table Pointers:\n----------\n"+pointersString+"----------";
                UIManager.put("OptionPane.messageFont", new Font("Monospaced", Font.PLAIN, 14));
                JOptionPane.showMessageDialog(null, iNodeString, "iNode Table Pointers", JOptionPane.PLAIN_MESSAGE);
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