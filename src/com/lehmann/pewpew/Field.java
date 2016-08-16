package com.lehmann.pewpew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import android.content.Context;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.lehmann.pewpew.box2d.AgrupamentoElementos;
import com.lehmann.pewpew.box2d.Alavanca;
import com.lehmann.pewpew.box2d.BarreiraForca;
import com.lehmann.pewpew.box2d.BasicElement;
import com.lehmann.pewpew.box2d.Box2DFactory;
import com.lehmann.pewpew.box2d.TrilhaElementos;

/**
 * Representa a mesa de jogo corrente. Conhece todos os elementos do jogo,
 * conhece o mundo do Box2D onde ocorre a física, possui um agendador de
 * execução de eventos e mantém o estado atual do jogo.
 * 
 * @author limao
 * 
 */
public class Field implements ContactListener {

	public static interface Delegate {
		void allDropTargetsInGroupHit(Field field,
				AgrupamentoElementos targetGroup);

		void allRolloversInGroupActivated(Field field,
				TrilhaElementos rolloverGroup);

		void ballInSensorRange(Field field, BarreiraForca sensor,
				Body ball);

		void ballLost(Field field);

		void flippersActivated(Field field, List<Alavanca> flippers);

		void gameEnded(Field field);

		void gameStarted(Field field);

		boolean isFieldActive(Field field);

		void processCollision(Field field, BasicElement element,
				Body hitBody, Body ball);

		void tick(Field field, long nanos);
	}

	private static class ScheduledAction implements Comparable<ScheduledAction> {
		Long actionTime;
		Runnable action;

		@Override
		public int compareTo(final ScheduledAction another) {
			return this.actionTime.compareTo(another.actionTime);
		}
	}

	private FieldLayout layout;
	private World world;
	private Set<Body> layoutBodies;

	private List<Body> balls;
	private Map<Body, BasicElement> bodyToFieldElement;
	private Map<String, BasicElement> fieldElementsByID;

	private BasicElement[] fieldElementsArray;

	private BasicElement[] fieldElementsToTick;

	private long gameTime;

	private PriorityQueue<ScheduledAction> scheduledActions;
	private Delegate delegate;

	private final GameState state = new GameState();

	private GameMessage message;

	private ArrayList<Body> deadBalls = new ArrayList<Body>();

	private final ArrayList<Alavanca> activatedFlips = new ArrayList<Alavanca>();

	private final Map<Body, List<Fixture>> ballContacts = new HashMap<Body, List<Fixture>>();

	public void addScore(final long s) {
		this.state.addScore(s);
	}

	@Override
	public void beginContact(final Contact contact) {
		// nothing to do here
	}

	public void doBallLost() {
		final boolean hasExtraBall = this.state.getExtraBalls() > 0;
		this.state.doNextBall();
		String msg = null;
		if (hasExtraBall) {
			msg = "Lance novamente";
		} else if (this.state.isGameInProgress()) {
			msg = "Bola " + this.state.getBallNumber();
		}

		if (msg != null) {
			final String msg2 = msg;
			this.scheduleAction(1500, new Runnable() {
				@Override
				public void run() {
					Field.this.showGameMessage(msg2, 1500, false);
				}
			});
		} else {
			this.endGame();
		}

		this.getDelegate().ballLost(this);
	}

	public void drawBalls(final IFieldRenderer renderer) {
		final List<Integer> color = this.layout.getBallColor();
		for (int i = 0; i < this.balls.size(); i++) {
			final Body ball = this.balls.get(i);
			final CircleShape shape = (CircleShape) ball.getFixtureList()
					.get(0).getShape();
			renderer.fillCircle(ball.getPosition().x, ball.getPosition().y,
					shape.getRadius(), color.get(0), color.get(1), color.get(2));
		}
	}

	@Override
	public void endContact(final Contact contact) {
		Body ball = null;
		Fixture fixture = null;
		if (this.balls.contains(contact.getFixtureA().getBody())) {
			ball = contact.getFixtureA().getBody();
			fixture = contact.getFixtureB();
		}
		if (this.balls.contains(contact.getFixtureB().getBody())) {
			ball = contact.getFixtureB().getBody();
			fixture = contact.getFixtureA();
		}
		if (ball != null) {
			List<Fixture> fixtures = this.ballContacts.get(ball);
			if (fixtures == null) {
				this.ballContacts
						.put(ball, fixtures = new ArrayList<Fixture>());
			}
			fixtures.add(fixture);
		}
	}

	public void endGame() {
		SoundsHelper.playStart();
		for (final Body ball : this.getBalls()) {
			this.world.destroyBody(ball);
		}
		this.balls.clear();
		this.getGameState().setGameInProgress(false);
		this.showGameMessage("Fim de jogo", 2500);
		this.getDelegate().gameEnded(this);
	}

	public List<Body> getBalls() {
		return this.balls;
	}

	public World getBox2DWorld() {
		return this.world;
	}

	public Delegate getDelegate() {
		return this.delegate;
	}

	public BasicElement getFieldElementByID(final String elementID) {
		return this.fieldElementsByID.get(elementID);
	}

	public List<BasicElement> getFieldElements() {
		return this.layout.getFieldElements();
	}

	public BasicElement[] getFieldElementsArray() {
		return this.fieldElementsArray;
	}

	public List<Alavanca> getFlipperElements() {
		return this.layout.getFlipperElements();
	}

	public GameMessage getGameMessage() {
		return this.message;
	}

	public GameState getGameState() {
		return this.state;
	}

	public long getGameTime() {
		return this.gameTime;
	}

	public float getHeight() {
		return this.layout.getHeight();
	}

	public Set<Body> getLayoutBodies() {
		return this.layoutBodies;
	}

	public float getTargetTimeRatio() {
		return this.layout.getTargetTimeRatio();
	}

	public Object getValueWithKey(final String key) {
		return this.layout.getValueWithKey(key);
	}

	public float getWidth() {
		return this.layout.getWidth();
	}

	public void handleDeadBalls() {
		final List<Number> deadRect = this.layout.getLaunchDeadZone();
		if (deadRect == null) {
			return;
		}

		for (int i = 0; i < this.balls.size(); i++) {
			final Body ball = this.balls.get(i);
			final Vector2 bpos = ball.getPosition();
			if (bpos.x > deadRect.get(0).floatValue()
					&& bpos.y > deadRect.get(1).floatValue()
					&& bpos.x < deadRect.get(2).floatValue()
					&& bpos.y < deadRect.get(3).floatValue()) {
				this.deadBalls.add(ball);
				this.world.destroyBody(ball);
			}
		}

		for (int i = 0; i < this.deadBalls.size(); i++) {
			this.balls.remove(this.deadBalls.get(i));
		}

		if (this.deadBalls.size() > 0) {
			this.launchBall();
			this.deadBalls.clear();
		}
	}

	public boolean hasActiveElements() {
		if (this.gameTime < 500) {
			return true;
		}
		if (this.getDelegate().isFieldActive(this)) {
			return true;
		}
		return this.getBalls().size() > 0;
	}

	public Body launchBall() {
		final List<Number> position = getLaunchPosition();
		final List<Float> velocity = this.layout.getLaunchVelocity();
		final float radius = this.layout.getBallRadius();

		final Body ball = Box2DFactory.createCirculo(this.world, position
				.get(0).floatValue(), position.get(1).floatValue(), radius,
				false);
		ball.setBullet(true);
		ball.setLinearVelocity(new Vector2(velocity.get(0), velocity.get(1)));
		this.balls.add(ball);
		SoundsHelper.playBall();

		return ball;
	}

	public List<Number> getLaunchPosition() {
		return this.layout.getLaunchPosition();
	}
	
	public void receivedOrientationValues(final float azimuth,
			final float pitch, final float roll) {
		final double angle = roll - Math.PI / 2;
		final float gravity = this.layout.getGravity();
		final float gx = (float) (gravity * Math.cos(angle));
		final float gy = -Math.abs((float) (gravity * Math.sin(angle)));
		this.world.setGravity(new Vector2(gx, gy));
	}

	public void removeBall(final Body ball) {
		this.world.destroyBody(ball);
		this.balls.remove(ball);
		if (this.balls.size() == 0) {
			this.doBallLost();
		}
	}

	public void removeBallWithoutBallLoss(final Body ball) {
		this.world.destroyBody(ball);
		this.balls.remove(ball);
	}

	public void resetForLevel(final Context context, final int level) {
		final Vector2 gravity = new Vector2(0.0f, -1.0f);
		final boolean doSleep = true;
		this.world = new World(gravity, doSleep);
		this.world.setContactListener(this);

		this.layout = FieldLayout.layoutForLevel(level, this.world);
		this.world.setGravity(new Vector2(0.0f, -this.layout.getGravity()));
		this.balls = new ArrayList<Body>();

		this.scheduledActions = new PriorityQueue<ScheduledAction>();
		this.gameTime = 0;

		this.bodyToFieldElement = new HashMap<Body, BasicElement>();
		this.fieldElementsByID = new HashMap<String, BasicElement>();
		final List<BasicElement> tickElements = new ArrayList<BasicElement>();

		for (final BasicElement element : this.layout.getFieldElements()) {
			if (element.getElementID() != null) {
				this.fieldElementsByID.put(element.getElementID(), element);
			}
			for (final Body body : element.getBodies()) {
				this.bodyToFieldElement.put(body, element);
			}
			if (element.shouldCallTick()) {
				tickElements.add(element);
			}
		}
		this.fieldElementsToTick = tickElements.toArray(new BasicElement[0]);
		this.fieldElementsArray = this.layout.getFieldElements().toArray(
				new BasicElement[0]);

		this.delegate = null;
		String delegateClass = this.layout.getDelegateClassName();
		if (delegateClass != null) {
			if (!delegateClass.contains(".")) {
				delegateClass = "com.lehmann.pewpew.boards." + delegateClass;
			}
			try {
				this.delegate = (Delegate) Class.forName(delegateClass)
						.newInstance();
			} catch (final Exception ex) {
				throw new RuntimeException(ex);
			}
		} else {
			this.delegate = new BaseFieldDelegate();
		}
	}

	public void scheduleAction(final long interval, final Runnable action) {
		final ScheduledAction sa = new ScheduledAction();
		sa.actionTime = this.gameTime + interval * 1000000;
		sa.action = action;
		this.scheduledActions.add(sa);
	}

	public void setFlippersEngaged(final List<Alavanca> flips,
			final boolean engaged) {
		this.activatedFlips.clear();
		boolean allFlipsPreviouslyActive = true;
		final int fsize = flips.size();
		for (int i = 0; i < fsize; i++) {
			final Alavanca flip = flips.get(i);
			if (!flip.isFlipperEngaged()) {
				allFlipsPreviouslyActive = false;
				if (engaged) {
					this.activatedFlips.add(flip);
				}
			}
			flip.setFlipperEngaged(engaged);
		}

		if (engaged && !allFlipsPreviouslyActive) {
			SoundsHelper.playFlipper();
			for (final BasicElement element : this.getFieldElementsArray()) {
				element.flipsActivated(this, this.activatedFlips);
			}
			this.getDelegate().flippersActivated(this, this.activatedFlips);
		}
	}

	public void setLeftFlippersEngaged(final boolean engaged) {
		this.setFlippersEngaged(this.layout.getLeftFlipperElements(), engaged);
	}

	public void setRightFlippersEngaged(final boolean engaged) {
		this.setFlippersEngaged(this.layout.getRightFlipperElements(), engaged);
	}

	public void showGameMessage(final String text, final long duration) {
		this.showGameMessage(text, duration, true);
	}

	public void showGameMessage(final String text, final long duration,
			final boolean playSound) {
		if (playSound) {
			SoundsHelper.playMessage();
		}
		this.message = new GameMessage();
		this.message.text = text;
		this.message.duration = duration;
		this.message.creationTime = System.currentTimeMillis();
	}

	public void startGame() {
		this.state.setTotalBalls(this.layout.getNumberOfBalls());
		this.state.startNewGame();
		this.getDelegate().gameStarted(this);
	}

	void clearBallContacts() {
		this.ballContacts.clear();
	}

	void processBallContacts() {
		if (this.ballContacts.size() == 0) {
			return;
		}
		for (final Body ball : this.ballContacts.keySet()) {
			final List<Fixture> fixtures = this.ballContacts.get(ball);
			for (int i = 0; i < fixtures.size(); i++) {
				final Fixture f = fixtures.get(i);
				final BasicElement element = this.bodyToFieldElement.get(f
						.getBody());
				if (element != null) {
					element.handleCollision(ball, f.getBody(), this);
					if (this.delegate != null) {
						this.delegate.processCollision(this, element,
								f.getBody(), ball);
					}
					if (element.getScore() != 0) {
						this.state.addScore(element.getScore());
						SoundsHelper.playScore();
					}
				}
			}
		}
	}

	void processElementTicks() {
		final int size = this.fieldElementsToTick.length;
		for (int i = 0; i < size; i++) {
			this.fieldElementsToTick[i].tick(this);
		}
	}

	void processGameMessages() {
		if (this.message != null) {
			if (System.currentTimeMillis() - this.message.creationTime > this.message.duration) {
				this.message = null;
			}
		}
	}

	void processScheduledActions() {
		while (true) {
			final ScheduledAction nextAction = this.scheduledActions.peek();
			if (nextAction != null && this.gameTime >= nextAction.actionTime) {
				this.scheduledActions.poll();
				nextAction.action.run();
			} else {
				break;
			}
		}
	}

	void tick(final long nanos, final int iters) {
		final float dt = (float) (nanos / 1000000000.0 / iters);

		for (int i = 0; i < iters; i++) {
			this.clearBallContacts();
			this.world.step(dt, 10, 10);
			this.processBallContacts();
		}

		this.gameTime += nanos;
		this.processElementTicks();
		this.processScheduledActions();
		this.processGameMessages();

		this.getDelegate().tick(this, nanos);
	}

	public void setGameAchievements(GameAchievements gameAchievements) {
		this.state.setGameAchievements(gameAchievements);
	}
}
