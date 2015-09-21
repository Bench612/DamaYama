import java.awt.Color;
import java.util.ArrayList;

import drawing.*;

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

	Color sphereColor = new Color(235, 169, 116).darker();

	public void drawSlant(Perspective p) {
		super.drawSlant(p);

		p.drawNgon(x + width / 2, y + height / 2, z, width / 2.5, direction
				+ Math.PI / 4, 4);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.5,
				width / 2 * 0.25, direction + Math.PI / 4, 4);
		ArrayList<Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		p.setColor(sphereColor);
		p.drawSphere(x + width / 2, y + height / 2, z + getHeight() * 0.8,
				width / 2, direction + Math.PI / 4, 8, 4);
		

		p.setColor(DamaYama.orange);
		p.drawSphere(x + width / 2 - width / 3, y + height / 2 + height * 0.2, z + getHeight() * 0.8,
				width / 4, direction + Math.PI / 4, 8, 4);
		p.drawSphere(x + width / 2 - width * 0.3, y + height / 2 - height * 0.22, z + getHeight() * 0.8,
				width / 4, direction + Math.PI / 4, 8, 4);
		p.drawSphere(x + width / 2 + width * 0.2, y + height / 2 + height * 0.2, z + getHeight() * 0.8 - height*0.2,
				width / 4, direction + Math.PI / 4, 8, 4);
		p.drawSphere(x + width / 2 + width * 0.34, y + height / 2 - height * 0.2, z + getHeight() * 0.8 + height*0.3,
				width / 4, direction + Math.PI / 4, 8, 4);
	}

	double explosionDamage = 0.8;

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