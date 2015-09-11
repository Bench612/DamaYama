import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

class MapOpenFrame extends JFrame {
	public MapOpenFrame() {
		super("Select a Map");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			setContentPane(new MapOpenPanel(new Scanner(new File(
					Map.mapFileName)), this));
		} catch (FileNotFoundException e) {
			System.out
					.println("Could not load map properly...\nAttempting to write new file");
			try {
				FileWriter writer = new FileWriter(Map.mapFileName);
				writer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				setContentPane(new MapOpenPanel(new Scanner(new File(
						Map.mapFileName)), this));
			} catch (FileNotFoundException e1) {
				System.out.println("Could not write file");
				e1.printStackTrace();
			}
		}
		this.setBounds(0, 0, 200, 200);
		setVisible(true);
	}
}

class MapOpenPanel extends DamaPanel implements ActionListener {
	JComboBox maps;
	JButton openMap;
	Window container;
	public static String fileName = "MyMap.txt";
	public static final String defaultFileName = "MyMap.txt";

	public MapOpenPanel(Scanner scanner, JFrame jf) {
		super(new FlowLayout());
		container = jf;
		try {
			maps = new JComboBox(Map.getMapNames(scanner));
		} catch (IOException e) {
			maps = new JComboBox();
			System.out.println("No maps found");
		}
		maps.setEditable(true);

		openMap = new JButton("Open Map");
		openMap.addActionListener(this);
		add(maps);
		add(openMap);
	}

	public static Map openMap(String s) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(s));
		} catch (FileNotFoundException e) {
		}
		try {
			return new Map(scanner);
		} catch (IOException e) {
		}
		return null;
	}

	public void actionPerformed(ActionEvent e) {
		Map newMap = openMap(maps.getSelectedItem().toString());
		if (newMap != null) {
			Map.setNewMap(newMap);
			fileName = maps.getSelectedItem() + "";
			container.dispose();
		} else {
			JOptionPane.showMessageDialog(this, "Error in loading file");
		}
	}
}