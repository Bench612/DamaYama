
import drawing.*;
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