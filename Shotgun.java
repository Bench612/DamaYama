class Shotgun extends Weapon {
	public Shotgun(MovingGameObjectWeapon p) {
		super(p, 20, 0.4, 0.1, Math.PI / 4, 6, 300, 0.1);
	}

	public String toString() {
		return "Shotgun " + upgradeCount();
	}
}