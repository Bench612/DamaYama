import drawing.*;

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
		super(1, 30, 1, DamaYama.orange);
		color = DamaYama.orange;
	}

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
		// draw head
		p.createNgon(x + width / 2, y + height / 2, z + getHeight() * 0.7,
				width / 2, direction + Math.PI / 4, 4);
		ArrayList<drawing.Point> bottom = p.getCurrentShape();
		p.createNgon(x + width / 2, y + height / 2, z + getHeight(), width / 2,
				direction + Math.PI / 4, 4);
		ArrayList<drawing.Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		// drawBody
		// drawBody
		p.createNgon(x + width / 2, y + height / 2, z, width / 2 * 0.75,
				direction + Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.createNgon(x + width / 2, y + height / 2, z + getHeight() * 0.6,
				width / 2 * 0.75, direction + Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		if (attackTime >= 0) {
			double attackPerc = 1 - (attackTime / (double) maxAttackTime);

			// draw arm1
			p.startNewConvexPolygon(6);
			p.createNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
					* (width / 2) + Math.cos(direction) * attackPerc
					* attackRange,
					y + height / 2 - Math.sin(direction + Math.PI / 2)
							* (height / 2) + Math.sin(direction) * attackPerc
							* attackRange,
					z + (getHeight() * 0.2) * (1 - attackPerc) + getHeight()
							* 0.4 * attackPerc, 0, direction + Math.PI / 4, 4);
			bottom = p.getCurrentShape();
			p.createNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
					* (width / 2),
					y + height / 2 - Math.sin(direction + Math.PI / 2)
							* (height / 2), z + getHeight() * 0.5, 0.1,
					direction + Math.PI / 4, 4);
			top = p.getCurrentShape();
			p.fillForm(p.createForm(top, bottom));
			p.fillShape(top);
			p.fillShape(bottom);
			p.finishConvexPolygon();

			// draw arm2
			p.startNewConvexPolygon(6);
			p.createNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
					* (width / 2) + Math.cos(direction) * attackPerc
					* attackRange,
					y + height / 2 - Math.sin(direction - Math.PI / 2)
							* (height / 2) + Math.sin(direction) * attackPerc
							* attackRange,
					z + (getHeight() * 0.2) * (1 - attackPerc) + getHeight()
							* 0.4 * attackPerc, 0, direction - Math.PI / 4, 4);
			bottom = p.getCurrentShape();
			p.createNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
					* (width / 2),
					y + height / 2 - Math.sin(direction - Math.PI / 2)
							* (height / 2), z + getHeight() * 0.5, 0.1,
					direction - Math.PI / 4, 4);
			top = p.getCurrentShape();
			p.fillForm(p.createForm(top, bottom));
			p.fillShape(top);
			p.fillShape(bottom);
			p.finishConvexPolygon();
		}
	}
}
