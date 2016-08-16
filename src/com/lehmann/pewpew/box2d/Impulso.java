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

public class Impulso extends BasicElement {

	Body pegBody;
	List<Body> pegBodySet;

	float radius;
	float cx, cy;
	float kick;

	@Override
	public void draw(final IFieldRenderer renderer) {
		final float px = this.pegBody.getPosition().x;
		final float py = this.pegBody.getPosition().y;
		renderer.fillCircle(px, py, this.radius, this.redColorComponent(0),
				this.greenColorComponent(0), this.blueColorComponent(255));
	}

	@Override
	public void finishCreate(final Map params, final World world) {
		final List pos = (List) params.get("position");
		this.radius = MathUtility.asFloat(params.get("radius"));
		this.cx = MathUtility.asFloat(pos.get(0));
		this.cy = MathUtility.asFloat(pos.get(1));
		this.kick = MathUtility.asFloat(params.get("kick"));

		this.pegBody = Box2DFactory.createCirculo(world, this.cx, this.cy,
				this.radius, true);
		this.pegBodySet = Collections.singletonList(this.pegBody);
	}

	@Override
	public List<Body> getBodies() {
		return this.pegBodySet;
	}

	@Override
	public void handleCollision(final Body ball, final Body bodyHit,
			final Field field) {
		final Vector2 impulse = this.impulseForBall(ball);
		if (impulse != null) {
			ball.applyLinearImpulse(impulse, ball.getWorldCenter());
			this.flashForFrames(3);
		}
	}

	@Override
	public boolean shouldCallTick() {
		return true;
	}

	Vector2 impulseForBall(final Body ball) {
		if (this.kick <= 0.01f) {
			return null;
		}
		final Vector2 ballpos = ball.getWorldCenter();
		final Vector2 thisPos = this.pegBody.getPosition();
		final float ix = ballpos.x - thisPos.x;
		final float iy = ballpos.y - thisPos.y;
		final float mag = (float) Math.sqrt(ix * ix + iy * iy);
		final float scale = this.kick / mag;
		return new Vector2(ix * scale, iy * scale);
	}
}
