import java.awt.*;
import java.awt.event.*;

public class SinglePlayerLobby extends Lobby {
	public void run() {
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("start")) {
			DamaYama.switchTo(new BoxHeadPanel());
		} else {
			super.actionPerformed(e);
		}
	}
}