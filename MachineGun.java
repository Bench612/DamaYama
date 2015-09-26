class MachineGun extends Weapon {
	public MachineGun(MovingGameObjectWeapon p) {
		super(p, 4, 0.35, 0.025, 0, 3, 400, 1);
	}

	public String toString() {
		return "Machine Gun " + upgradeCount();
	}
}
