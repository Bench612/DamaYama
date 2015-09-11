import java.awt.Color;
import java.awt.Graphics;


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