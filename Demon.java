import java.awt.Color;
import drawing.*;
import java.util.ArrayList;

class Demon extends ShootingEnemy {

	public Demon() {
		super(10, 40, 1, DamaYama.blue);
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

	public void drawSlant(Perspective p) {
		super.drawSlant(p);

		// drawBody
		p.startNewConvexPolygon(6);
		p.drawNgon(x + width / 2, y + height / 2, z, width / 2 * 0.75,
				direction + Math.PI / 4, 4);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.6,
				width / 2 * 0.75, direction + Math.PI / 4, 4);
		ArrayList<Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
		p.finishConvexPolygon();

		// draw head
		p.startNewConvexPolygon(7);
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.7,
				width / 2, direction + Math.PI / 5, 5);
		bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight(), width / 2,
				direction + Math.PI / 5, 5);
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
		p.setColor(Color.black);
		p.startNewConvexPolygon(6);
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
		p.finishConvexPolygon();

		// draw arm2
		p.startNewConvexPolygon(6);
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
		p.finishConvexPolygon();
	}
}