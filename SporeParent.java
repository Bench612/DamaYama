import java.util.ArrayList;


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
		p.drawSphere(x + width / 2, y + height / 2, z + getHeight() * 0.8,
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