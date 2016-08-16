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

public class BarreiraForca extends BasicElement {

	float xmin, ymin, xmax, ymax;
	boolean circular = false;
	float cx, cy;
	float radiusSquared;

	@Override
	public void draw(final IFieldRenderer renderer) {
		// no UI
	}

	@Override
	public void finishCreate(final Map<String, Object> params, final World world) {
		if (params.containsKey("center") && params.containsKey("radius")) {
			this.circular = true;
			final List centerPos = (List) params.get("center");
			this.cx = MathUtility.asFloat(centerPos.get(0));
			this.cy = MathUtility.asFloat(centerPos.get(1));
			final float radius = MathUtility.asFloat(params.get("radius"));
			this.radiusSquared = radius * radius;
			// cria bounding box para permitir a rejeição de outros elementos
			this.xmin = this.cx - radius / 2;
			this.xmax = this.cx + radius / 2;
			this.ymin = this.cy - radius / 2;
			this.ymax = this.cy + radius / 2;
		} else {
			final List rectPos = (List) params.get("rect");
			this.xmin = MathUtility.asFloat(rectPos.get(0));
			this.ymin = MathUtility.asFloat(rectPos.get(1));
			this.xmax = MathUtility.asFloat(rectPos.get(2));
			this.ymax = MathUtility.asFloat(rectPos.get(3));
		}
	}

	@Override
	public List<Body> getBodies() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public boolean shouldCallTick() {
		return true;
	}

	@Override
	public void tick(final Field field) {
		final List<Body> balls = field.getBalls();
		for (int i = 0; i < balls.size(); i++) {
			final Body ball = balls.get(i);
			if (this.ballInRange(ball)) {
				field.getDelegate().ballInSensorRange(field, this, ball);
				return;
			}
		}
	}

	boolean ballInRange(final Body ball) {
		final Vector2 bpos = ball.getPosition();
		if (bpos.x < this.xmin || bpos.x > this.xmax || bpos.y < this.ymin
				|| bpos.y > this.ymax) {
			return false;
		}
		if (this.circular) {
			final float distSquared = (bpos.x - this.cx) * (bpos.x - this.cx)
					+ (bpos.y - this.cy) * (bpos.y - this.cy);
			if (distSquared > this.radiusSquared) {
				return false;
			}
		}
		return true;
	}

}
