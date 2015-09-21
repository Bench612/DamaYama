import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;

import javax.swing.JPanel;

class BoxHeadPanel extends PlayPanel {
	static int wave = 0;

	public BoxHeadPanel(DamaYama frame) {
		super(frame);
		innerPanel = new StatsPanel(this);
		running = true;
		new Thread(this).start();
	}

	public void start() {
		super.start();
		players = new ArrayList<Player>();
		if (allKeys == null) {
			players.add(new Player(this, 0));
			directionX = new int[1];
			directionY = new int[1];
		} else {
			for (int i = 0; i < allKeys.length; i++)
				players.add(new Player(this, i));
			mainSleepSpeed = normalMainSleepSpeed / allKeys.length;
		}
		for (int i = 0; i < players.size(); i++)
			addGameObject(players.get(i));
		wave = -1;
		timeSinceLastSpawn = Integer.MAX_VALUE;
		startNewWave();
	}

	private int spawnsThisWave = 0;

	private void startNewWave() {
		wave++;
		spawnsThisWave = 0;
		displayMessage("Wave " + (wave + 1));
	}

	public double numberForWave() {
		return 10 * Math.pow(2, wave);
	}

	private void introduceGameObject(MovingGameObject object) {
		addGameObject(object);
		spawnsThisWave += object.getPointCount();
	}

	public void updateSpawns() {
		super.updateSpawns();
		if (spawnsThisWave >= numberForWave()) {
			if (gameObjects.size() == players.size() && toSpawn.isEmpty())
				startNewWave();
		} else if (timeSinceLastSpawn > 100 / (spawnCoeffecient * (wave + 1))) {
			System.out.println("adding");
			double random = DamaYama.random();
			if (random > .4)
				introduceGameObject(new BasicEnemy());
			else if (random > .2)
				introduceGameObject(new SporeParent());
			else if (random > .11)
				introduceGameObject(new Demon());
			else
				introduceGameObject(new Jackal());
			timeSinceLastSpawn = 0;
		} else
			timeSinceLastSpawn++;
	}

	int timeSinceLastSpawn = 0;

	double spawnCoeffecient = 0.33;

	public void paintComponent(Graphics g) {
		if (!started)
			return;
		super.paintComponent(g);
		Font original = g.getFont();
		Font fontLarge = original.deriveFont(32.0f);
		g.setFont(fontLarge);
		int height = g.getFontMetrics().getAscent();
		g.setColor(Color.black);
		g.drawString(
				getPlayer().weapon + ": Ammo - " + getPlayer().weapon.ammo, g
						.getFontMetrics().getHeight() + 5, g.getFontMetrics()
						.getHeight() + 5);
		g.setFont(original);
		int y = drawMessages(g, getWidth() / 2, height + 10);
		g.setColor(Color.blue.darker().darker());
		FontMetrics fm = g.getFontMetrics();
		g.drawString(chatString, getWidth() / 2 - fm.stringWidth(chatString)
				/ 2, y + fm.getHeight());
		Polygon outline = new Polygon();
		Polygon poly = getPlayer().weapon.drawStatDiagram(5, 5, g
				.getFontMetrics().getHeight() * 2, g.getFontMetrics()
				.getHeight() * 2, outline);
		g.drawPolygon(outline);
		g.setColor(Color.red);
		g.fillPolygon(poly);
	}

	public void addNewWeapon(Weapon w) {
		if (getPlayer().getWeapon(w.getClass()) == null)
			getPlayer().addNewWeapon(w);
	}

}

class StatsPanel extends JPanel {
	PlayPanel play;

	public int getAccuracy() {
		try {
			return (int) Math.round((double) play.getPlayer().hits
					/ (play.getPlayer().misses + play.getPlayer().hits) * 100);
		} catch (Exception e) {
			return 0;
		}
	}

	public int getHealth() {
		try {
			return (int) Math
					.round((double) play.getPlayer().getHealth() * 100);
		} catch (Exception e) {
			return 100;
		}
	}

	public double getDMG() {
		try {
			return -Math.round(play.getPlayer().totalDamage * 100);
		} catch (Exception e) {
			return 0;
		}
	}

	public StatsPanel(BoxHeadPanel p) {
		play = p;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.black);
		g.drawString("Wave :" + (BoxHeadPanel.wave + 1), 0, g.getFontMetrics()
				.getHeight());
		g.drawString("Accuracy :" + getAccuracy() + "%", 0, (int) (g
				.getFontMetrics().getHeight() * 2.5));
		g.drawString("Health :" + getHealth() + "%", 0, (int) (g
				.getFontMetrics().getHeight() * 4));
		g.drawString("Damage :" + getDMG(), 0, (int) (g.getFontMetrics()
				.getHeight() * 5.5));
	}
}
