package com.lehmann.pewpew;

import java.lang.reflect.Method;
import java.util.List;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class FieldViewManager implements SurfaceHolder.Callback {

	private IFieldRenderer view;
	private boolean canDraw;
	private Runnable startGameAction;

	private boolean showFPS;

	private boolean highQuality;

	private Field field;
	private float zoom = 1.0f;

	private float maxZoom = 1.0f;

	private float cachedXOffset, cachedYOffset, cachedScale, cachedHeight;
	private String debugMessage;

	private Method getPointerCountMethod;

	private Method getXMethod;

	private int MOTIONEVENT_ACTION_MASK = 0xffffffff;

	private int MOTIONEVENT_ACTION_POINTER_UP;

	private int MOTIONEVENT_ACTION_POINTER_INDEX_MASK;

	private int MOTIONEVENT_ACTION_POINTER_INDEX_SHIFT;

	{
		try {
			this.getPointerCountMethod = MotionEvent.class
					.getMethod("getPointerCount");
			this.getXMethod = MotionEvent.class.getMethod("getX", int.class);
			this.MOTIONEVENT_ACTION_MASK = MotionEvent.class.getField(
					"ACTION_MASK").getInt(null);
			this.MOTIONEVENT_ACTION_POINTER_UP = MotionEvent.class.getField(
					"ACTION_POINTER_UP").getInt(null);
			this.MOTIONEVENT_ACTION_POINTER_INDEX_MASK = MotionEvent.class
					.getField("ACTION_POINTER_INDEX_MASK").getInt(null);
			this.MOTIONEVENT_ACTION_POINTER_INDEX_SHIFT = MotionEvent.class
					.getField("ACTION_POINTER_INDEX_SHIFT").getInt(null);
		} catch (final Exception ex) {
		}
	}

	public void cacheScaleAndOffsets() {
		this.zoom = this.maxZoom;
		if (this.zoom <= 1.0f || !this.field.getGameState().isGameInProgress()) {
			this.cachedXOffset = 0;
			this.cachedYOffset = 0;
			this.zoom = 1.0f;
		} else {
			final List<Body> balls = this.field.getBalls();
			float x = -1, y = -1;
			if (balls.size() == 1) {
				final Body b = balls.get(0);
				x = b.getPosition().x;
				y = b.getPosition().y;
			} else if (balls.size() == 0) {
				final List<Number> position = this.field.getLaunchPosition();
				x = position.get(0).floatValue();
				y = position.get(1).floatValue();
			} else {
				for (final Body b : balls) {
					final Vector2 pos = b.getPosition();
					if (y < 0 || pos.y < y) {
						x = pos.x;
						y = pos.y;
					}
				}
			}
			final float maxOffsetRatio = 1.0f - 1.0f / this.zoom;
			this.cachedXOffset = x - this.field.getWidth() / (2.0f * this.zoom);
			if (this.cachedXOffset < 0) {
				this.cachedXOffset = 0;
			}
			if (this.cachedXOffset > this.field.getWidth() * maxOffsetRatio) {
				this.cachedXOffset = this.field.getWidth() * maxOffsetRatio;
			}
			this.cachedYOffset = y - this.field.getHeight()
					/ (2.0f * this.zoom);
			if (this.cachedYOffset < 0) {
				this.cachedYOffset = 0;
			}
			if (this.cachedYOffset > this.field.getHeight() * maxOffsetRatio) {
				this.cachedYOffset = this.field.getHeight() * maxOffsetRatio;
			}
		}

		this.cachedScale = this.getScale();
		this.cachedHeight = this.view.getHeight();
	}

	public boolean canDraw() {
		return this.canDraw;
	}

	public void draw() {
		this.cacheScaleAndOffsets();
		this.view.doDraw();
	}

	public String getDebugMessage() {
		return this.debugMessage;
	}

	public Field getField() {
		return this.field;
	}

	public boolean handleTouchEvent(final MotionEvent event) {
		final int actionType = event.getAction() & this.MOTIONEVENT_ACTION_MASK;
		synchronized (this.field) {
			if (!this.field.getGameState().isGameInProgress()) {
				if (this.startGameAction != null) {
					this.startGameAction.run();
					return true;
				}
			}
			if (actionType == MotionEvent.ACTION_DOWN) {
				this.field.handleDeadBalls();
				if (this.field.getBalls().size() == 0) {
					this.field.launchBall();
				}
			}
			try {
				boolean left = false, right = false;
				if (actionType != MotionEvent.ACTION_UP) {
					final int npointers = (Integer) this.getPointerCountMethod
							.invoke(event);
					int liftedPointerIndex = -1;
					if (actionType == this.MOTIONEVENT_ACTION_POINTER_UP) {
						liftedPointerIndex = (event.getAction() & this.MOTIONEVENT_ACTION_POINTER_INDEX_MASK) >> this.MOTIONEVENT_ACTION_POINTER_INDEX_SHIFT;
					}
					final float halfwidth = this.view.getWidth() / 2;
					for (int i = 0; i < npointers; i++) {
						if (i != liftedPointerIndex) {
							final float touchx = (Float) this.getXMethod
									.invoke(event, i);
							if (touchx < halfwidth) {
								left = true;
							} else {
								right = true;
							}
						}
					}
				}
				this.field.setLeftFlippersEngaged(left);
				this.field.setRightFlippersEngaged(right);
			} catch (final Exception ignored) {
			}
		}
		return true;
	}

	public boolean isHighQuality() {
		return this.highQuality;
	}

	public void setDebugMessage(final String value) {
		this.debugMessage = value;
	}

	public void setField(final Field value) {
		this.field = value;
	}

	public void setFieldView(final IFieldRenderer view) {
		if (this.view != view) {
			this.view = view;
			view.setManager(this);
			if (view instanceof SurfaceView) {
				this.canDraw = false;
				((SurfaceView) view).getHolder().addCallback(this);
			} else {
				this.canDraw = true;
			}
		}
	}

	public void setHighQuality(final boolean value) {
		this.highQuality = value;
	}

	public void setShowFPS(final boolean value) {
		this.showFPS = value;
	}

	public void setStartGameAction(final Runnable action) {
		this.startGameAction = action;
	}

	public void setZoom(final float value) {
		this.maxZoom = value;
	}

	public boolean showFPS() {
		return this.showFPS;
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, final int format,
			final int width, final int height) {
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		this.canDraw = true;
	}

	@Override
	public void surfaceDestroyed(final SurfaceHolder holder) {
		this.canDraw = false;
	}

	public float world2pixelX(final float x) {
		return (x - this.cachedXOffset) * this.cachedScale;
	}

	public float world2pixelY(final float y) {
		return this.cachedHeight - (y - this.cachedYOffset) * this.cachedScale;
	}

	float getCachedScale() {
		return this.cachedScale;
	}

	float getScale() {
		final float xs = this.view.getWidth() / this.field.getWidth();
		final float ys = this.view.getHeight() / this.field.getHeight();
		return Math.min(xs, ys) * this.zoom;
	}

}
