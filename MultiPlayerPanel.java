import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

class MultiPlayerPanel extends BoxHeadPanel {
	public static final String extension = ".game";
	static File lastGameReplay;
	String fileName;
	boolean host, addedIntoGame = false;
	int currentUpdatingIndex = 0;// it is a player index for updating
	long bytesRead = 0;

	protected boolean saveTempReplay() {
		try {
			lastGameReplay = new File("LastReplay.txt");
			copyFile(new File(fileName), lastGameReplay);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static final void copyFile(File original, File newFile)
			throws IOException {
		FileInputStream stream = new FileInputStream(original);
		FileWriter writer = new FileWriter(newFile);
		while (stream.available() > 0)
			writer.write(stream.read());
		stream.close();
		writer.close();
	}

	public void tryClose() {
		super.tryClose();
		while (!saveTempReplay()) {
			System.out.println("failed to save replay");
		}
	}

	public void paintComponent(Graphics g) {
		if (addedIntoGame && started) {
			super.paintComponent(g);
		} else {
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			Font orig = g.getFont();
			g.setFont(orig.deriveFont(32f));
			String message;
			if (addedIntoGame)
				message = "Please wait... Waiting for other players";
			else if (doneHosting || !host)
				message = "Please wait... Adding into game";
			else
				message = "Please wait... Hosting";
			g.setColor(Color.black);
			g.drawString(message, 0, g.getFontMetrics().getHeight());
			g.setFont(orig);
		}
	}

	String toWriteTo = "";// writes on a file after its done hosting

	public MultiPlayerPanel(int players, String file, String fileToWriteOn) {
		host = true;
		fileName = file;
		toWriteTo = fileToWriteOn;
		allKeys = new boolean[players][keys.length];
		directionX = new int[keys.length];
		directionY = new int[keys.length];
		allPreviousKeys = new boolean[players][previousKeys.length];
	}

	public void focusLost(FocusEvent e) {
	}

	public MultiPlayerPanel(String file) {
		host = false;
		fileName = file;
	}

	public static final int maxArray = 7;

	public boolean write(boolean[] keys) {
		try {
			FileWriter w = new FileWriter(fileName, true);
			for (int startIndex = 0; startIndex < keys.length; startIndex += maxArray)
				write(w, keys, startIndex, startIndex + maxArray);
			w.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean write(int a, int b) {
		try {
			FileWriter w = new FileWriter(fileName, true);
			if (a < 0)
				w.write(negative);
			else
				w.write(positive);
			w.write(Math.abs(a));
			if (b < 0)
				w.write(negative);
			else
				w.write(positive);
			w.write(Math.abs(b));
			w.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	// end index is non inclusive
	private static void write(FileWriter w, boolean[] keys, int startIndex,
			int endIndex) {
		if (keys.length < endIndex)
			endIndex = keys.length;
		byte number = 0;
		for (int i = startIndex; i < endIndex; i++) {
			if (keys[i])
				number += Math.pow(2, i - startIndex);
		}
		try {
			w.write(number);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	final static int negative = 0;
	final static int positive = 1;

	public long readNewDirections(int index, boolean limit) {
		long timesSlept = 0;
		closeInputStream();
		resetInputStream();
		try {
			while (inputStream.available() < 4) {
				closeInputStream();
				if (timesSlept > 1000 && limit)
					return timesSlept;
				try {
					timesSlept++;
					timeSinceUpdate += sleep;
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resetInputStream();
			}
			try {
				int a2 = inputStream.read();
				int a = inputStream.read();
				int b2 = inputStream.read();
				int b = inputStream.read();
				directionX[index] = a;
				if (a2 == negative)
					directionX[index] *= -1;
				directionY[index] = b;
				if (b2 == negative)
					directionY[index] *= -1;
				bytesRead += 4;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeInputStream();
		return timesSlept;
	}

	public long read(boolean[] allKeys, boolean limit) {
		long timesSlept = 0;
		closeInputStream();
		resetInputStream();
		try {
			while (inputStream.available() < Math.ceil(allKeys.length
					/ (double) maxArray)) {
				closeInputStream();
				if (timesSlept > 1000 && limit)
					return timesSlept;
				try {
					timesSlept++;
					timeSinceUpdate += sleep;
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resetInputStream();
			}
			for (int startIndex = 0; startIndex < allKeys.length; startIndex += maxArray)
				read(inputStream, allKeys, startIndex, startIndex + maxArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeInputStream();
		return timesSlept;
	}

	// end index is non inclusive
	private void read(FileInputStream stream, boolean[] allKeys,
			int startIndex, int endIndex) {
		if (allKeys.length < endIndex)
			endIndex = allKeys.length;
		try {
			int number = stream.read();
			bytesRead++;
			for (int i = endIndex - 1; i >= startIndex; i--) {
				if ((number / Math.pow(2, i - startIndex) < 1))
					allKeys[i] = false;
				else {
					allKeys[i] = true;
					number -= Math.pow(2, i - startIndex);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final int sleep = 2;
	FileInputStream inputStream = null;

	public void update() {
		if (addedIntoGame && started) {
			// currentUpdatingIndex should be zero at the start!
			while (currentUpdatingIndex < playerIndex) {
				if (readPart())
					return;
			}
			writePart();
			// now finishes reading the rest
			while (currentUpdatingIndex < allKeys.length) {
				if (readPart())
					return;
			}
			currentUpdatingIndex = 0;
			super.update();
		}
	}

	private boolean writePart() {
		// writes my current keys
		while (!write(keys)) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
		while (!write(newShootDirectionX, newShootDirectionY)) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private boolean readPart() {
		if (read(allKeys[currentUpdatingIndex], true) > 5000 / sleep) {
			JOptionPane.showMessageDialog(this, "Latency too high!");
			tryClose();
			return true;
		}
		if (readNewDirections(currentUpdatingIndex, true) > 5000 / sleep) {
			JOptionPane.showMessageDialog(this, "Latency too high!");
			tryClose();
			return true;
		}
		currentUpdatingIndex++;
		return false;
	}

	public void updatePlayers() {
		for (int i = 0; i < players.size(); i++) {
			players.get(i).update(allKeys[i], allPreviousKeys[i],
					directionX[i], directionY[i]);
		}
	}

	public static final int seedParts = 3;

	boolean doneHosting = false;

	public void innerRun() {
		if (!addedIntoGame) {
			if (host && !doneHosting) {
				try {
					FileWriter writer = new FileWriter(fileName);
					writer.write(allKeys.length);
					for (int i = 0; i < seedParts; i++)
						writer.write((byte) (Math.random() * 256));
					Map.getCurrent().write(writer);
					writer.close();
					FileWriter clear = new FileWriter(chatFile);
					clear.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				doneHosting = true;
			}
			repaint();
			while (!write(keys)) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			repaint();
			// now tries to get its currentIndex
			while (inputStream == null) {
				playerIndex = -1;
				resetInputStream();
				int players;
				try {
					players = inputStream.read();
					bytesRead++;
					allKeys = new boolean[players][keys.length];
					directionX = new int[keys.length];
					directionY = new int[keys.length];
					allPreviousKeys = new boolean[players][previousKeys.length];
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					long seed = 0;
					for (int i = 0; i < seedParts; i++) {
						seed += inputStream.read();
						bytesRead++;
					}
					DamaYama.setSeed(seed);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				// now loads the map
				try {
					bytesRead += Map.setNewMapKeep(inputStream);
					Map.getCurrent().compile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				closeInputStream();
				addedIntoGame = true;
				if (host) {
					if (toWriteTo.length() > 1) {
						FileWriter writer = null;
						while (writer == null) {
							try {
								writer = new FileWriter(toWriteTo);
							} catch (IOException e) {
								e.printStackTrace();
							}
							if (writer == null)
								try {
									Thread.sleep(sleep);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
						}
						try {
							writer.write(fileName);
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				// now waits for other players
				for (currentUpdatingIndex = 0; currentUpdatingIndex < allKeys.length; currentUpdatingIndex++) {
					if ((read(allPreviousKeys[currentUpdatingIndex], false) >= 1)
							&& playerIndex == -1)
						playerIndex = currentUpdatingIndex - 1;
				}
				if (playerIndex == -1)
					playerIndex = allKeys.length - 1;
				System.out.println("player #" + playerIndex + " out of "
						+ allKeys.length);
				// now resets the currentUpdatingIndex
				currentUpdatingIndex = 0;
			}
		}
	}

	private void closeInputStream() {
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void resetInputStream() {
		try {
			long actualSkip = -1;
			inputStream = new FileInputStream(fileName);
			actualSkip = inputStream.skip(bytesRead);
			while (actualSkip != bytesRead) {
				closeInputStream();
				System.out.println(actualSkip + "actual Skip" + bytesRead);
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				inputStream = new FileInputStream(fileName);
				actualSkip = inputStream.skip(bytesRead);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}