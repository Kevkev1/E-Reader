package test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * A class to create a eReader window
 * @author Kevin Hetterscheid
 * @version 1.0
 */
public class GUI extends JFrame {
	private static final long serialVersionUID = 5860367452991049874L;
	
	private Container c;
	private JTextPane textPane;
	private ReadEpub rEpub = null;
	
	private JButton nextPage;
	private JButton previousPage;
	private JButton feedBack;
	private JLabel volumeLabel;
	private JSlider volumeSlider;
	
	private JMenuItem menuGoTo;
	private JMenuItem menuSavePage;
	private JMenuItem menuLoadPage;
	
	private StyledDocument styledDocument;
	
	private String textFont = "Arial";
	private int textSize = 12;
	private boolean isTextBold;
	private boolean isTextItalic;
	private boolean isTextUnderlined;
	
	private static final String RECENTS_FILE_PATH = "res/recent.sav";
	private static final String TEXT_CONFIG_FILE_PATH = "res/text.cfg";
	
	/**
	 * The constructor of this class.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c = getContentPane();
		c.setLayout(new BorderLayout());
		init();
		addMenuBar();
		setSize(600,900);
		setVisible(true);
	}
	
	/**
	 * Adds a Menu Bar to the <code>JFrame</code>.
	 */
	private void addMenuBar() {
		JMenuBar menubar = new JMenuBar();
		
		// Popup Menu
		JPopupMenu popup = new JPopupMenu();
		popup.setLayout(new GridLayout(4,1));
		
		JLabel fontLabel = new JLabel("Change font:");
		JComboBox<String> fontChooser = new JComboBox<String>();
		String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for(int i=0; i<fonts.length; i++) {
			fontChooser.addItem(fonts[i]);
		}
		fontChooser.setSelectedItem(textFont);
		
		JLabel fontSize = new JLabel("Change font size:");
		JSlider sizeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, textSize);
		sizeSlider.setMinorTickSpacing(2);
		sizeSlider.setMajorTickSpacing(20);
		sizeSlider.setPaintTicks(true);
		sizeSlider.setPaintLabels(true);
		sizeSlider.setToolTipText("Font size: " + sizeSlider.getValue());
		
		JLabel fontLayout = new JLabel("Change font layout:");
		JCheckBox layoutCheckBold = new JCheckBox("Bold",isTextBold);
		JCheckBox layoutCheckItalic = new JCheckBox("Italic",isTextItalic);
		JCheckBox layoutCheckUnderlined = new JCheckBox("Underlined",isTextUnderlined);
		
		JPanel changeFont = new JPanel();
		changeFont.add(fontLabel);
		changeFont.add(fontChooser);
		
		JPanel changeSize = new JPanel();
		changeSize.add(fontSize);
		changeSize.add(sizeSlider);
		
		JPanel changeLayout = new JPanel();
		changeLayout.add(fontLayout);
		changeLayout.add(layoutCheckBold);
		changeLayout.add(layoutCheckItalic);
		changeLayout.add(layoutCheckUnderlined);
		
		JButton applyTextChanges = new JButton("Apply");
		applyTextChanges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textFont = fontChooser.getItemAt(fontChooser.getSelectedIndex());
				textSize = sizeSlider.getValue();
				isTextBold = layoutCheckBold.isSelected();
				isTextItalic = layoutCheckItalic.isSelected();
				isTextUnderlined = layoutCheckUnderlined.isSelected();
				UpdateText();
				popup.setVisible(false);
			}
		});
		
		popup.add(changeFont);
		popup.add(changeSize);
		popup.add(changeLayout);
		popup.add(applyTextChanges);
		
		//Settings Menu (Menu bar)
		JMenu set = new JMenu("Settings");
		
		JMenuItem menuTextSet = new JMenuItem("Text Settings");
		menuTextSet.setToolTipText("Change text settings");
		menuTextSet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popup.show(null, 10, 10);
			}
		});
		
		set.add(menuTextSet);

		//Navigation Menu (Menu bar)
		JMenu nav = new JMenu("Navigation");
        
        menuGoTo = new JMenuItem("Go to...");
        menuGoTo.setToolTipText("Go to a specific page");
        menuGoTo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String tmp = JOptionPane.showInputDialog(null, "Go to which page?", null);
				@SuppressWarnings("unused")
				int page = 0;
				if(tmp != null) page = Integer.parseInt(tmp);
				//textPane.setText(rEpub.goTo(page));
			}
        });
        
        menuSavePage = new JMenuItem("Save Page");
        menuSavePage.setToolTipText("Save the current page");
        menuSavePage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rEpub.savePage();
			}
        });
        
        menuLoadPage = new JMenuItem("Load Page");
        menuLoadPage.setToolTipText("Load the current page");
        menuLoadPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rEpub.loadPage();
				textPane.setText(rEpub.nextParagraph());
			}
        });
        
        menuGoTo.setEnabled(false);
        menuSavePage.setEnabled(false);
        menuLoadPage.setEnabled(false);
        
        menuSavePage.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
        menuLoadPage.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.Event.CTRL_MASK));
        
        nav.add(menuGoTo);
        nav.add(menuSavePage);
        nav.add(menuLoadPage);
		
        //File Menu (Menu bar)
        JMenu file = new JMenu("File");

        ImageIcon exit = null;
        ImageIcon open = null;
        ImageIcon faq = null;
        
        try {
			exit = new ImageIcon(ImageIO.read(new File("res/exit.png")));
			open = new ImageIcon(ImageIO.read(new File("res/open-file.png")));
			faq = new ImageIcon(ImageIO.read(new File("res/question.png")));
		} catch (IOException e1) { }
        
        JMenuItem menuExit = new JMenuItem("Exit",exit);
        menuExit.setToolTipText("Exit the program");
        menuExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        
        JMenuItem menuOpen = new JMenuItem("Open...",open); 
        menuOpen.setToolTipText("Open a file");
        menuOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter("ePub Files", "epub");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(null);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	openBook(chooser.getSelectedFile().getAbsolutePath());
			    }
			}
        });
        
        JMenu menuOpenRecent = new JMenu("Open Recent");
        
        HashMap<String, String> recents = getRecents();
        JMenuItem[] recentMenuItems = new JMenuItem[recents.size()];
        int count = 0;
        for(String title : recents.keySet()) {
        	recentMenuItems[count] = new JMenuItem(title);
        	recentMenuItems[count].setToolTipText("Open " + title);
        	recentMenuItems[count].addActionListener(new ActionListener() {
    			@Override
    			public void actionPerformed(ActionEvent e) {
    				openBook(recents.get(title));
    			}
            });
        	menuOpenRecent.add(recentMenuItems[count]);
        	count++;
        }
        
        menuOpen.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
        menuExit.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.Event.CTRL_MASK));
        
        file.add(menuOpen);
        file.add(menuOpenRecent);
        file.addSeparator();
        file.add(menuExit);
        
        //Help Menu (Menu bar)
        JMenu help = new JMenu("Help");
        
        JMenuItem menuFAQ = new JMenuItem("FAQ",faq);
        menuFAQ.setToolTipText("Frequently Asked Questions");
        menuFAQ.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Show FAQ
			}
        	
        });
        
        help.add(menuFAQ);
        
        menubar.add(file);
        menubar.add(nav);
        menubar.add(set);
        menubar.add(Box.createHorizontalGlue());
        menubar.add(help);
        setJMenuBar(menubar);
	}
	
	/**
	 * Initializes the <code>JFrame</code> with all the necessary components. 
	 */
	private void init() {
		ImageIcon arrowright = null;
		ImageIcon arrowleft = null;
		
		try {
			arrowright = new ImageIcon(ImageIO.read(new File("res/arrow-right.png")));
			arrowleft = new ImageIcon(ImageIO.read(new File("res/arrow-left.png")));
		} catch (IOException e1) { }
		
		//Setting up the TextPane
		styledDocument = new DefaultStyledDocument();
		textPane = new JTextPane(styledDocument);
		textPane.setEditable(false);
		textPane.addMouseListener(new MouseAdapter() {            
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) if(rEpub != null) textPane.setText(rEpub.nextParagraph());
				if(e.getButton() == MouseEvent.BUTTON3) if(rEpub != null) textPane.setText(rEpub.previousParagraph());
			}
		});
		c.add(textPane,BorderLayout.CENTER);
		LoadTextSettings();
		UpdateText();
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		
		nextPage = new JButton("");
		nextPage.setIcon(arrowright);
		nextPage.setBorderPainted(false);
		nextPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { 
				if(rEpub != null) textPane.setText(rEpub.nextParagraph());
			}	
		});
		
		previousPage = new JButton("");
		previousPage.setIcon(arrowleft);
		previousPage.setBorderPainted(false);
		previousPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { 
				if(rEpub != null) textPane.setText(rEpub.previousParagraph());
			}	
		});
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout());
		
		feedBack = new JButton("Feedback");
		feedBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { 
				sendFeedback(); 
			}	
		});
		
		volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
		volumeSlider.setMinorTickSpacing(5);
		volumeSlider.setPaintTicks(true);
		volumeSlider.setPaintLabels(true);
		volumeSlider.setToolTipText("Change Volume");
		volumeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				volumeLabel.setText("Volume: " + volumeSlider.getValue() + "%");
			}
		});
		
		volumeLabel = new JLabel("Volume: " + volumeSlider.getValue() + "%");
		
		panel2.add(feedBack, BorderLayout.NORTH);
		panel2.add(volumeLabel, BorderLayout.WEST);
		panel2.add(volumeSlider, BorderLayout.CENTER);
		
		panel.add(previousPage);
		panel.add(panel2);
		panel.add(nextPage);
		
		c.add(panel,BorderLayout.SOUTH);
		
		setFocusable(true);
		
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyDispatcher());
	}
	
	/**
	 * Gets all the recent e-books opened from the file recent.sav
	 * @return HashMap with 2 strings, the title of a book and the path to the .epub file.
	 */
	public HashMap<String, String> getRecents() {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(RECENTS_FILE_PATH));
			String line;
			HashMap<String, String> recents = new HashMap<String, String>();
			while((line = reader.readLine()) != null) {
				String[] details = line.split("[|]");
				recents.put(details[0], details[1]);
			}
			reader.close();
			return recents;
		} catch (IOException e) { e.printStackTrace(); }
		return null;
	}
	
	/**
	 * Adds a book to the recently read books.
	 * @param title The title of the book.
	 * @param path The path to the .epub file.
	 */
	public void addToRecent(String title, String path) {
		BufferedReader reader;
		BufferedWriter writer;
		File file;
		File tempFile;
		try {
			file = new File(RECENTS_FILE_PATH);
			tempFile = new File(file.getAbsolutePath() + ".tmp");
			if(!file.exists()) file.createNewFile();
			if(!tempFile.exists()) tempFile.createNewFile();
			reader = new BufferedReader(new FileReader(file));
			writer = new BufferedWriter(new FileWriter(tempFile));
			String line;
			ArrayList<String> list = new ArrayList<String>();
			while((line = reader.readLine()) != null) {
				if(!line.equals(title + "|" + path)) list.add(line);
			}
			writer.append(title + "|" + path);
			writer.newLine();
			if(list.size() == 6) {
				for(int i=0; i<list.size()-1; i++) {
					writer.append(list.get(i));
				}
			}
			else {
				for(int i=0; i<list.size(); i++) {
					writer.append(list.get(i));
					writer.newLine();
				}
			}
			writer.close();
			reader.close();
			file.delete();
			tempFile.renameTo(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens a book in the window
	 * @param path The path to the .epub file.
	 */
	private void openBook(String path) {
		rEpub = new ReadEpub(path);
		textPane.setText(rEpub.getTitle());
		addToRecent(rEpub.getTitle(), path);
		menuGoTo.setEnabled(true);
        menuSavePage.setEnabled(true);
        menuLoadPage.setEnabled(true);
	}
	
	/**
	 * A custom KeyListener to listen for the left and right arrow keys for the page turning.
	 */
	private class KeyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
            	if(rEpub != null && e.getKeyCode() == KeyEvent.VK_RIGHT) textPane.setText(rEpub.nextParagraph());
            	if(rEpub != null && e.getKeyCode() == KeyEvent.VK_LEFT) textPane.setText(rEpub.previousParagraph());
            }
            return false;
        }
    }
	
	private void UpdateText() {
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setFontFamily(set, textFont);
		StyleConstants.setFontSize(set, textSize);
		if(isTextBold) StyleConstants.setBold(set, true);
		else StyleConstants.setBold(set, false);
		if(isTextItalic) StyleConstants.setItalic(set, true);
		else StyleConstants.setItalic(set, false);
		if(isTextUnderlined) StyleConstants.setUnderline(set, true);
		else StyleConstants.setUnderline(set, false);
		styledDocument.setCharacterAttributes(0, 99999, set, true);
		
		BufferedWriter writer;
		File file;
		try {
			file = new File(TEXT_CONFIG_FILE_PATH);
			if(file.exists()) file.delete();
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
			writer.append(textFont + "|" + textSize + "|" + isTextBold + "|" + isTextItalic + "|" + isTextUnderlined);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void LoadTextSettings() { 
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(TEXT_CONFIG_FILE_PATH));
			String line;
			line = reader.readLine();
			String[] settings = line.split("[|]");
			textFont = settings[0];
			textSize = Integer.parseInt(settings[1]);
			isTextBold = settings[2].equals("true");
			isTextItalic = settings[3].equals("true");
			isTextUnderlined = settings[4].equals("true");
			reader.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public void sendFeedback() {
		int tmp = JOptionPane.showConfirmDialog(null, "Are you sure you want to send feedback?", "Alert", JOptionPane.YES_NO_OPTION);
		if(tmp == 0) JOptionPane.showMessageDialog(null, "Thank you for your feedback!");
	}
	
	/**
	 * The main function to start the GUI.
	 * @param args Unused.
	 */
	public static void main(String[] args) {
		new GUI();
	}
}
