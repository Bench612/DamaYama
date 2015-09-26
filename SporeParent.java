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

		p.createNgon(x + width / 2, y + height / 2, z, width / 2.5, direction
				+ Math.PI / 4, 4);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.createNgon(x + width / 2, y + height / 2, z + getHeight() * 0.5,
				width / 2 * 0.25, direction + Math.PI / 4, 4);
		ArrayList<Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		p.setColor(sphereColor);
		p.fillSphere(x + width / 2, y + height / 2, z + getHeight() * 0.8,
				width / 2, direction + Math.PI / 4, 8, 4);
		

		p.setColor(DamaYama.orange);
		p.fillSphere(x + width / 2 + Math.cos(direction + 2.6) * width * 0.3387 , y + height / 2 + Math.sin(direction + 2.6) * height * 0.3387, z + getHeight() * 0.8,
				width / 4, direction + Math.PI / 4, 8, 4);
		p.fillSphere(x + width / 2 + Math.cos(direction - 2.6) * width * 0.387, y + height / 2 + Math.sin(direction - 2.6) * height * 0.387, z + getHeight() * 0.8,
				width / 4, direction + Math.PI / 4, 8, 4);
		p.fillSphere(x + width / 2 + Math.cos(direction + Math.PI / 4) * width * 0.282, y + height / 2 + Math.sin(direction + Math.PI / 4) * height * 0.282, z + getHeight() * 0.8 - height*0.2,
				width / 4, direction + Math.PI / 4, 8, 4);
		p.fillSphere(x + width / 2  + Math.cos(direction -0.52) * width * 0.394, y + height / 2 + Math.sin(direction -0.52) * height * 0.394, z + getHeight() * 0.8 + height*0.3,
				width / 4, direction + Math.PI / 4, 8, 4);
	}

	double explosionDamage = 1.5;

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