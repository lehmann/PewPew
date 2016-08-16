package com.lehmann.pewpew;

import java.text.NumberFormat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public class ScoreView extends View {

	private Field field;
	private final Paint textPaint = new Paint();
	private final Rect textRect = new Rect();

	private final Paint fpsPaint = new Paint();

	private long highScore;
	private Long lastUpdateTime;

	private int gameOverMessageIndex = 0;
	private int gameOverMessageCycleTime = 3500;

	private double fps;
	private boolean showFPS = false;

	private static final NumberFormat SCORE_FORMAT = NumberFormat.getInstance();

	public ScoreView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.textPaint.setARGB(255, 255, 255, 0);
		this.textPaint.setAntiAlias(true);
		final DisplayMetrics metrics = new DisplayMetrics();
		final WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(metrics);
		this.textPaint.setTextSize(24 * metrics.density);

		this.fpsPaint.setARGB(255, 255, 255, 0);
	}

	@Override
	public void draw(final Canvas c) {
		String displayString = null;
		synchronized (this.field) {
			final GameMessage msg = this.field.getGameMessage();
			displayString = msg != null ? msg.text : null;
			if (displayString == null) {
				if (this.field.getGameState().isGameInProgress()) {
					displayString = ScoreView.SCORE_FORMAT.format(this.field
							.getGameState().getScore());
				} else {
					boolean cycle = false;
					if (this.lastUpdateTime == null) {
						this.lastUpdateTime = System.currentTimeMillis();
					} else if (System.currentTimeMillis() - this.lastUpdateTime > this.gameOverMessageCycleTime) {
						cycle = true;
						this.lastUpdateTime = System.currentTimeMillis();
					}
					displayString = this.displayedGameOverMessage(cycle);
				}
			}
		}

		this.textPaint.getTextBounds(displayString, 0, displayString.length(),
				this.textRect);
		c.drawText(displayString, this.getWidth() / 2 - this.textRect.width()
				/ 2, this.getHeight() / 2, this.textPaint);
		if (this.showFPS && this.fps > 0) {
			c.drawText(String.format("%.1f fps", this.fps), 0, 20,
					this.fpsPaint);
		}
	}

	public void setField(final Field value) {
		this.field = value;
	}

	public void setFPS(final double value) {
		this.fps = value;
	}

	public void setHighScore(final long value) {
		this.highScore = value;
	}

	public void setShowFPS(final boolean value) {
		this.showFPS = value;
	}

	String displayedGameOverMessage(final boolean cycle) {
		final String msg = null;
		if (cycle) {
			this.gameOverMessageIndex = (this.gameOverMessageIndex + 1) % 3;
		}
		while (msg == null) {
			switch (this.gameOverMessageIndex) {
				case 0:
					return "Toque para iniciar";
				case 1:
					final long score = this.field.getGameState().getScore();
					if (score > 0) {
						return "Última pontuação: "
								+ ScoreView.SCORE_FORMAT.format(score);
					}
					break;
				case 2:
					if (this.highScore > 0) {
						return "Recorde: "
								+ ScoreView.SCORE_FORMAT.format(this.highScore);
					}
					break;
			}
			this.gameOverMessageIndex = (this.gameOverMessageIndex + 1) % 3;
		}
		return msg;
	}

}
