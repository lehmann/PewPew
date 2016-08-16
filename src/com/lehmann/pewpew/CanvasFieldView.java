package com.lehmann.pewpew;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.lehmann.pewpew.box2d.BasicElement;

public class CanvasFieldView extends SurfaceView implements IFieldRenderer {

	private FieldViewManager manager;
	private Canvas canvas;

	private final Paint paint = new Paint();
	private final Paint backgroundPaint = new Paint();
	private final Paint textPaint = new Paint();
	private final Rect backgroundDest = new Rect();
	private final Bitmap backgroundBitmap;

	public CanvasFieldView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.paint.setAntiAlias(true);
		this.paint.setStrokeWidth(20);
		this.textPaint.setARGB(255, 255, 255, 255);
		backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.star_wars_background);
	}

	@Override
	public void doDraw() {
		final Canvas c = this.getHolder().lockCanvas();
		c.drawARGB(255, 0, 0, 0);
		backgroundDest.set(0, 0, c.getWidth(), c.getHeight());
		c.drawBitmap(backgroundBitmap, null, backgroundDest, backgroundPaint);
		this.canvas = c;

		for (final BasicElement element : this.manager.getField()
				.getFieldElementsArray()) {
			element.draw(this);
		}

		this.manager.getField().drawBalls(this);

		if (this.manager.showFPS()) {
			if (this.manager.getDebugMessage() != null) {
				c.drawText("" + this.manager.getDebugMessage(), 10, 10,
						this.textPaint);
			}
		}
		this.getHolder().unlockCanvasAndPost(c);
	}

	@Override
	public void drawLine(final float x1, final float y1, final float x2,
			final float y2, final int red, final int green, final int blue) {
		this.paint.setARGB(255, red, green, blue);
		this.paint.setStrokeWidth(this.manager.isHighQuality() ? 10 : 5);
		this.canvas.drawLine(this.manager.world2pixelX(x1),
				this.manager.world2pixelY(y1), this.manager.world2pixelX(x2),
				this.manager.world2pixelY(y2), this.paint);
	}

	@Override
	public void fillCircle(final float cx, final float cy, final float radius,
			final int red, final int green, final int blue) {
		this.drawCircle(cx, cy, radius, red, green, blue, Paint.Style.FILL);
	}

	@Override
	public void frameCircle(final float cx, final float cy, final float radius,
			final int red, final int green, final int blue) {
		this.drawCircle(cx, cy, radius, red, green, blue, Paint.Style.STROKE);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		return this.manager.handleTouchEvent(event);
	}

	@Override
	public void setManager(final FieldViewManager value) {
		this.manager = value;
	}

	void drawCircle(final float cx, final float cy, final float radius,
			final int red, final int green, final int blue,
			final Paint.Style style) {
		this.paint.setARGB(255, red, green, blue);
		this.paint.setStyle(style);
		this.paint.setStrokeWidth(this.manager.isHighQuality() ? 10 : 5);
		final float rad = radius * this.manager.getCachedScale();
		this.canvas.drawCircle(this.manager.world2pixelX(cx),
				this.manager.world2pixelY(cy), rad, this.paint);
	}

}
