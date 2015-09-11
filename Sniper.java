
class Sniper extends Weapon {
	public Sniper(MovingGameObjectWeapon p) {
		super(p, 50, 5, 0, 0, 1, 20, 0.0001);
	}

	public String toString() {
		return "Sniper " + upgradeCount();
	}
}