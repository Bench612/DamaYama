import java.util.ArrayList;

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