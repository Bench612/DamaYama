import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;

class ReplayPanel extends MultiPlayerPanel {
	public ReplayPanel(DamaYama frame, String file) {
		super(frame, file);
	}

	public boolean write(boolean[] keys) {
		return true;
	}

	public boolean write(boolean[] keys, int a, int b) {
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
				|| e.getKeyCode() == KeyEvent.VK_UP
				|| e.getKeyCode() == KeyEvent.VK_PLUS) {
			mainSleepSpeed /= 2;
			mainSleepSpeed = Math.max(mainSleepSpeed, 1);
		} else if (e.getKeyCode() == KeyEvent.VK_S
				|| e.getKeyCode() == KeyEvent.VK_DOWN
				|| e.getKeyCode() == KeyEvent.VK_MINUS) {
			mainSleepSpeed *= 2;
			mainSleepSpeed = Math.min(mainSleepSpeed, 80);
		}
	}
}
