package com.lehmann.pewpew.box2d;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.lehmann.pewpew.Field;
import com.lehmann.pewpew.IFieldRenderer;
import com.lehmann.pewpew.util.MathUtility;

public class Parede extends BasicElement {

	Body wallBody;
	List<Body> bodySet;
	float x1, y1, x2, y2;
	float kick;

	boolean killBall;
	boolean retractWhenHit;

	@Override
	public void draw(final IFieldRenderer renderer) {
		if (this.isRetracted()) {
			return;
		}
		renderer.drawLine(this.x1, this.y1, this.x2, this.y2,
				this.redColorComponent(BasicElement.DEFAULT_WALL_RED),
				this.greenColorComponent(BasicElement.DEFAULT_WALL_GREEN),
				this.blueColorComponent(BasicElement.DEFAULT_WALL_BLUE));
	}

	@Override
	public void finishCreate(final Map params, final World world) {
		final List pos = (List) params.get("position");
		this.x1 = MathUtility.asFloat(pos.get(0));
		this.y1 = MathUtility.asFloat(pos.get(1));
		this.x2 = MathUtility.asFloat(pos.get(2));
		this.y2 = MathUtility.asFloat(pos.get(3));
		final float restitution = MathUtility.asFloat(params.get("restitution"));

		this.wallBody = Box2DFactory.createParede(world, this.x1, this.y1,
				this.x2, this.y2, restitution);
		this.bodySet = Collections.singletonList(this.wallBody);

		this.kick = MathUtility.asFloat(params.get("kick"));
		this.killBall = Boolean.TRUE.equals(params.get("kill"));
		this.retractWhenHit = Boolean.TRUE.equals(params.get("retractWhenHit"));

		final boolean disabled = Boolean.TRUE.equals(params.get("disabled"));
		if (disabled) {
			this.setRetracted(true);
		}
	}

	@Override
	public List<Body> getBodies() {
		return this.bodySet;
	}

	@Override
	public void handleCollision(final Body ball, final Body bodyHit,
			final Field field) {
		if (this.retractWhenHit) {
			this.setRetracted(true);
		}

		if (this.killBall) {
			field.removeBall(ball);
		} else {
			final Vector2 impulse = this.impulseForBall(ball);
			if (impulse != null) {
				ball.applyLinearImpulse(impulse, ball.getWorldCenter());
				this.flashForFrames(3);
			}
		}
	}

	public boolean isRetracted() {
		return !this.wallBody.isActive();
	}

	public void setRetracted(final boolean retracted) {
		if (retracted != this.isRetracted()) {
			this.wallBody.setActive(!retracted);
		}
	}

	@Override
	public boolean shouldCallTick() {
		return this.kick > 0.01f;
	}

	Vector2 impulseForBall(final Body ball) {
		if (this.kick <= 0.01f) {
			return null;
		}
		float ix = this.y2 - this.y1;
		float iy = this.x1 - this.x2;
		final float mag = (float) Math.sqrt(ix * ix + iy * iy);
		final float scale = this.kick / mag;
		ix *= scale;
		iy *= scale;

		final Vector2 balldiff = ball.getWorldCenter().cpy()
				.sub(this.x1, this.y1);
		final float dotprod = balldiff.x * ix + balldiff.y * iy;
		if (dotprod < 0) {
			ix = -ix;
			iy = -iy;
		}

		return new Vector2(ix, iy);
	}

}
