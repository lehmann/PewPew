package com.lehmann.pewpew.box2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.lehmann.pewpew.IFieldRenderer;
import com.lehmann.pewpew.util.MathUtility;

public class Arco extends BasicElement {

	private final List<Body> corpos = new ArrayList<Body>();
	private float[][] linhas;

	@Override
	public void draw(final IFieldRenderer renderer) {
		for (final float[] segment : this.linhas) {
			renderer.drawLine(segment[0], segment[1], segment[2], segment[3],
					this.redColorComponent(BasicElement.DEFAULT_WALL_RED),
					this.greenColorComponent(BasicElement.DEFAULT_WALL_GREEN),
					this.blueColorComponent(BasicElement.DEFAULT_WALL_BLUE));
		}
	}

	@Override
	public void finishCreate(final Map<String, Object> params, final World world) {
		final List centerPos = (List) params.get("center");
		final float cx = MathUtility.asFloat(centerPos.get(0));
		final float cy = MathUtility.asFloat(centerPos.get(1));

		float xradius, yradius;
		if (params.containsKey("radius")) {
			xradius = yradius = MathUtility.asFloat(params.get("radius"));
		} else {
			xradius = MathUtility.asFloat(params.get("xradius"));
			yradius = MathUtility.asFloat(params.get("yradius"));
		}

		final Number segments = (Number) params.get("segments");
		final int numsegments = segments != null ? segments.intValue() : 5;
		final float minangle = MathUtility.toRadians(MathUtility.asFloat(params
				.get("minangle")));
		final float maxangle = MathUtility.toRadians(MathUtility.asFloat(params
				.get("maxangle")));
		final float diff = maxangle - minangle;

		this.linhas = new float[numsegments][];
		for (int i = 0; i < numsegments; i++) {
			final float angle1 = minangle + i * diff / numsegments;
			final float angle2 = minangle + (i + 1) * diff / numsegments;
			final float x1 = cx + xradius * (float) Math.cos(angle1);
			final float y1 = cy + yradius * (float) Math.sin(angle1);
			final float x2 = cx + xradius * (float) Math.cos(angle2);
			final float y2 = cy + yradius * (float) Math.sin(angle2);

			final Body wall = Box2DFactory.createParede(world, x1, y1, x2, y2,
					0f);
			this.corpos.add(wall);
			this.linhas[i] = new float[] { x1, y1, x2, y2 };
		}
	}

	@Override
	public List<Body> getBodies() {
		return this.corpos;
	}

}
