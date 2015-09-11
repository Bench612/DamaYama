import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;


class BoxHeadPanel extends PlayPanel {
	static int wave = 0;

	public BoxHeadPanel() {
		super();
		innerPanel = new StatPanel(this);
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
		startNewWave();
		for (int i = 0; i < 1; i++)
			addGameObject(new BasicEnemy());
	}

	int spawnsThisWave = 0;

	private void startNewWave() {
		wave++;
		spawnsThisWave = 0;
		displayMessage("Wave " + wave);
	}

	public double numberForWave() {
		return 10 * Math.pow(2, wave);
	}

	protected void addGameObject(MovingGameObject object) {
		super.addGameObject(object);
		spawnsThisWave += object.getPointCount();
	}

	public void updateSpawns() {
		super.updateSpawns();
		if (spawnsThisWave >= numberForWave()) {
			if (gameObjects.size() == players.size())
				startNewWave();
		} else if (timeSinceLastSpawn > 1 / (spawnCoeffecient * (wave + 1) / 100)) {
			double random = DamaYama.random();
			if (random > .4)
				addGameObject(new BasicEnemy());
			else if (random > .2)
				addGameObject(new SporeParent());
			else if (random > .11)
				addGameObject(new Demon());
			else
				addGameObject(new Jackal());
			timeSinceLastSpawn = 0;
		} else
			timeSinceLastSpawn++;
	}

	int timeSinceLastSpawn = 0;

	double spawnCoeffecient = 1;
	double decayCoeffecient = 0.00003;

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
