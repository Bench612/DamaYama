import java.awt.Color;
import drawing.*;
import java.awt.Graphics;
import java.util.ArrayList;

public class EmptySpace extends NonSolidSpace {
	public EmptySpace(int x, int y) {
		super(x, y);
	}

	public EmptySpace(int x, int y, int z) {
		super(x, y, z);
	}

	public String getDescription() {
		return "A space where a player can walk";
	}

	public void draw(Graphics g, int x, int y) {
		g.setColor(getColor());
		g.fillRect(x, y, SIZE, SIZE);
		super.draw(g, x, y);
	}

	public String toString() {
		return " ";
	}

	public void drawSlant(Perspective p) {
		p.setColor(getColor());
		super.drawSlant(p);
	}

	public static final Color defaultColor = new Color(230, 240, 230);

	public Color getColor() {
		return defaultColor;
	}

	public Space createCopy(int x, int y, int z) {
		return new EmptySpace(x, y, 0);
	}

	public boolean allowPass(Weapon w) {
		return true;
	}
}
