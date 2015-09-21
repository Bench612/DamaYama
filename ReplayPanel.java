import java.awt.event.KeyEvent;

class ReplayPanel extends MultiPlayerPanel {
	public ReplayPanel(DamaYama frame, String file) {
		super(frame, file);
	}

	public boolean write(byte[] bytes) {
		return true;
	}

	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		if (e.getKeyCode() == KeyEvent.VK_RIGHT
				|| e.getKeyCode() == KeyEvent.VK_D) {
			playerIndex++;
			if (playerIndex >= players.size())
				playerIndex = 0;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT
				|| e.getKeyCode() == KeyEvent.VK_A) {
			playerIndex--;
			if (playerIndex < 0)
				playerIndex = players.size() - 1;
		} else if (e.getKeyCode() == KeyEvent.VK_W
				|| e.getKeyCode() == KeyEvent.VK_UP) {
			mainSleepSpeed /= 2;
			mainSleepSpeed = Math.max(mainSleepSpeed, 5);
		} else if (e.getKeyCode() == KeyEvent.VK_S
				|| e.getKeyCode() == KeyEvent.VK_DOWN) {
			mainSleepSpeed *= 2;
			mainSleepSpeed = Math.min(mainSleepSpeed, 80);
		}
	}
}
