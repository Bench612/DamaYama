import java.awt.Color;


class Demon extends ShootingEnemy {

	public Demon() {
		super(10, 40, 1);
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
}