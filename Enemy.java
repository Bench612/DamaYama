import java.awt.Color;
import java.awt.Graphics;

class Enemy extends MovingGameObjectWeapon {

	Color color;

	double attackRange = 0.6;
	public static final int maxAttackAngle = 180;
	public static final int armAngle = 30;
	int attackTime = -1;
	final static int maxAttackTime = 25;

	protected Enemy(double sheildMulti, int speed, double size, Color c) {
		super(Math.max(speed - BoxHeadPanel.wave, 2), 0.75 * size, 0.75 * size,
				sheildMulti);
		color = c;
		attackRange *= size;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		g.setColor(color);
		super.draw(g, xOff, yOff);
		if (timeSinceHealthChange <= 100)
			drawHealthBar(g, xOff, yOff);
	}

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
		if (timeSinceHealthChange <= 100)
			drawHealthBarSlant(p);
		p.setColor(color);
		p.setOutline(color.darker());
	}

	public void update() {
		timeSinceHealthChange++;
		super.update();
		moving = false;
		if (attackTime == 0) {
			attackTime--;
			attack();
		} else if (attackTime > 0)
			attackTime--;
		else {
			if (!knockedBack) {
				followFocus();
				moving = attackTime < 0;
			} else {
				knockedBack = false;
				if (Math.abs(knockbackSpeedX) > knockbackFriction) {
					if (knockbackSpeedX < 0)
						knockbackSpeedX += knockbackFriction;
					else
						knockbackSpeedX -= knockbackFriction;
					knockedBack = true;
				}
				if (Math.abs(knockbackSpeedY) > knockbackFriction) {
					if (knockbackSpeedY < 0)
						knockbackSpeedY += knockbackFriction;
					else
						knockbackSpeedY -= knockbackFriction;
					knockedBack = true;
				}
				moveDown(knockbackSpeedY);
				moveRight(knockbackSpeedX);
			}
		}
	}

	protected void startAttack(Player p) {
		attackTime = maxAttackTime;
	}

	protected void attack() {
		double attackX = Math.cos(direction) * attackRange + x + width / 2;
		double attackY = Math.sin(direction) * -attackRange + y + height / 2; // negative
		int xI = getXIndex(attackX);
		int yI = getYIndex(attackY);
		for (int i = 0; i < Map.getCurrent().spaces[xI][yI].objects.size(); i++) {
			MovingGameObject object = Map.getCurrent().spaces[xI][yI].objects
					.get(i);
			if (Player.class.isInstance(object)
					&& object.contains(attackX, attackY)) {
				((Player) object).changeHealth(-.5, this);
				changeHealth(0.15, object);
			}
		}
	}

	public void die() {
		super.die();
		makeItem();
		PlayPanel.currentRunning.removeFromGameObjects(this);
	}

	public void makeItem() {
	}

	public void atObstacle(MovingGameObject other) {
		if (Player.class.isInstance(other))
			startAttack((Player) other);
	}

}