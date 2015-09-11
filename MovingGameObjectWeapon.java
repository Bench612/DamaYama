import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;


class MovingGameObjectWeapon extends MovingGameObject {

	Weapon weapon;

	public MovingGameObjectWeapon(int speedy, double w, double h, double sheild1) {
		super(speedy, w, h, sheild1);
		bulletEndX = new ArrayList<Double>();
		bulletEndZ = new ArrayList<Double>();
		bulletEndY = new ArrayList<Double>();
		didHit = new ArrayList<Boolean>();
	}

	public int getShootDirectionX() {
		return (int) (directionX * 100);
	}

	public int getShootDirectionY() {
		return (int) (directionY * 100);
	}

	ArrayList<Boolean> didHit;

	protected void tryFireWeapon() {
		if (timeSinceShot > weapon.delay && weapon.canFire()) {
			bulletEndX.clear();
			bulletEndY.clear();
			bulletEndZ.clear();
			didHit.clear();
			double increment = weapon.spread / weapon.bulletsPerShot;
			double angle = Math.atan2(getShootDirectionY(),
					getShootDirectionX()) - weapon.spread / 2;
			for (int i = 0; i < weapon.bulletsPerShot; i++) {
				double shootAngle = (angle - weapon.accuracy / 2)
						+ (DamaYama.random() * weapon.accuracy);
				tryFireWeapon((int) (Math.cos(shootAngle) * 360),
						(int) (Math.sin(shootAngle) * 360));
				angle += increment;
			}
			weapon.loseAmmo();
		}
	}

	private void tryFireWeapon(double slopeX, double slopeY) {
		timeSinceShot = 0;
		if (slopeX != 0) {
			// calcualte the line of fire
			double slope = slopeY / slopeX;
			double yIntercept = (y + height / 2) - (slope * (x + width / 2));

			// first try to find first obstacle
			int increment, incrementY;
			if (slopeX < 0) {
				increment = -1;
			} else {
				increment = 1;
			}
			if (slope > 0)
				incrementY = increment;
			else if (slope < 0)
				incrementY = -increment;
			else
				incrementY = increment;

			int startX = getXIndex(x + width / 2);

			int previousYValue = getYIndex(y + height / 2);

			for (int i = startX + increment; true; i += increment) {
				double doubleYValue = (slope * i) + yIntercept;
				int yValueNew = getYIndex(doubleYValue);
				for (int yValue = previousYValue; ((yValue * incrementY < yValueNew
						* incrementY) || yValue == yValueNew); yValue += incrementY) {
					if (!allowWeapon(i - increment, yValue)) {
						if (slopeX < 0)
							bulletEndX.add((i - increment) + 1.0);
						else
							bulletEndX.add((double) i - increment);
						bulletEndY.add((double) yValue);
						if (Map.getCurrent().inBounds(i - increment, yValue))
							bulletEndZ.add(Map.getCurrent().spaces[i
									- increment][yValue].maxHeight() / 2);
						else
							bulletEndZ.add(z + (DamaYama.random() * 0.1 + 0.4)
									* getHeight());
						didHit.add(false);
						return;
					} else {
						MovingGameObject closest = closestObject(
								Map.getCurrent().spaces[i - increment][yValue],
								slope, yIntercept);
						if (closest != null) {
							shootAt(closest, slopeX, slopeY);
							return;
						}
					}
				}
				previousYValue = yValueNew;
			}
		} else if (slopeY != 0) {
			double constantXDouble = x + width / 2;
			int constantX = getXIndex(constantXDouble);
			int yIncrement;
			if (slopeY < 0)
				yIncrement = -1;
			else
				yIncrement = 1;
			for (int yValue = getYIndex(y + height / 2); true; yValue += yIncrement) {
				if (!allowWeapon(constantX, yValue)) {
					bulletEndX.add(constantXDouble);
					if (slopeY < 0)
						bulletEndY.add(yValue + 1.0);
					else
						bulletEndY.add((double) yValue);
					if (Map.getCurrent().inBounds(constantX, yValue))
						bulletEndZ
								.add(Map.getCurrent().spaces[constantX][yValue]
										.maxHeight() / 2);
					else
						bulletEndZ.add(z + (DamaYama.random() * 0.1 + 0.4)
								* getHeight());
					didHit.add(false);
					break;
				}
				MovingGameObject closest = closestObject(
						Map.getCurrent().spaces[constantX][yValue],
						constantXDouble);
				if (closest != null) {
					shootAt(closest, slopeX, slopeY);
					break;
				}
			}
		}
	}

	int timeSinceShot = 100;
	MovingGameObject shootAt; // mainly used for drawing purposes
	ArrayList<Double> bulletEndX, bulletEndY;
	ArrayList<Double> bulletEndZ;
	public static final double coeffecientOfAccuracy = 0.15;

	public void shootAt(MovingGameObject object, double slopeX, double slopeY) {
		object.changeHealth(-weapon.damage, this);
		double direction1 = Math.atan2(slopeY, slopeX);
		object.knockbackSpeedX += Math.cos(direction1) * weapon.knockback;
		object.knockbackSpeedY += Math.sin(direction1) * weapon.knockback;
		object.knockedBack = true;
		weapon.loseAmmo();
		shootAt = object;
		double bX = ((DamaYama.random() * shootAt.width / 2 + shootAt.x + shootAt.width / 4));
		double bY = ((DamaYama.random() * shootAt.height / 2 + shootAt.y + shootAt.width / 4));
		double bZ = ((DamaYama.random() * shootAt.getHeight() / 2 + shootAt.z));
		bulletEndX.add(bX);
		bulletEndY.add(bY);
		bulletEndZ.add(bZ);
		PlayPanel.currentRunning.effects.add(new SphereEffect(bX, bY, bZ,
				0.1 * Math.pow(weapon.damage, 0.333)));
		didHit.add(true);
	}

	// this is for a vertical line
	public MovingGameObject closestObject(Space s, double x1) {
		double min = -1;
		MovingGameObject returnValue = null;
		for (int i = 0; i < s.objects.size(); i++) {
			if (s.objects.get(i) != this && isFacingBasic(s.objects.get(i))) {
				double test = Math.abs(s.objects.get(i).x
						+ s.objects.get(i).width / 2 - x1)
						- (s.objects.get(i).width / 2);
				double distance = Math.abs(s.objects.get(i).y
						+ s.objects.get(i).height / 2 - y + height / 2);
				if (test <= 0 && (distance <= min || min == -1)) {
					returnValue = s.objects.get(i);
					min = distance;
				}
			}
		}
		return returnValue;
	}

	// this is for a function line
	public MovingGameObject closestObject(Space s, double slope,
			double yIntercept) {
		double min = -1;
		MovingGameObject returnValue = null;
		for (int i = 0; i < s.objects.size(); i++) {
			if (s.objects.get(i) != this && isFacingBasic(s.objects.get(i))) {
				double test = s.objects.get(i)
						.distanceToLine(slope, yIntercept);
				double distance = Perspective.distanceTo(s.objects.get(i).x
						+ s.objects.get(i).width / 2, s.objects.get(i).y
						+ s.objects.get(i).height / 2, x + width / 2, y
						+ height / 2);
				if (test <= 0 && (distance < min || min == -1)) {
					returnValue = s.objects.get(i);
					min = distance;
				}
			}
		}
		return returnValue;
	}

	public boolean allowWeapon(int x1, int y1) {
		return Map.getCurrent().inBounds(x1, y1)
				&& Map.getCurrent().spaces[x1][y1].allowPass(weapon);
	}

	public void draw(Graphics g, int xOff, int yOff) {
		super.draw(g, xOff, yOff);
		if (timeSinceShot < 2 && bulletEndX != null) {
			for (int i = 0; i < bulletEndX.size(); i++) {
				if (didHit.get(i))
					g.setColor(Color.DARK_GRAY);
				else
					g.setColor(Color.LIGHT_GRAY);
				g.drawLine((int) ((x + width / 2) * Space.SIZE + xOff),
						(int) ((y + height / 2) * Space.SIZE) + yOff,
						(int) (bulletEndX.get(i) * Space.SIZE) + xOff,
						(int) (bulletEndY.get(i) * Space.SIZE) + yOff);
			}
		}
	}

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
		if (timeSinceShot < 2) {
			for (int i = 0; i < bulletEndX.size(); i++) {
				if (i < didHit.size()) {
					if (didHit.get(i))
						p.setOutline(Color.BLACK);
					else
						p.setOutline(Color.GRAY);
					p.startNewShape(x + width / 2, y + height / 2, z
							+ getHeight() / 2, 2);
					p.addPoint(p.getScreenPoint(bulletEndX.get(i),
							bulletEndY.get(i), bulletEndZ.get(i)));
					p.drawShape(p.getCurrentShape());
				}
			}
		}
	}

	public boolean isFacing(MovingGameObject object) {
		if (getShootDirectionX() == 0) {
			return sameSign(object.y - y, getShootDirectionY())
					&& Math.abs(object.x - this.x) < (width + object.width) / 2;
		} else {
			double slope = getShootDirectionY() / getShootDirectionX();
			return sameSign(object.x - x, getShootDirectionX())
					&& (sameSign(object.y - y, getShootDirectionY()) || getShootDirectionY() == 0)
					&& object.distanceToLine(slope, (y + height / 2)
							- (slope * (x + width / 2))) < object.width / 2;
		}
	}

	private boolean isFacingBasic(MovingGameObject other) {
		if (getShootDirectionY() != 0)
			if (Math.abs(getShootDirectionY()) > Math.abs(getShootDirectionX()))
				return sameSign(other.y - y, getShootDirectionY());
		return sameSign(other.y - y, directionY)
				&& sameSign(other.x - x, getShootDirectionX());
	}

	private boolean sameSign(double a, double b) {
		return (a <= 0 && b <= 0) || (a >= 0 && b >= 0);
	}

	public void update() {
		timeSinceShot++;
		super.update();
	}
}