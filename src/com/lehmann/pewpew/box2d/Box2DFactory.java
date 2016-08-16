package com.lehmann.pewpew.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Classe utilitária para a criação de elementos
 * 
 * @author limao
 * 
 */
public class Box2DFactory {

	public static Body createCirculo(final World world, final float x,
			final float y, final float radius, final boolean isStatic) {
		final CircleShape sd = new CircleShape();
		sd.setRadius(radius);

		final FixtureDef fdef = new FixtureDef();
		fdef.shape = sd;
		fdef.density = 1.0f;
		fdef.friction = 0.3f;
		fdef.restitution = 0.6f;

		final BodyDef bd = new BodyDef();
		bd.allowSleep = true;
		bd.position.set(x, y);
		final Body body = world.createBody(bd);
		body.createFixture(fdef);
		if (isStatic) {
			body.setType(BodyDef.BodyType.StaticBody);
		} else {
			body.setType(BodyDef.BodyType.DynamicBody);
		}
		return body;
	}

	public static Body createParede(final World world, final float x1,
			final float y1, final float x2, final float y2,
			final float restitution) {
		final float cx = (x1 + x2) / 2;
		final float cy = (y1 + y2) / 2;
		final float angle = (float) Math.atan2(y2 - y1, x2 - x1);
		final float mag = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1)
				* (y2 - y1));
		return Box2DFactory.createParede(world, cx - mag / 2, cy - 0.05f, cx
				+ mag / 2, cy + 0.05f, angle, restitution);
	}

	public static Body createParede(final World world, final float xmin,
			final float ymin, final float xmax, final float ymax,
			final float angle, final float restitution) {
		final float cx = (xmin + xmax) / 2;
		final float cy = (ymin + ymax) / 2;
		float hx = (xmax - xmin) / 2;
		float hy = (ymax - ymin) / 2;
		if (hx < 0) {
			hx = -hx;
		}
		if (hy < 0) {
			hy = -hy;
		}
		final PolygonShape wallshape = new PolygonShape();
		wallshape.setAsBox(hx, hy, new Vector2(0f, 0f), angle);

		final FixtureDef fdef = new FixtureDef();
		fdef.shape = wallshape;
		fdef.density = 1.0f;
		if (restitution > 0) {
			fdef.restitution = restitution;
		}

		final BodyDef bd = new BodyDef();
		bd.position.set(cx, cy);
		final Body wall = world.createBody(bd);
		wall.createFixture(fdef);
		wall.setType(BodyDef.BodyType.StaticBody);
		return wall;
	}

}
