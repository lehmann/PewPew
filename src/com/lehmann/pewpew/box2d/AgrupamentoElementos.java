package com.lehmann.pewpew.box2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.lehmann.pewpew.Field;
import com.lehmann.pewpew.IFieldRenderer;
import com.lehmann.pewpew.util.MathUtility;

public class AgrupamentoElementos extends BasicElement {

	List<Body> allBodies = new ArrayList<Body>();
	Map<Body, float[]> bodyPositions = new HashMap<Body, float[]>();

	public boolean allTargetsHit() {
		final int bsize = this.allBodies.size();
		for (int i = 0; i < bsize; i++) {
			if (this.allBodies.get(i).isActive()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void draw(final IFieldRenderer renderer) {
		final int red = this.redColorComponent(0);
		final int green = this.greenColorComponent(255);
		final int blue = this.blueColorComponent(0);

		final int bsize = this.allBodies.size();
		for (int i = 0; i < bsize; i++) {
			final Body body = this.allBodies.get(i);
			if (body.isActive()) {
				final float[] parray = this.bodyPositions.get(body);
				renderer.drawLine(parray[0], parray[1], parray[2], parray[3],
						red, green, blue);
			}
		}

	}

	@Override
	public void finishCreate(final Map<String, Object> params, final World world) {
		final List<List> positions = (List<List>) params.get("positions");
		for (final List pos : positions) {
			final float[] parray = new float[] { MathUtility.asFloat(pos.get(0)),
					MathUtility.asFloat(pos.get(1)),
					MathUtility.asFloat(pos.get(2)),
					MathUtility.asFloat(pos.get(3)) };
			final float restitution = 0f;
			final Body wallBody = Box2DFactory.createParede(world, parray[0],
					parray[1], parray[2], parray[3], restitution);
			this.allBodies.add(wallBody);
			this.bodyPositions.put(wallBody, parray);
		}
	}

	@Override
	public List<Body> getBodies() {
		return this.allBodies;
	}

	@Override
	public void handleCollision(final Body ball, final Body bodyHit,
			final Field field) {
		bodyHit.setActive(false);
		if (this.allTargetsHit()) {
			field.getDelegate().allDropTargetsInGroupHit(field, this);

			final float restoreTime = MathUtility.asFloat(this.parameters
					.get("reset"));
			if (restoreTime > 0) {
				field.scheduleAction((long) (restoreTime * 1000),
						new Runnable() {
							@Override
							public void run() {
								AgrupamentoElementos.this
										.makeAllTargetsVisible();
							}
						});
			}
		}
	}

	public void makeAllTargetsVisible() {
		final int bsize = this.allBodies.size();
		for (int i = 0; i < bsize; i++) {
			this.allBodies.get(i).setActive(true);
		}
	}

}
