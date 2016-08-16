package com.lehmann.pewpew.box2d;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.lehmann.pewpew.Field;
import com.lehmann.pewpew.IFieldRenderer;
import com.lehmann.pewpew.util.MathUtility;

/**
 * Representa a alavanca que impulsiona a bolinha
 * 
 * @author limao
 * 
 */
public class Alavanca extends BasicElement {

	Body flipperBody;
	List<Body> flipperBodySet;
	public Body anchorBody;
	public RevoluteJoint joint;
	RevoluteJointDef jointDef;

	float flipperLength;
	float upspeed, downspeed;
	float minangle, maxangle;
	float cx, cy;

	@Override
	public void draw(final IFieldRenderer renderer) {
		final Vector2 position = this.anchorBody.getPosition();
		float angle = this.joint.getJointAngle();
		if (angle < this.jointDef.lowerAngle) {
			angle = this.jointDef.lowerAngle;
		}
		if (angle > this.jointDef.upperAngle) {
			angle = this.jointDef.upperAngle;
		}
		final float x1 = position.x;
		final float y1 = position.y;
		final float x2 = position.x + this.flipperLength
				* (float) Math.cos(angle);
		final float y2 = position.y + this.flipperLength
				* (float) Math.sin(angle);

		renderer.drawLine(x1, y1, x2, y2, this.redColorComponent(0),
				this.greenColorComponent(255), this.blueColorComponent(0));

	}

	@Override
	public void finishCreate(final Map params, final World world) {
		final List pos = (List) params.get("position");

		this.cx = MathUtility.asFloat(pos.get(0));
		this.cy = MathUtility.asFloat(pos.get(1));
		this.flipperLength = MathUtility.asFloat(params.get("length"));
		this.minangle = MathUtility.toRadians(MathUtility.asFloat(params
				.get("minangle")));
		this.maxangle = MathUtility.toRadians(MathUtility.asFloat(params
				.get("maxangle")));
		this.upspeed = MathUtility.asFloat(params.get("upspeed"));
		this.downspeed = MathUtility.asFloat(params.get("downspeed"));

		this.anchorBody = Box2DFactory.createCirculo(world, this.cx, this.cy,
				0.05f, true);

		final float ext = this.flipperLength > 0 ? -0.05f : +0.05f;

		this.flipperBody = Box2DFactory.createParede(world, this.cx + ext,
				this.cy - 0.12f, this.cx + this.flipperLength, this.cy + 0.12f,
				0f);
		this.flipperBody.setType(BodyDef.BodyType.DynamicBody);
		this.flipperBody.setBullet(true);
		this.flipperBody.getFixtureList().get(0).setDensity(5.0f);

		this.jointDef = new RevoluteJointDef();
		this.jointDef.initialize(this.anchorBody, this.flipperBody,
				new Vector2(this.cx, this.cy));
		this.jointDef.enableLimit = true;
		this.jointDef.enableMotor = true;
		this.jointDef.lowerAngle = this.flipperLength > 0 ? this.minangle
				: -this.maxangle;
		this.jointDef.upperAngle = this.flipperLength > 0 ? this.maxangle
				: -this.minangle;
		this.jointDef.maxMotorTorque = 1000f;

		this.joint = (RevoluteJoint) world.createJoint(this.jointDef);

		this.flipperBodySet = Collections.singletonList(this.flipperBody);
		this.setEffectiveMotorSpeed(-this.downspeed);
	}

	public Body getAnchorBody() {
		return this.anchorBody;
	}

	@Override
	public List<Body> getBodies() {
		return this.flipperBodySet;
	}

	public float getFlipperLength() {
		return this.flipperLength;
	}

	public RevoluteJoint getJoint() {
		return this.joint;
	}

	public boolean isFlipperEngaged() {
		return this.getEffectiveMotorSpeed() > 0;
	}

	public boolean isLeftFlipper() {
		return !this.isReversed();
	}

	public boolean isRightFlipper() {
		return this.isReversed();
	}

	public void setFlipperEngaged(final boolean active) {
		if (active != this.isFlipperEngaged()) {
			final float speed = active ? this.upspeed : -this.downspeed;
			this.setEffectiveMotorSpeed(speed);
		}
	}

	@Override
	public boolean shouldCallTick() {
		return true;
	}

	@Override
	public void tick(final Field field) {
		super.tick(field);

		if (this.getEffectiveMotorSpeed() > 0.5f) {
			final float topAngle = this.isReversed() ? this.jointDef.lowerAngle
					: this.jointDef.upperAngle;
			if (Math.abs(topAngle - this.joint.getJointAngle()) < 0.05) {
				this.setEffectiveMotorSpeed(0.5f);
			}
		}
	}

	float getEffectiveMotorSpeed() {
		final float speed = this.joint.getMotorSpeed();
		return this.isReversed() ? -speed : speed;
	}

	boolean isReversed() {
		return this.flipperLength < 0;
	}

	void setEffectiveMotorSpeed(float speed) {
		if (this.isReversed()) {
			speed = -speed;
		}
		this.joint.setMotorSpeed(speed);
	}
}
