class SMG extends Weapon {
	public SMG(MovingGameObjectWeapon p) {
		super(p, 8, 0.25, 0.1, 0, 1, 800, 0.4);
	}

	public String toString() {
		return "SMG " + upgradeCount();
	}
}