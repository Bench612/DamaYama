class BasicWeapon extends Weapon {

	public BasicWeapon(MovingGameObjectWeapon p) {
		super(p, 20, 0.3, 0.2, 0, 1, Double.POSITIVE_INFINITY, 0.1);
	}

	public String toString() {
		return "Pistol " + upgradeCount();
	}
}