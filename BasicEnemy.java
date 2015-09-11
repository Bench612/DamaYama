import java.awt.*;
import java.util.ArrayList;

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
