import java.awt.*;
import java.awt.event.*;

public class SinglePlayerLobby extends Lobby {
	public SinglePlayerLobby(DamaYama parent) {
		super(parent);
	}

	public void run() {
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("start")) {
			parent.switchTo(new BoxHeadPanel(parent));
		} else {
			super.actionPerformed(e);
		}
	}
}