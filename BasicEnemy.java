import java.awt.*;
import java.util.ArrayList;

class Enemy extends MovingGameObjectWeapon {

	Color color;

	double attackRange = 0.6;
	public static final int maxAttackAngle = 180;
	public static final int armAngle = 30;
	int attackTime = -1;
	final static int maxAttackTime = 25;

	protected Enemy(double sheildMulti, int speed, double size, Color c) {
		super(Math.max(speed - BoxHeadPanel.wave, 2), 0.75 * size, 0.75 * size,
				sheildMulti);
		color = c;
		attackRange *= size;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		g.setColor(color);
		super.draw(g, xOff, yOff);
		if (timeSinceHealthChange <= 100)
			drawHealthBar(g, xOff, yOff);
	}

	public void drawSlant(Perspective p) {
		if (timeSinceHealthChange <= 100)
			drawHealthBarSlant(p);
		p.setColor(color);
		p.setOutline(color.darker());
		super.drawSlant(p);
	}

	public void update() {
		timeSinceHealthChange++;
		super.update();
		moving = false;
		if (attackTime == 0) {
			attackTime--;
			attack();
		} else if (attackTime > 0)
			attackTime--;
		else {
			if (!knockedBack) {
				followFocus();
				moving = attackTime < 0;
			} else {
				knockedBack = false;
				if (Math.abs(knockbackSpeedX) > knockbackFriction) {
					if (knockbackSpeedX < 0)
						knockbackSpeedX += knockbackFriction;
					else
						knockbackSpeedX -= knockbackFriction;
					knockedBack = true;
				}
				if (Math.abs(knockbackSpeedY) > knockbackFriction) {
					if (knockbackSpeedY < 0)
						knockbackSpeedY += knockbackFriction;
					else
						knockbackSpeedY -= knockbackFriction;
					knockedBack = true;
				}
				moveDown(knockbackSpeedY);
				moveRight(knockbackSpeedX);
			}
		}
	}

	protected void startAttack(Player p) {
		attackTime = maxAttackTime;
	}

	protected void attack() {
		double attackX = Math.cos(direction) * attackRange + x + width / 2;
		double attackY = Math.sin(direction) * -attackRange + y + height / 2; // negative
		int xI = getXIndex(attackX);
		int yI = getYIndex(attackY);
		for (int i = 0; i < Map.getCurrent().spaces[xI][yI].objects.size(); i++) {
			MovingGameObject object = Map.getCurrent().spaces[xI][yI].objects
					.get(i);
			if (Player.class.isInstance(object)
					&& object.contains(attackX, attackY)) {
				((Player) object).changeHealth(-.5, this);
				changeHealth(0.15, object);
			}
		}
	}

	public void die() {
		super.die();
		makeItem();
		PlayPanel.currentRunning.removeFromGameObjects(this);
	}

	public void makeItem() {
	}

	public void atObstacle(MovingGameObject other) {
		if (Player.class.isInstance(other))
			startAttack((Player) other);
	}

}

public class BasicEnemy extends Enemy {

	public int getPointCount() {
		return 1;
	}

	public void makeItem() {
		new Health(this);
	}

	public BasicEnemy() {
		super(1, 40, 1, DamaYama.orange);
		color = DamaYama.orange;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		if (attackTime >= 0) {
			g.setColor(Color.black);
			int attackAngle = maxAttackAngle * attackTime / maxAttackTime;
			int attackDirection = (int) Math.toDegrees(direction);
			g.fillArc((int) ((x - attackRange / 2) * Space.SIZE + xOff),
					(int) ((y - attackRange / 2) * Space.SIZE) + yOff,
					(int) ((width + attackRange) * Space.SIZE),
					(int) ((height + attackRange) * Space.SIZE),
					attackDirection + attackAngle / 2 + armAngle, -armAngle);
			g.fillArc((int) ((x - attackRange / 2) * Space.SIZE + xOff),
					(int) ((y - attackRange / 2) * Space.SIZE) + yOff,
					(int) ((width + attackRange) * Space.SIZE),
					(int) ((height + attackRange) * Space.SIZE),
					attackDirection - attackAngle / 2 - armAngle, armAngle);
		}
		g.setColor(color);
		super.draw(g, xOff, yOff);
		if (timeSinceHealthChange <= 100)
			drawHealthBar(g, xOff, yOff);
	}

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
		// draw head
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.6,
				width / 2, direction + Math.PI / 4, 4);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight(), width / 2,
				direction + Math.PI / 4, 4);
		ArrayList<Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		// drawBody
		p.drawNgon(x + width / 2, y + height / 2, z, width / 2 * 0.85,
				direction + Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.6,
				width / 2 * 0.85, direction + Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		if (attackTime >= 0) {
			double attackPerc = 1 - (attackTime / (double) maxAttackTime);

			// draw arm1
			p.drawNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
					* (width / 2) + Math.cos(direction) * attackPerc
					* attackRange,
					y + height / 2 - Math.sin(direction + Math.PI / 2)
							* (height / 2) + Math.sin(direction) * attackPerc
							* attackRange,
					z + (getHeight() * 0.2) * (1 - attackPerc) + getHeight()
							* 0.4 * attackPerc, 0, direction + Math.PI / 4, 4);
			bottom = p.getCurrentShape();
			p.drawNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
					* (width / 2),
					y + height / 2 - Math.sin(direction + Math.PI / 2)
							* (height / 2), z + getHeight() * 0.5, 0.1,
					direction + Math.PI / 4, 4);
			top = p.getCurrentShape();
			p.fillForm(p.createForm(top, bottom));
			p.fillShape(top);
			p.fillShape(bottom);

			// draw arm2
			p.drawNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
					* (width / 2) + Math.cos(direction) * attackPerc
					* attackRange,
					y + height / 2 - Math.sin(direction - Math.PI / 2)
							* (height / 2) + Math.sin(direction) * attackPerc
							* attackRange,
					z + (getHeight() * 0.2) * (1 - attackPerc) + getHeight()
							* 0.4 * attackPerc, 0, direction - Math.PI / 4, 4);
			bottom = p.getCurrentShape();
			p.drawNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
					* (width / 2),
					y + height / 2 - Math.sin(direction - Math.PI / 2)
							* (height / 2), z + getHeight() * 0.5, 0.1,
					direction - Math.PI / 4, 4);
			top = p.getCurrentShape();
			p.fillForm(p.createForm(top, bottom));
			p.fillShape(top);
			p.fillShape(bottom);
		}
	}
}

class ShootingEnemy extends Enemy {
	public ShootingEnemy(int shei, int spee, int siz) {
		super(shei, spee, siz, DamaYama.blue);
	}

	public void update() {
		super.update();
		for (int i = 0; i < PlayPanel.currentRunning.players.size(); i++)
			if (this.isFacing(PlayPanel.currentRunning.players.get(i)))
				this.tryFireWeapon();
	}

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
		// draw head
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.6,
				width / 2, direction + Math.PI / 5, 5);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight(), width / 2,
				direction + Math.PI / 5, 5);
		ArrayList<Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		// drawBody
		p.drawNgon(x + width / 2, y + height / 2, z, width / 2 * 0.75,
				direction + Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.6,
				width / 2 * 0.75, direction + Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		double attackPerc = 0;
		if (attackTime >= 0) {
			attackPerc = 1 - (attackTime / (double) maxAttackTime);
		}

		// draw arm1
		p.drawNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
				* (width / 2) + Math.cos(direction) * attackPerc * attackRange,
				y + height / 2 - Math.sin(direction + Math.PI / 2)
						* (height / 2) + Math.sin(direction) * attackPerc
						* attackRange, z + (getHeight() * 0.2)
						* (1 - attackPerc) + getHeight() * 0.4 * attackPerc, 0,
				direction + Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
				* (width / 2),
				y + height / 2 - Math.sin(direction + Math.PI / 2)
						* (height / 2), z + getHeight() * 0.5, 0.1, direction
						+ Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		// draw arm2
		p.drawNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
				* (width / 2) + Math.cos(direction) * attackPerc * attackRange,
				y + height / 2 - Math.sin(direction - Math.PI / 2)
						* (height / 2) + Math.sin(direction) * attackPerc
						* attackRange, z + (getHeight() * 0.2)
						* (1 - attackPerc) + getHeight() * 0.4 * attackPerc, 0,
				direction - Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
				* (width / 2),
				y + height / 2 - Math.sin(direction - Math.PI / 2)
						* (height / 2), z + getHeight() * 0.5, 0.1, direction
						- Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
	}
}

class Demon extends ShootingEnemy {

	public Demon() {
		super(10, 40, 1);
		double random = DamaYama.random();
		if (random > 0.6)
			weapon = new BasicWeapon(this);
		else if (random > 0.33)
			weapon = new SMG(this);
		else if (random > 0.30) {
			weapon = new Sniper(this);
			color = Color.yellow;
		} else if (random > 0.15)
			weapon = new MachineGun(this);
		else
			weapon = new Shotgun(this);
	}

	public int getPointCount() {
		return 4;
	}

	public void makeItem() {
		new Ammo(this);
	}
}

class Jackal extends ShootingEnemy {
	public Jackal() {
		super(30, 40, 1);
		double random = DamaYama.random();
		if (random > 0.4)
			weapon = new BasicWeapon(this);
		else if (random > 0.25)
			weapon = new SMG(this);
		else if (random > 0.20) {
			weapon = new Sniper(this);
			color = Color.yellow;
		} else if (random > 0.1)
			weapon = new Shotgun(this);
		else
			weapon = new MachineGun(this);
	}

	public void update() {
		super.update();
		for (int i = 0; i < PlayPanel.currentRunning.players.size(); i++)
			if (this.isFacing(PlayPanel.currentRunning.players.get(i)))
				this.tryFireWeapon();
	}

	public void makeItem() {
		new Upgrade(this);
	}

	public double getSheild(MovingGameObject other) {
		// stronger sheild in the front
		if (this.isFacing(other))
			return super.getSheild(other);
		else
			return 1.5;
	}

	public int getPointCount() {
		return 5;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		super.draw(g, xOff, yOff);
		g.setColor(color.darker());
		g.fillArc((int) (x * Space.SIZE) + xOff, (int) (y * Space.SIZE) + yOff,
				(int) (width * Space.SIZE), (int) (height * Space.SIZE),
				(int) Math.toDegrees(this.direction) + 90, 180);
	}

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
		p.setColor(color.darker());
		p.setOutline(Color.black);
		p.startNewShape(x + width / 2 + Math.cos(direction + Math.PI / 4)
				* (width / 2),
				y + height / 2 - Math.sin(direction + Math.PI / 4)
						* (height / 2), z, 4);
		p.addPoint(p.getScreenPoint(
				x + width / 2 + Math.cos(direction + Math.PI / 4) * (width / 2),
				y + height / 2 - Math.sin(direction + Math.PI / 4)
						* (height / 2), z + getHeight() * 0.6));
		p.addPoint(p.getScreenPoint(
				x + width / 2 + Math.cos(direction - Math.PI / 4) * (width / 2),
				y + height / 2 - Math.sin(direction - Math.PI / 4)
						* (height / 2), z + getHeight() * 0.6));
		p.addPoint(p.getScreenPoint(
				x + width / 2 + Math.cos(direction - Math.PI / 4) * (width / 2),
				y + height / 2 - Math.sin(direction - Math.PI / 4)
						* (height / 2), z));
		p.fillShape(p.getCurrentShape());
	}
}

class Spore extends Enemy {
	int spawnX, spawnY;

	public Spore(double x, double y) {
		super(0.1, 10, 0.4, DamaYama.orange);
		spawnX = (int) x;
		spawnY = (int) y;
	}

	public boolean spawn() {
		return this.spawn(spawnX, spawnY);
	}

	protected void startAttack(Player p) {
		p.changeHealth(-0.4, this);
		die();
	}

	public void makeItem() {
	}

	public double getHeight() {
		return width;
	}

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
		p.drawSphere(x + width / 2, y + height / 2, z + getHeight() / 2,
				width / 2, direction, 6, 3);
	}

	public int getPointCount() {
		return 0;
	}
}

class SporeParent extends Enemy {
	public SporeParent() {
		super(0.75, 30, 1, DamaYama.orange.darker());
	}

	public int getPointCount() {
		return 1;
	}

	int explosionRange = 2;

	protected void startAttack(Player p) {
		die();
	}

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
		p.drawSphere(x + width / 2, y + height / 2, z + getHeight() * 0.75,
				width / 2, direction + Math.PI / 4, 8, 4);

		p.drawNgon(x + width / 2, y + height / 2, z, width / 2, direction
				+ Math.PI / 4, 4);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.5,
				width / 2 * 0.25, direction + Math.PI / 4, 4);
		ArrayList<Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
	}

	double explosionDamage = 1;

	public void die() {
		explode(x, y, z, width, height, explosionRange, explosionDamage);
		PlayPanel.currentRunning.addGameObject(new Spore(x + width / 2, y
				+ height / 2));
		PlayPanel.currentRunning.addGameObject(new Spore(x, y));
		PlayPanel.currentRunning.addGameObject(new Spore(x + width / 2, y));
		PlayPanel.currentRunning.addGameObject(new Spore(x, y + height / 2));
		super.die();
	}
}