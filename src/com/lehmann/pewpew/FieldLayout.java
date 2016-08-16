package com.lehmann.pewpew;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.badlogic.gdx.physics.box2d.World;
import com.lehmann.pewpew.box2d.Alavanca;
import com.lehmann.pewpew.box2d.BasicElement;
import com.lehmann.pewpew.util.JSONUtility;
import com.lehmann.pewpew.util.MathUtility;

public class FieldLayout {

	private static int levelsCount = -1;
	private static final Map<Object, Map<String, Object>> layoutMap = new HashMap<Object, Map<String, Object>>();
	private static Context ctx;

	private static List<Integer> DEFAULT_BALL_COLOR = Arrays.asList(255, 0, 0);

	private static final Random RND = new Random();
	private final List<BasicElement> fieldElements = new ArrayList<BasicElement>();
	private List<Alavanca> flips, leftFlips, rightFlips;
	private float width;
	private float height;
	private List<Integer> ballColor;
	private float targetTimeRatio;

	private Map<String, Object> allParameters;

	public static FieldLayout layoutForLevel(final int level, final World world) {
		Map<String, Object> levelLayout = FieldLayout.layoutMap.get(level);
		if (levelLayout == null) {
			levelLayout = FieldLayout.readFieldLayout(level);
			FieldLayout.layoutMap.put(level, levelLayout);
		}
		return new FieldLayout(levelLayout, world);
	}

	/**
	 * Retorna a quantidade de mesas disponíveis para jogo. Atualmente existe só 1, mas nada impede de ser criada mais mesas.
	 * @return a quantidade de mesas disponíveis para jogo
	 */
	public static int numberOfLevels() {
		if (FieldLayout.levelsCount > 0) {
			return FieldLayout.levelsCount;
		}
		try {
			final List<String> tableFiles = Arrays.asList(FieldLayout.ctx
					.getAssets().list("mesas"));
			int count = 0;
			while (tableFiles.contains("mesa" + (count + 1) + ".tbl")) {
				count++;
			}
			FieldLayout.levelsCount = count;
		} catch (final IOException ex) {
			Log.e("FieldLayout", "Problema ao carregar as mesas de jogo", ex);
		}
		return FieldLayout.levelsCount;
	}

	public static void setContext(final Context value) {
		ctx = value;
	}

	static List listForKey(final Map<String, Object> map, final String key) {
		if (map.containsKey(key)) {
			return (List) map.get(key);
		}
		return Collections.EMPTY_LIST;
	}

	static Map<String, Object> readFieldLayout(final int level) {
		try {
			final String assetPath = "mesas/mesa" + level + ".tbl";
			AssetManager assets = FieldLayout.ctx.getAssets();

			//layout da mesa é compactado, para economizar espaço no .apk
			final InputStream fin = new GZIPInputStream(assets.open(assetPath));
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					fin));

			final StringBuilder buffer = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				buffer.append(line);
			}
			fin.close();
			final Map<String, Object> layoutMap = JSONUtility
					.mapFromJSONString(buffer.toString());
			return layoutMap;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public FieldLayout(final Map<String, Object> layoutMap, final World world) {
		this.width = MathUtility.asFloat(layoutMap.get("width"), 20.0f);
		this.height = MathUtility.asFloat(layoutMap.get("height"), 30.0f);
		this.targetTimeRatio = MathUtility.asFloat(layoutMap
				.get("targetTimeRatio"));
		this.ballColor = layoutMap.containsKey("ballcolor") ? (List<Integer>) layoutMap
				.get("ballcolor") : FieldLayout.DEFAULT_BALL_COLOR;
		this.allParameters = layoutMap;

		this.flips = this.addFieldElements(layoutMap, "flippers",
				Alavanca.class, world);
		this.leftFlips = new ArrayList<Alavanca>();
		this.rightFlips = new ArrayList<Alavanca>();
		for (final Alavanca f : this.flips) {
			if (f.isLeftFlipper()) {
				this.leftFlips.add(f);
			} else {
				this.rightFlips.add(f);
			}
		}

		this.addFieldElements(layoutMap, "elements", null, world);
	}

	public List<Integer> getBallColor() {
		return this.ballColor;
	}

	public float getBallRadius() {
		return MathUtility.asFloat(this.allParameters.get("ballradius"), 0.5f);
	}

	public String getDelegateClassName() {
		return (String) this.allParameters.get("delegate");
	}

	public List<BasicElement> getFieldElements() {
		return this.fieldElements;
	}

	public List<Alavanca> getFlipperElements() {
		return this.flips;
	}

	public float getGravity() {
		return MathUtility.asFloat(this.allParameters.get("gravity"), 4.0f);
	}

	public float getHeight() {
		return this.height;
	}

	public List<Number> getLaunchDeadZone() {
		final Map<String, Object> launchMap = (Map<String, Object>) this.allParameters
				.get("launch");
		return (List<Number>) launchMap.get("deadzone");
	}

	public List<Number> getLaunchPosition() {
		final Map<String, Object> launchMap = (Map<String, Object>) this.allParameters
				.get("launch");
		return (List<Number>) launchMap.get("position");
	}

	public List<Float> getLaunchVelocity() {
		final Map<String, Object> launchMap = (Map<String, Object>) this.allParameters
				.get("launch");
		final List<Number> velocity = (List<Number>) launchMap.get("velocity");
		float vx = velocity.get(0).floatValue();
		float vy = velocity.get(1).floatValue();

		if (launchMap.containsKey("random_velocity")) {
			final List<Number> delta = (List<Number>) launchMap
					.get("random_velocity");
			if (delta.get(0).floatValue() > 0) {
				vx += delta.get(0).floatValue() * this.RND.nextFloat();
			}
			if (delta.get(1).floatValue() > 0) {
				vy += delta.get(1).floatValue() * this.RND.nextFloat();
			}
		}
		return Arrays.asList(vx, vy);
	}

	public List<Alavanca> getLeftFlipperElements() {
		return this.leftFlips;
	}

	public int getNumberOfBalls() {
		return this.allParameters.containsKey("numballs") ? ((Number) this.allParameters
				.get("numballs")).intValue() : 3;
	}

	public List<Alavanca> getRightFlipperElements() {
		return this.rightFlips;
	}

	public float getTargetTimeRatio() {
		return this.targetTimeRatio;
	}

	public Object getValueWithKey(final String key) {
		final Map<String, Object> values = (Map<String, Object>) this.allParameters
				.get("values");
		if (values == null) {
			return null;
		}
		return values.get(key);
	}

	public float getWidth() {
		return this.width;
	}

	<T extends BasicElement> List<T> addFieldElements(
			final Map<String, Object> layoutMap, final String key,
			final Class<T> defaultClass, final World world) {
		final List<T> elements = new ArrayList<T>();
		for (final Object obj : FieldLayout.listForKey(layoutMap, key)) {
			if (!(obj instanceof Map)) {
				continue;
			}
			final Map<String, Object> params = (Map<String, Object>) obj;
			elements.add((T) BasicElement.criaElemento(params, world,
					defaultClass));
		}
		this.fieldElements.addAll(elements);
		return elements;
	}
}
