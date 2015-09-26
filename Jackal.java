import java.awt.Color;
import drawing.*;
import java.awt.Graphics;
import java.util.ArrayList;

class Jackal extends ShootingEnemy {
	public Jackal() {
		super(30, 40, 1, DamaYama.blue);
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

	protected Color getHitColor(MovingGameObject other) {
		if (this.isFacing(other))
			return color;
		else
			return super.getHitColor(other);
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
			return 2;
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

	static Color shieldColor = new Color(40, 120, 170, 170);
	public void drawSlant(Perspective p) {
		super.drawSlant(p);

		// draw head
		p.startNewConvexPolygon(5);
		p.createNgon(x + width / 2, y + height / 2, z + getHeight() * 0.8,
				width / 2, direction, 3);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.createNgon(x + width / 2, y + height / 2, z + getHeight(), width / 2,
				direction, 3);
		ArrayList<Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
		p.finishConvexPolygon();

		// drawBody
		p.startNewConvexPolygon(10);
		p.createNgon(x + width / 2, y + height / 2, z, width / 4 * 0.75,
				direction + Math.PI / 4, 8);
		bottom = p.getCurrentShape();
		p.createNgon(x + width / 2, y + height / 2, z + getHeight() * 0.6,
				width / 4 * 0.75, direction + Math.PI / 4, 8);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
		p.finishConvexPolygon();

		double attackPerc = 0;
		if (attackTime >= 0) {
			attackPerc = 1 - (attackTime / (double) maxAttackTime);
		}

		// draw arm1
		p.startNewConvexPolygon(6);
		p.createNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
				* (width / 2) + Math.cos(direction) * attackPerc * attackRange,
				y + height / 2 - Math.sin(direction + Math.PI / 2)
						* (height / 2) + Math.sin(direction) * attackPerc
						* attackRange, z + (getHeight() * 0.2)
						* (1 - attackPerc) + getHeight() * 0.4 * attackPerc, 0,
				direction + Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.createNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
				* (width / 2),
				y + height / 2 - Math.sin(direction + Math.PI / 2)
						* (height / 2), z + getHeight() * 0.5, 0.1, direction
						+ Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
		p.finishConvexPolygon();

		// draw arm2
		p.startNewConvexPolygon(6);
		p.createNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
				* (width / 2) + Math.cos(direction) * attackPerc * attackRange,
				y + height / 2 - Math.sin(direction - Math.PI / 2)
						* (height / 2) + Math.sin(direction) * attackPerc
						* attackRange, z + (getHeight() * 0.2)
						* (1 - attackPerc) + getHeight() * 0.4 * attackPerc, 0,
				direction - Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.createNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
				* (width / 2),
				y + height / 2 - Math.sin(direction - Math.PI / 2)
						* (height / 2), z + getHeight() * 0.5, 0.1, direction
						- Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
		p.finishConvexPolygon();

		//draw shield
		p.setColor(shieldColor);
		p.setOutline(color.darker());
		p.startNewShape(x + width / 2 + Math.cos(direction + Math.PI / 4)
				* (width / 2 + 0.01),
				y + height / 2 - Math.sin(direction + Math.PI / 4)
						* (height / 2 + 0.01), z, 4);
		p.addPoint(p.getScreenPoint(
				x + width / 2 + Math.cos(direction + Math.PI / 4)
						* (width / 2 + 0.01),
				y + height / 2 - Math.sin(direction + Math.PI / 4)
						* (height / 2 + 0.01), z + getHeight() * 0.6));
		p.addPoint(p.getScreenPoint(
				x + width / 2 + Math.cos(direction - Math.PI / 4)
						* (width / 2 + 0.01),
				y + height / 2 - Math.sin(direction - Math.PI / 4)
						* (height / 2 + 0.01), z + getHeight() * 0.6));
		p.addPoint(p.getScreenPoint(
				x + width / 2 + Math.cos(direction - Math.PI / 4)
						* (width / 2 + 0.01),
				y + height / 2 - Math.sin(direction - Math.PI / 4)
						* (height / 2 + 0.01), z));
		p.fillShape(p.getCurrentShape());
	}
}