import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

public class Lobby extends DamaPanel implements ActionListener, Runnable {
	String chatFileName = "Lobby.chat";
	String fileName = "Lobby.lob";
	String[] files;
	JComboBox comboBox;
	DamaYama parent;

	public Lobby(DamaYama parent) {
		super(new BorderLayout());
		this.parent = parent;
		this.setBackground(Color.black);
		if (!new File(chatFileName).exists()) {
			try {
				FileWriter writer = new FileWriter(chatFileName);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			files = Map.getMapNames(new Scanner(new File(Map.mapFileName)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JPanel center = new JPanel(new BorderLayout());
		comboBox = new JComboBox(files);
		comboBox.addActionListener(this);
		comboBox.setActionCommand("update");
		center.add(comboBox, BorderLayout.NORTH);
		center.add(new MapShowPanel(this), BorderLayout.CENTER);
		JButton start = new JButton("Start");
		start.addActionListener(this);
		start.setActionCommand("start");
		JButton quit = new JButton("Exit");
		quit.addActionListener(this);
		quit.setActionCommand("quit");
		JPanel south = new JPanel();
		south.add(start);
		south.add(quit);
		center.add(south, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);
		new Thread(this).start();
	}

	boolean hosting = false;
	boolean started = false;

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("update"))
			try {
				Map.setNewMapKeep(new Map(new Scanner(new File(files[comboBox
						.getSelectedIndex()]))));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		else if (e.getActionCommand().equals("start")) {
			if (!started && !hosting) {
				hosting = true;
				System.out.println("hosting");
			}
		} else {
			parent.reset();
		}
	}

	boolean running = false;

	public void run() {
		if (running)
			return;
		running = true;
		while (this.isVisible() && running && !quit) {
			if (hosting && !started) { // has to start the game
				FileWriter writer = null;
				while (writer == null) {
					System.out.println("hosting real");
					try {
						writer = new FileWriter(fileName);
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					writer.write("start" + "\n");
					writer.write(parent.username + "\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				started = true;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Scanner scanner;
			try {
				scanner = new Scanner(new File(fileName));
				if (scanner.hasNextLine()) {
					String firstLine = scanner.nextLine();
					if (firstLine.equals("start")) {
						scanner.close();
						if (!started) {
							started = true;
							FileWriter writer = null;
							try {
								writer = new FileWriter(fileName, true);
							} catch (IOException e) {
								e.printStackTrace();
							}
							while (writer == null) {
								try {
									Thread.sleep(2);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
								try {
									writer = new FileWriter(fileName, true);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							try {
								writer.write(parent.username + "\n");
								writer.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else {
						scanner.close();
						if (firstLine.length() > 1) {
							if (started)
								parent.switchTo(new MultiPlayerPanel(parent,
										firstLine));
							else
								parent.switchTo(new ReplayPanel(parent,
										firstLine));
							quit = true;
							running = false;
							return;
						}
					}
				} else
					scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (hosting && started) {
				int players = 0;
				try {
					scanner = new Scanner(new File(fileName));
					scanner.nextLine();
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						System.out.println(line);
						if (line.length() >= 1)
							players++;
					}
					scanner.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				if (players > 0) {
					System.out.println("players " + players);
					parent.switchTo(new MultiPlayerPanel(parent, players,
							actualGameName, fileName));
					FileWriter writer;
					try {
						writer = new FileWriter(chatFileName);
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					running = false;
					quit = true;
					return;
				}
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		running = false;
	}

	boolean quit = false;
	static String actualGameName = "Game.txt";
}

class MapShowPanel extends MapPanel {
	public MapShowPanel(Lobby lobby) {
		super(false, slant);
		setBackground(Color.black);
		try {
			Map.setNewMapKeep(new Map(new Scanner(new File(lobby.files[0]))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}