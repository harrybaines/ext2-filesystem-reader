package coursework;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class Ext2Reader extends JFrame implements ActionListener {
        
    private Volume vol;

    public static final int WIDTH = 600;
    public static final int HEIGHT = 700;

    private JButton viewDirBtn;
    private JButton viewFileBtn;
    private JTextField userEntry;
    private String areaString;
    private JTextArea textArea;

    public Ext2Reader(Volume vol) {

        this.vol = vol;

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(5,1,2,2));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JPanel middlePanel = new JPanel(new GridBagLayout());
        middlePanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 10));

        mainPanel.add("Center", topPanel);
        mainPanel.add("South", middlePanel);

        JLabel titleLbl = new JLabel("Ext2 FileSystem Reader", SwingConstants.CENTER);
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
        JScrollPane scrollPane = new JScrollPane(textArea);

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
            this.createNewFile(userEntry.getText(), true);
        }
        else if (e.getSource() == viewFileBtn) {
            this.createNewFile(userEntry.getText(), false);
        }
    }

    public void createNewFile(String filePath, boolean clickedDirectory) {

        if (filePath.equals("") || filePath.charAt(0) != '/')
            JOptionPane.showMessageDialog(null, "Please enter a valid path.", "Entry Error", JOptionPane.ERROR_MESSAGE);
        else {
            Ext2File fileChosen = new Ext2File(vol, filePath);
            if (clickedDirectory) {
                for (String s : fileChosen.getFileInfoList())
                    textArea.append(s);
            }
            else {
                if (fileChosen.isDirectory())
                    JOptionPane.showMessageDialog(null, "Can't view file contents - this is a directory.", "File Content Error", JOptionPane.ERROR_MESSAGE);
                else {
                    byte fileBuf[] = fileChosen.read(0L, fileChosen.getSize()); // length issue
                    System.out.println("LENGTH: " + fileBuf.length);
                    areaString = new String(fileBuf);
                    textArea.append(areaString);
                }
            }
        }
    }
}