package com.lehmann.pewpew.box2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.lehmann.pewpew.Field;
import com.lehmann.pewpew.IFieldRenderer;
import com.lehmann.pewpew.SoundsHelper;
import com.lehmann.pewpew.util.MathUtility;

public class TrilhaElementos extends BasicElement {

	static class ElementoTrilha {
		float cx, cy;
		float radius;
		float radiusSquared;
		List<Integer> color;
		long score;
		float resetDelay;
	}

	boolean cycleOnFlip;
	boolean canToggleOff;
	boolean ignoreBall;
	float defaultRadius;
	float defaultResetDelay;
	List<ElementoTrilha> rollovers = new ArrayList<ElementoTrilha>();
	List<ElementoTrilha> activeRollovers = new ArrayList<ElementoTrilha>();
	List<ElementoTrilha> rolloversHitOnPreviousTick = new ArrayList<ElementoTrilha>();

	List<ElementoTrilha> hitRollovers = new ArrayList<ElementoTrilha>();

	List<ElementoTrilha> newActiveRollovers = new ArrayList<ElementoTrilha>();

	public void activateFirstUnactivatedRollover() {
		final int rsize = this.rollovers.size();
		for (int i = 0; i < rsize; i++) {
			final ElementoTrilha rollover = this.rollovers.get(i);
			if (!this.activeRollovers.contains(rollover)) {
				this.activeRollovers.add(rollover);
				break;
			}
		}
	}

	public boolean allRolloversActive() {
		return this.activeRollovers.size() == this.rollovers.size();
	}

	public void cycleRollovers(final boolean toRight) {
		this.newActiveRollovers.clear();
		for (int i = 0; i < this.rollovers.size(); i++) {
			final int prevIndex = toRight ? i == 0 ? this.rollovers.size() - 1
					: i - 1 : i == this.rollovers.size() - 1 ? 0 : i + 1;
			if (this.activeRollovers.contains(this.rollovers.get(prevIndex))) {
				this.newActiveRollovers.add(this.rollovers.get(i));
			}
		}

		this.activeRollovers.clear();
		for (int i = 0; i < this.newActiveRollovers.size(); i++) {
			this.activeRollovers.add(this.newActiveRollovers.get(i));
		}
	}

	@Override
	public void draw(final IFieldRenderer renderer) {
		final int defaultRed = this.redColorComponent(0);
		final int defaultGreen = this.greenColorComponent(255);
		final int defaultBlue = this.blueColorComponent(0);

		final int rsize = this.rollovers.size();
		for (int i = 0; i < rsize; i++) {
			final ElementoTrilha rollover = this.rollovers.get(i);
			final int red = rollover.color != null ? rollover.color.get(0)
					: defaultRed;
			final int green = rollover.color != null ? rollover.color.get(1)
					: defaultGreen;
			final int blue = rollover.color != null ? rollover.color.get(2)
					: defaultBlue;

			if (this.activeRollovers.contains(rollover)) {
				renderer.fillCircle(rollover.cx, rollover.cy, rollover.radius,
						red, green, blue);
			} else {
				renderer.frameCircle(rollover.cx, rollover.cy, rollover.radius,
						red, green, blue);
			}
		}

	}

	@Override
	public void finishCreate(final Map<String, Object> params, final World world) {
		this.canToggleOff = Boolean.TRUE.equals(params.get("toggleOff"));
		this.cycleOnFlip = Boolean.TRUE.equals(params.get("cycleOnFlipper"));
		this.ignoreBall = Boolean.TRUE.equals(params.get("ignoreBall"));
		this.defaultRadius = MathUtility.asFloat(params.get("radius"));
		this.defaultResetDelay = MathUtility.asFloat(params.get("reset"));

		final List<Map<String, Object>> rolloverMaps = (List<Map<String, Object>>) params
				.get("rollovers");
		for (final Map<String, Object> rmap : rolloverMaps) {
			final ElementoTrilha rollover = new ElementoTrilha();
			this.rollovers.add(rollover);

			final List pos = (List) rmap.get("position");
			rollover.cx = MathUtility.asFloat(pos.get(0));
			rollover.cy = MathUtility.asFloat(pos.get(1));
			// raio, cor, pontuação e tempo de delay pode ser definido para
			// cada elemento  caso contrário, usa o default
			rollover.radius = rmap.containsKey("radius") ? MathUtility
					.asFloat(rmap.get("radius")) : this.defaultRadius;
			rollover.color = (List<Integer>) rmap.get("color");
			rollover.score = rmap.containsKey("score") ? ((Number) rmap
					.get("score")).longValue() : this.score;
			rollover.resetDelay = rmap.containsKey("reset") ? MathUtility
					.asFloat(rmap.get("reset")) : this.defaultResetDelay;

			rollover.radiusSquared = rollover.radius * rollover.radius;
		}
	}

	@Override
	public void flipsActivated(final Field field,
			final List<Alavanca> flippers) {
		if (this.cycleOnFlip) {
			boolean hasRightFlipper = false;
			for (int i = 0; !hasRightFlipper && i < flippers.size(); i++) {
				hasRightFlipper = flippers.get(i).isRightFlipper();
			}
			this.cycleRollovers(hasRightFlipper);
		}
	}

	@Override
	public List<Body> getBodies() {
		return Collections.emptyList();
	}

	public boolean isRolloverActiveAtIndex(final int index) {
		return this.activeRollovers.contains(this.rollovers.get(index));
	}

	public int numberOfRollovers() {
		return this.rollovers.size();
	}

	public void setAllRolloversActivated(final boolean active) {
		this.activeRollovers.clear();
		if (active) {
			this.activeRollovers.addAll(this.rollovers);
		}
	}

	public void setRolloverActiveAtIndex(final int index, final boolean active) {
		final ElementoTrilha r = this.rollovers.get(index);
		if (active) {
			if (!this.activeRollovers.contains(r)) {
				this.activeRollovers.add(r);
			}
		} else {
			this.activeRollovers.remove(r);
		}
	}

	@Override
	public boolean shouldCallTick() {
		return true;
	}

	@Override
	public void tick(final Field field) {
		if (this.ignoreBall) {
			return;
		}

		final boolean allActivePrevious = this.allRolloversActive();
		final List<ElementoTrilha> hitRollovers = this
				.rolloversHitByBalls(field.getBalls());
		for (final ElementoTrilha rollover : hitRollovers) {
			if (this.rolloversHitOnPreviousTick.contains(rollover)) {
				continue;
			}
			if (!this.activeRollovers.contains(rollover)) {
				this.activeRollovers.add(rollover);
				field.addScore(rollover.score);
				SoundsHelper.playRollover();
				if (rollover.resetDelay > 0) {
					field.scheduleAction((long) (rollover.resetDelay * 1000),
							new Runnable() {
								@Override
								public void run() {
									TrilhaElementos.this.activeRollovers
											.remove(rollover);
								}
							});
				}
			} else if (this.canToggleOff) {
				this.activeRollovers.remove(rollover);
				field.addScore(rollover.score);
				SoundsHelper.playRollover();
			}
		}

		this.rolloversHitOnPreviousTick.clear();
		for (int i = 0; i < hitRollovers.size(); i++) {
			this.rolloversHitOnPreviousTick.add(hitRollovers.get(i));
		}

		if (!allActivePrevious && this.allRolloversActive()) {
			field.getDelegate().allRolloversInGroupActivated(field, this);
		}
	}

	protected List<ElementoTrilha> rolloversHitByBalls(final List<Body> balls) {
		this.hitRollovers.clear();

		final int rsize = this.rollovers.size();
		for (int i = 0; i < rsize; i++) {
			final ElementoTrilha rollover = this.rollovers.get(i);
			boolean hit = false;
			for (int j = 0; j < balls.size(); j++) {
				final Body ball = balls.get(j);
				final Vector2 position = ball.getPosition();
				final float xdiff = position.x - rollover.cx;
				final float ydiff = position.y - rollover.cy;
				final float distanceSquared = xdiff * xdiff + ydiff * ydiff;
				if (distanceSquared <= rollover.radiusSquared) {
					hit = true;
					break;
				}
			}
			if (hit) {
				this.hitRollovers.add(rollover);
			}
		}
		return this.hitRollovers;
	}

}
