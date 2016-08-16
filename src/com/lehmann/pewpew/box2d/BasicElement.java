package com.lehmann.pewpew.box2d;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.lehmann.pewpew.Field;
import com.lehmann.pewpew.IFieldRenderer;

/**
 * Classe base para todos os elementos participantes da mesa de jogo
 * 
 * @author limao
 * 
 */
public abstract class BasicElement {

	Map<String, Object> parameters;
	World box2dWorld;
	String elementID;
	int[] color; // cores em R-G-B

	int flashCounter = 0; // indicador de quantos frames a cor do elemento
							// ficará invertida (após ser atingido pela bola)
	long score = 0;

	// cor default para Parede, Arco, ParedeSegmento
	static int DEFAULT_WALL_RED = 160;
	static int DEFAULT_WALL_GREEN = 64;
	static int DEFAULT_WALL_BLUE = 64;

	public static BasicElement criaElemento(final Map<String, Object> params,
			final World world, Class<?> defaultClass) {
		try {
			if (params.containsKey("class")) {
				// se não estiver declarado o pacote, usa o padrão
				String className = (String) params.get("class");
				if (!className.contains(".")) {
					className = "com.lehmann.pewpew.box2d." + className;
				}
				defaultClass = Class.forName(className);
			}

			final BasicElement self = (BasicElement) defaultClass.newInstance();
			self.initialize(params, world);
			return self;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public abstract void draw(IFieldRenderer renderer);

	public abstract void finishCreate(Map<String, Object> params, World world);

	public void flashForFrames(final int frames) {
		this.flashCounter = frames;
	}

	public void flipsActivated(final Field field,
			final List<Alavanca> flippers) {

	}

	public abstract List<Body> getBodies();

	public String getElementID() {
		return this.elementID;
	}

	public Map<String, Object> getParameters() {
		return this.parameters;
	}

	public long getScore() {
		return this.score;
	}

	public void handleCollision(final Body ball, final Body bodyHit,
			final Field field) {
	}

	public void initialize(final Map<String, Object> params, final World world) {
		this.parameters = params;
		this.box2dWorld = world;
		this.elementID = (String) params.get("id");

		final List<Integer> colorList = (List<Integer>) params.get("color");
		if (colorList != null) {
			this.color = new int[] { colorList.get(0), colorList.get(1),
					colorList.get(2) };
		}

		if (params.containsKey("score")) {
			this.score = ((Number) params.get("score")).longValue();
		}

		this.finishCreate(params, world);
	}

	public boolean shouldCallTick() {
		return false;
	}

	public void tick(final Field field) {
		if (this.flashCounter > 0) {
			this.flashCounter--;
		}
	}

	protected int blueColorComponent(final int defvalue) {
		return this.colorComponent(2, defvalue);
	}

	protected int colorComponent(final int index, final int defvalue) {
		int value = defvalue;
		if (this.color != null) {
			value = this.color[index];
		}
		return this.flashCounter > 0 ? 255 - value : value;
	}

	protected int greenColorComponent(final int defvalue) {
		return this.colorComponent(1, defvalue);
	}

	protected int redColorComponent(final int defvalue) {
		return this.colorComponent(0, defvalue);
	}
}
