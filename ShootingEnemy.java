import java.util.ArrayList;

class ShootingEnemy extends Enemy {
	public ShootingEnemy(int shei, int spee, int siz) {
		super(shei, spee, siz, DamaYama.blue);
	}

	public void update() {
		super.update();
		for (int i = 0; i < PlayPanel.currentRunning.players.size(); i++)
			if (this.isFacing(PlayPanel.currentRunning.players.get(i)))
				this.tryFireWeapon();
	}

}