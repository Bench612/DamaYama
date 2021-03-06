import java.awt.Polygon;

public class Weapon {
	MovingGameObjectWeapon owner;
	double spread;
	double bulletsPerShot;
	double maxAmmo;
	int delay;
	double damage;
	double ammo;
	double accuracy;
	double knockback;

	public Weapon(MovingGameObjectWeapon p, int del, double dmg, double knock,
			double sp, double bulletsPer, double startingAmmo, double accurate) {
		delay = del;
		damage = dmg;
		knockback = knock;
		owner = p;
		spread = sp;
		bulletsPerShot = bulletsPer;
		ammo = startingAmmo;
		maxAmmo = startingAmmo;
		accuracy = accurate;
	}

	public boolean canFire() {
		return ammo >= bulletsPerShot;
	}

	public void loseAmmo() {
		ammo -= bulletsPerShot;
		if (ammo < 0)
			ammo = 0;
	}

	boolean ammoUpgrade, damageUpgrade, delayUpgrade;

	public String upgradeCount() {
		if (delayUpgrade)
			return "v.4";
		if (damageUpgrade)
			return "v.3";
		if (ammoUpgrade)
			return "v.2";
		return "";

	}

	public void upgrade() {
		if (!ammoUpgrade) {
			ammoUpgrade = true;
			maxAmmo *= 2;
			ammo = maxAmmo;
			return;
		}
		if (!damageUpgrade) {
			damageUpgrade = true;
			damage *= 2;
			return;
		} else if (!delayUpgrade) {
			delayUpgrade = true;
			delay /= 2;
			return;
		}
	}

	static int diagramParts = 4;

	public Polygon drawStatDiagram(int x, int y, int width, int height,
			Polygon outline) {
		double angle = 0;
		double angleIncrement = Math.PI * 2 / diagramParts;
		Polygon polygon = new Polygon();
		for (int i = 0; i < diagramParts; i++) {
			double value;
			switch (i) {
			case 0:
				value = 1 - accuracy;// wasted ammo
				break;
			case 1:
				value = (damage * bulletsPerShot / delay) / 1; // damage per
																	// time
				// (ideal)
				break;
			case 2:
				value = maxAmmo * damage / 800; // total damage
				break;
			default:
				value = ((knockback * bulletsPerShot) / delay) / 0.05; // total
																		// knockback per sec
				break;
			}
			value = Math.min(Math.max(value, 0), 1);
			polygon.addPoint(
					(int) (width / 2.0 * value * Math.cos(angle) + x + width / 2.0),
					(int) (height / 2.0 * value * Math.sin(angle) + y + height / 2.0));
			outline.addPoint(
					(int) (x + width / 2 + Math.cos(angle) * width / 2),
					(int) (y + height / 2 + Math.sin(angle) * height / 2));
			angle += angleIncrement;
		}
		return polygon;
	}
}
