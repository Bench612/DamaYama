import java.awt.Graphics;

class Effect {
	int timeLeft;

	public Effect(int timeLimit) {
		timeLeft = timeLimit;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		timeLeft--;
		if (timeLeft <= 0)
			PlayPanel.currentRunning.effects.remove(this);
	}
	public void drawSlant(Perspective p){
		timeLeft--;
		if (timeLeft <= 0)
			PlayPanel.currentRunning.effects.remove(this);
	}
}