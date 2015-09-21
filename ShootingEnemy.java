import java.awt.Color;
import java.util.ArrayList;

import drawing.Perspective;

class ShootingEnemy extends Enemy {
	public ShootingEnemy(int shei, int spee, int siz, Color c) {
		super(shei, spee, siz, c);
	}

	public void update() {
		super.update();
		for (int i = 0; i < PlayPanel.currentRunning.players.size(); i++)
			if (this.isFacing(PlayPanel.currentRunning.players.get(i)))
				this.tryFireWeapon();
	}

	static Color aimColor = new Color(200, 50, 50, 30);

	@Override
	public void drawSlant(Perspective p) {
		if (weapon.canFire()) {
			p.setOutline(aimColor);
			p.setColor(aimColor);
			double angle = Math.atan2(getShootDirectionY(),
					getShootDirectionX());
			p.startNewShape(x + width / 2, y + height / 2, z + getHeight() / 2,
					2);
			p.addPoint(p.getScreenPoint(
					x
							+ width
							/ 2
							+ (Math.cos(angle
									- (weapon.accuracy + weapon.spread) / 2) * 2),
					y
							+ width
							/ 2
							+ (Math.sin(angle
									- (weapon.accuracy + weapon.spread) / 2) * 2),
					z + getHeight() / 2));
			p.addPoint(p.getScreenPoint(
					x
							+ width
							/ 2
							+ (Math.cos(angle
									+ (weapon.accuracy + weapon.spread) / 2) * 2),
					y
							+ width
							/ 2
							+ (Math.sin(angle
									+ (weapon.accuracy + weapon.spread) / 2) * 2),
					z + getHeight() / 2));
			p.fillShape(p.getCurrentShape());
		}

		super.drawSlant(p);
	}

}