import java.awt.Color;
import drawing.*;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

public class MovingGameObject {

	protected double x, y, z, width, height;// the units are NOT in pixels they
	// are
	// in Space.SIZE (to
	// make the arrays easier)
	boolean isFocus;
	double speed;
	double movingX;
	double movingY;
	double direction;// this one is an angle (rad)
	double directionX;
	double directionY;
	double[] squaresX; // each index represents a new potential square
	double[] squaresY;
	Rectangle bounds;// units are in pixels
	boolean moving = true;
	private double health, sheild;

	public double getSheild(MovingGameObject other) {
		return sheild;
	}

	public void explode(double x, double y, double z, double width,
			double height, int explosionRange, double damage) {
		int minX = Math.max(0, getXIndex(x) - explosionRange);
		int maxX = Math.min(Map.getCurrent().spaces.length,
				getXIndex(x + width) + explosionRange);
		int minY = Math.max(0, getYIndex(y) - explosionRange);
		int maxY = Math.min(Map.getCurrent().spaces[0].length, getYIndex(y
				+ height)
				+ explosionRange);
		for (int i = minX; i < maxX; i++)
			for (int b = minY; b < maxY; b++)
				for (int j = 0; j < Map.getCurrent().spaces[i][b].objects
						.size(); j++) {
					MovingGameObject toDamage = Map.getCurrent().spaces[i][b].objects
							.get(j);
					double distance = toDamage.distanceTo(x + width / 2, y
							+ height / 2);
					if (distance < width / 2 + toDamage.width / 2
							+ explosionRange
							&& toDamage.z + toDamage.getHeight() >= z) {
						toDamage.changeHealth(-damage
								* (1 - (distance / explosionRange)), this);
					}
				}
		PlayPanel.currentRunning.effects.add(new CircleEffect(
				((x + width / 2)), ((y + height / 2)), z,
				((width / 2 + explosionRange * 2))));
	}

	Color healthColor = Color.green;

	boolean knockedBack = false;
	public static final double knockbackFriction = 0.05;
	public double knockbackSpeedX, knockbackSpeedY;

	public double getHealth() {
		return health;
	}

	public double changeHealth(double amount, MovingGameObject other) {
		if (other instanceof Player) {
			Player p = (Player) other;
			if (p.misses + p.hits != 0) {
				double acc = (double) p.hits / (p.misses + p.hits);
				if (acc >= .9)
					amount *= 1.1;
				if (acc >= .99)
					amount *= 1.3;
			}
		}
		double returnValue = 0;
		if (health > 0) {
			if (amount > 0)
				health += amount;
			else {
				returnValue = amount / getSheild(other);
				health += returnValue;
			}
			if (health > 1)
				health = 1;
			if (health <= 0 && canLeaveAll()) {
				die();
			}
			int greenComponent = Math.min(255,
					Math.max(0, (int) (health * 255)));
			healthColor = new Color(255 - greenComponent, greenComponent, 0);
			timeSinceHealthChange = 0;
		}
		if (other instanceof Player)
			((Player) other).totalDamage += amount;
		return returnValue;
	}

	int timeSinceHealthChange = 101;

	public void die() {
		health = 0;
		leaveAll();
	}

	protected Color getHitColor(MovingGameObject object) {
		return bloodColor;
	}

	static Color bloodColor = new Color(183, 29, 29, 100);

	public MovingGameObject(int speedy, double w, double h, double sheild1) {
		speed = 1.0 / speedy;
		width = w;
		height = h;
		squaresX = new double[(int) Math.ceil(width) + 1];
		squaresY = new double[(int) Math.ceil(height) + 1];
		bounds = new Rectangle(0, 0, (int) (width * Space.SIZE),
				(int) (height * Space.SIZE));
		sheild = sheild1;
		health = 1;
	}

	public boolean spawn() {
		return Map.getCurrent().spawn(this);
	}

	public boolean spawn(int x1, int y1) {
		x = x1;
		y = y1;
		updateXSquares();
		updateYSquares();
		if (canEnterAll(x, y, true)) {
			enterAll(x, y);
			return true;
		}
		return false;
	}

	public void update() {
		movingX = 0;
		movingY = 0;
		performAction();
		zVelocity += zAcceleration;
		z += zVelocity;
		checkDrop();
	}

	public static final double jumpVelocity = 0.0775;

	protected void jump() {
		if (onGround()) {
			zVelocity = jumpVelocity;
			z += zVelocity;
		}
	}

	public boolean onGround() {
		return zMin >= z;
	}

	public void addAsFocus() {
		Map.getCurrent().addFocus(x + width / 2, y + height / 2);
	}

	public Space currentSpace() {
		return Map.getCurrent().spaces[getXIndex(x + width / 2)][getYIndex(y
				+ height / 2)];
	}

	Space moveTorward;
	double waitingX, waitingY;
	Player follow;

	public void followFocus() {
		if (PlayPanel.currentRunning.players.size() == 0)
			return;
		Space currentSp = currentSpace();
		if (currentSp == moveTorward || moveTorward == null || stuck) {
			moveTorward = null;
			Space bestSpace = currentSpace();
			// check all adjacentSpaces
			for (int i = -1; i <= 1; i++)
				for (int j = -1; j <= 1; j++) {
					if (Map.getCurrent().inBounds(currentSp.x + i,
							currentSp.y + j)) {
						Space s = Map.getCurrent().spaces[getXIndex(currentSp.x
								+ i)][getYIndex(currentSp.y + j)];
						if (s.steps < bestSpace.steps && s.steps >= 0
								&& currentSp.maxHeight() + 0.5 >= s.maxHeight())
							bestSpace = s;
					}
				}
			if (bestSpace.steps > 0) {
				moveTorward = bestSpace;
				moveTorward(moveTorward);
			} else {
				if (bestSpace.steps == 0) {
					for (int i = 0; i < bestSpace.objects.size(); i++) {
						if (bestSpace.objects.get(i) instanceof Player) {
							follow = (Player) bestSpace.objects.get(i);
							break;
						}
					}
				}
				// only use regular follow if there is no other path
				if (follow == null) {
					boolean nonGhostExists = false;
					for (Player p : PlayPanel.currentRunning.players) {
						if (!(p instanceof Ghost)) {
							nonGhostExists = true;
							break;
						}
					}
					if (nonGhostExists) {
						while (follow instanceof Ghost || follow == null) {
							follow = PlayPanel.currentRunning.players
									.get((int) (DamaYama.random() * PlayPanel.currentRunning.players
											.size()));
						}
					}
				}
				moveTorward(follow.x + follow.width / 2, follow.y
						+ follow.height / 2, follow.z);
			}
		} else
			moveTorward(moveTorward);
	}

	public void atObstacle(MovingGameObject obstacle) {

	}// to ovveride with subclasses

	// tries to move torward a point and moves out of the way if it can't (to
	// allow others through)
	public void moveTorward(Space s) {
		moveTorward(s.x + 0.5, s.y + 0.5, s.maxHeight());
		moveTorward = s;
	}

	boolean stuck = false;

	public void moveTorward(double x1, double y1, double z1) {
		if (z1 > z)
			this.jump();
		intersection = null;
		stuck = true;
		if (x1 < x + width / 2) { // moveLeft
			if (moveRight(Math.max(x1 - (x + width / 2), -speed)))
				stuck = false;
		} else if (x1 > x + width / 2) // moveRight
			if (moveRight(Math.min(x1 - (x + width / 2), speed)))
				stuck = false;

		if (y1 < y + height / 2) { // move up
			if (moveDown(Math.max(y1 - (y + height / 2), -speed)))
				stuck = false;
		} else if (y1 > y + height / 2) // move down
			if (moveDown(Math.min(y1 - (y + height / 2), speed)))
				stuck = false;

		if (intersection instanceof Player)
			atObstacle(intersection);
	}

	public void updateXSquares() {
		squaresX[squaresX.length - 1] = x + width;
		for (int i = 0; i < squaresX.length - 1; i++)
			squaresX[i] = x + i;
		bounds.setLocation((int) (x * Space.SIZE), (int) (y * Space.SIZE));
	}

	public void updateYSquares() {
		squaresY[squaresY.length - 1] = y + height;
		for (int i = 0; i < squaresY.length - 1; i++)
			squaresY[i] = y + i;
		bounds.setLocation((int) (x * Space.SIZE), (int) (y * Space.SIZE));
	}

	public boolean moveUp() {
		return moveDown(-speed);
	}

	public boolean moveDown() {
		return moveDown(speed);
	}

	public boolean intersectsRectangle(MovingGameObject other, double endX,
			double endY) {
		if (other == this)
			return false;
		bounds.setLocation((int) (endX * Space.SIZE), (int) (endY * Space.SIZE));
		if (bounds.intersects(other.bounds)
				|| other.z + other.getHeight() + other.z < z
				|| z < other.getHeight() + other.z) {
			return true;
		} else {
			bounds.setLocation((int) (x * Space.SIZE), (int) (y * Space.SIZE));
			return false;
		}
	}

	public int getPointCount() {
		return 0;
	}

	public double distanceTo(double endX, double endY) {
		return Perspective
				.distanceTo(x + width / 2, y + height / 2, endX, endY);
	}

	public double distanceTo(MovingGameObject o) {
		return Perspective.distanceTo(x + width / 2, y + height / 2, o.x
				+ o.width / 2, o.y + o.height / 2);
	}

	public double distanceTo(Space s) {
		return distanceTo(s.x + 0.5, s.y + 0.5);
	}

	public boolean intersectsCircle(MovingGameObject other, double endX,
			double endY) {
		if (other == this || other == null)
			return false;
		return Perspective.distanceTo(endX + width / 2, endY + height / 2,
				other.x + other.width / 2, other.y + other.height / 2) < Math
				.min(width, height)
				/ 2
				+ Math.min(other.width, other.height)
				/ 2;
	}

	MovingGameObject intersection;

	public boolean intersectsWithAnyObject(double endX, double endY) {
		double xOff = endX - x;
		double yOff = endY - y;
		for (int i = 0; i < squaresX.length; i++)
			for (int b = 0; b < squaresY.length; b++) {
				int testX = getXIndex(squaresX[i] + xOff);
				int testY = getYIndex(squaresY[b] + yOff);
				if (Map.getCurrent().inBounds(testX, testY))
					for (int z = 0; z < Map.getCurrent().spaces[testX][testY].objects
							.size(); z++)
						if (intersectsCircle(
								Map.getCurrent().spaces[testX][testY].objects
										.get(z),
								endX, endY)) {
							intersection = Map.getCurrent().spaces[testX][testY].objects
									.get(z);
							return true;
						}
			}
		return false;
	}

	protected boolean moveDown(double amount) {
		movingY = amount;
		updateDirection();
		double y2 = y + amount;
		boolean changedIndex = getYIndex(y) == getYIndex(y2)
				&& getYIndex(squaresY[squaresY.length - 1]) == getYIndex(squaresY[squaresY.length - 1]
						+ amount);
		if ((changedIndex && !intersectsWithAnyObject(x, y2))
				|| (!changedIndex && trySwitchSquare(x, y2, false))) {
			y = y2;
			updateYSquares();
			moving = true;
			return true;
		}
		return false;
	}

	public void performAction() {
		for (int i = 0; i < squaresX.length; i++)
			for (int b = 0; b < squaresY.length; b++)
				Map.getCurrent().spaces[getXIndex(squaresX[i])][getYIndex(squaresY[b])]
						.performAction(this);
	}

	public boolean moveRight() {
		return moveRight(speed);
	}

	public boolean moveLeft() {
		return moveRight(-speed);
	}

	private void updateDirection() {
		direction = Math.atan2(-movingY, movingX);
		directionX = movingX;
		directionY = movingY;
	}

	protected boolean moveRight(double amount) {
		movingX = amount;
		updateDirection();
		double x2 = x + amount;
		boolean changedIndex = getXIndex(x) == getXIndex(x2)
				&& getXIndex(squaresX[squaresX.length - 1]) == getXIndex(squaresX[squaresX.length - 1]
						+ amount);
		if ((changedIndex && !intersectsWithAnyObject(x2, y))
				|| (!changedIndex && trySwitchSquare(x2, y, false))) {
			x = x2;
			updateXSquares();
			moving = true;
			return true;
		}
		return false;
	}

	public boolean trySwitchSquare(double endX, double endY,
			boolean ignoreHeight) {
		if (canLeaveAll() && canEnterAll(endX, endY, ignoreHeight)) {
			leaveAll();
			enterAll(endX, endY);
			return true;
		}
		return false;
	}

	private void enter(int x1, int y1) {
		Map.getCurrent().spaces[x1][y1].enter(this);
	}

	double zVelocity = 0;
	static final double zAcceleration = -0.006444;
	double zMin = 0;

	private void enterAll(double endX, double endY) {
		double offsetX = endX - x;
		double offsetY = endY - y;
		zMin = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < squaresX.length; i++)
			for (int b = 0; b < squaresY.length; b++) {
				int xI = getXIndex(squaresX[i] + offsetX);
				int yI = getYIndex(squaresY[b] + offsetY);
				enter(xI, yI);
				zMin = Math.max(Map.getCurrent().spaces[xI][yI].maxHeight(),
						zMin);
			}
		checkDrop();
	}

	private void checkDrop() {
		if (z < zMin) {
			z = zMin;
			zVelocity = 0;
		}
	}

	private boolean canEnterAll(double endX, double endY, boolean ignoreHeight) {
		double offsetX = endX - x;
		double offsetY = endY - y;
		if (intersectsWithAnyObject(endX, endY))
			return false;
		for (int i = 0; i < squaresX.length; i++)
			for (int b = 0; b < squaresY.length; b++)
				if (!canEnter(getXIndex(squaresX[i] + offsetX),
						getYIndex(squaresY[b] + offsetY), ignoreHeight))
					return false;
		return true;
	}

	private boolean canLeave(int x1, int y1) {
		return Map.getCurrent().spaces[x1][y1].canLeave(this);
	}

	private void leave(int x1, int y1) {
		Map.getCurrent().spaces[x1][y1].leave(this);
	}

	public static final double healthHeight = 0.1;

	protected void drawHealthBar(Graphics g, int xOff, int yOff) {
		g.setColor(healthColor);
		g.fillRect((int) (x * Space.SIZE + xOff),
				(int) ((y - healthHeight) * Space.SIZE) + yOff, (int) (width
						* Space.SIZE * health),
				(int) (healthHeight * Space.SIZE));
	}

	protected void drawHealthBarSlant(Perspective p) {
		p.setColor(healthColor);
		p.setOutline(healthColor);
		double barMaxWidth = width * 1.1;
		double barWidth = barMaxWidth * health;
		p.startNewConvexPolygon(6);
		p.startNewShape(x + (width - barMaxWidth) / 2 + 0.05, y + width / 2
				+ 0.05, z + getHeight() + 0.2, 4);
		p.drawYLine(-0.1);
		p.drawXLine(barWidth);
		p.drawYLine(0.1);
		p.drawXLine(-barWidth);
		ArrayList<Point> top = p.getCurrentShape();
		p.startNewShape(x + (width - barMaxWidth) / 2 + 0.05, y + width / 2
				+ 0.05, z + getHeight() + 0.1, 4);
		p.drawYLine(-0.1);
		p.drawXLine(barWidth);
		p.drawYLine(0.1);
		p.drawXLine(-barWidth);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
		p.finishConvexPolygon();
	}

	protected boolean canLeaveAll() {
		for (int i = 0; i < squaresX.length; i++)
			for (int b = 0; b < squaresY.length; b++)
				if (!canLeave(getXIndex(squaresX[i]), getYIndex(squaresY[i])))
					return false;
		return true;
	}

	protected boolean leaveAll() {
		for (int i = 0; i < squaresX.length; i++)
			for (int b = 0; b < squaresY.length; b++)
				leave(getXIndex(squaresX[i]), getYIndex(squaresY[b]));
		moveTorward = null;
		return true;
	}

	public boolean canEnter(int endX, int endY, boolean ignoreHeight) {
		return Map.getCurrent().inBounds(endX, endY)
				&& Map.getCurrent().spaces[endX][endY].canEnter(this,
						ignoreHeight);
	}

	protected static int convertToIndex(double x) {
		return (int) Math.floor(x);// the floor part is neccesary for negative
		// #s
	}

	// this assumes a circular shape
	public double distanceToLine(double slope, double yIntercept) {
		return Perspective.distanceToLine(x + width / 2, y + height / 2, slope,
				yIntercept) - width / 2;
	}

	public static int getXIndex(double x2) {
		return convertToIndex(x2);
	}

	public static int getYIndex(double y2) {
		return convertToIndex(y2);
	}

	public boolean contains(double endX, double endY) {
		return distanceTo(endX, endY) < (width + height) / 2;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		g.fillOval((int) (x * Space.SIZE) + xOff,
				(int) (y * Space.SIZE) + yOff, (int) (width * Space.SIZE),
				(int) (height * Space.SIZE));

		/*
		 * if (moveTorward != null) { if (upDownLock) g.setColor(Color.red); if
		 * (leftRightLock) g.setColor(Color.green); g.drawLine((int) ((x + width
		 * / 2) * Space.SIZE) + xOff, (int) ((y + width / 2) * Space.SIZE + 0.5)
		 * + yOff, (int) ((moveTorward.x + 0.5) * Space.SIZE + xOff), (int)
		 * ((moveTorward.y + 0.5) * Space.SIZE + yOff + 0.5)); }
		 */

	}

	public void drawSlant(Perspective p) {
	}

	public double getHeight() {
		return width * 2;
	}
}