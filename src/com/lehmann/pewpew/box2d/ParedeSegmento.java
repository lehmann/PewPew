package com.lehmann.pewpew.box2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.lehmann.pewpew.IFieldRenderer;
import com.lehmann.pewpew.util.MathUtility;

public class ParedeSegmento extends BasicElement {

	private final List wallBodies = new ArrayList();
	private float[][] lineSegments;

	@Override
	public void draw(final IFieldRenderer renderer) {
		for (final float[] segment : this.lineSegments) {
			renderer.drawLine(segment[0], segment[1], segment[2], segment[3],
					this.redColorComponent(BasicElement.DEFAULT_WALL_RED),
					this.greenColorComponent(BasicElement.DEFAULT_WALL_GREEN),
					this.blueColorComponent(BasicElement.DEFAULT_WALL_BLUE));
		}
	}

	@Override
	public void finishCreate(final Map params, final World world) {
		final List positions = (List) params.get("positions");
		this.lineSegments = new float[positions.size() - 1][];
		for (int i = 0; i < this.lineSegments.length; i++) {
			final List startpos = (List) positions.get(i);
			final List endpos = (List) positions.get(i + 1);

			final float[] segment = new float[] {
					MathUtility.asFloat(startpos.get(0)),
					MathUtility.asFloat(startpos.get(1)),
					MathUtility.asFloat(endpos.get(0)),
					MathUtility.asFloat(endpos.get(1)) };
			this.lineSegments[i] = segment;

			final Body wall = Box2DFactory.createParede(world, segment[0],
					segment[1], segment[2], segment[3], 0f);
			this.wallBodies.add(wall);
		}
	}

	@Override
	public List<Body> getBodies() {
		return this.wallBodies;
	}

}
