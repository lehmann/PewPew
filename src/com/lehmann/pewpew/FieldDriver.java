package com.lehmann.pewpew;

import com.lehmann.pewpew.util.FPSRateManager;

public class FieldDriver {

	private FieldViewManager fieldViewManager;
	private Field field;

	private boolean running;
	private Thread gameThread;

	private FPSRateManager frameRateManager = new FPSRateManager(new double[] { 60,
			50, 45, 40, 30 }, new double[] { 57, 48, 43, 38 });
	private double averageFPS;

	private static final long INACTIVE_FRAME_MSECS = 250;

	public double getAverageFPS() {
		return this.averageFPS;
	}

	public void resetFrameRate() {
		this.frameRateManager.resetFrameRate();
	}

	public void setAverageFPS(final double value) {
		this.averageFPS = value;
	}

	public void setField(final Field value) {
		this.field = value;
	}

	public void setFieldViewManager(final FieldViewManager value) {
		this.fieldViewManager = value;
	}

	public void start() {
		this.running = true;
		this.gameThread = new Thread() {
			@Override
			public void run() {
				FieldDriver.this.threadMain();
			}
		};
		this.gameThread.start();
	}

	public void stop() {
		this.running = false;
		try {
			this.gameThread.join();
		} catch (final InterruptedException ex) {
		}
	}

	void drawField() {
		this.fieldViewManager.draw();
	}

	void threadMain() {
		while (this.running) {
			this.frameRateManager.frameStarted();
			boolean fieldActive = true;
			if (this.field != null && this.fieldViewManager.canDraw()) {
				try {
					synchronized (this.field) {
						final long nanosPerFrame = (long) (1000000000L / this.frameRateManager
								.targetFramesPerSecond());
						long fieldTickNanos = (long) (nanosPerFrame * this.field
								.getTargetTimeRatio());
						fieldActive = this.field.hasActiveElements();
						if (!fieldActive) {
							fieldTickNanos = (long) (FieldDriver.INACTIVE_FRAME_MSECS * 1000000 * this.field
									.getTargetTimeRatio());
						}
						this.field.tick(fieldTickNanos, 4);
					}
					this.drawField();
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}

			if (!fieldActive) {
				this.frameRateManager.clearTimestamps();
				this.setAverageFPS(0);
				try {
					Thread.sleep(FieldDriver.INACTIVE_FRAME_MSECS);
				} catch (final InterruptedException ignored) {
				}
				continue;
			}

			this.frameRateManager.sleepUntilNextFrame();

			if (this.frameRateManager.getTotalFrames() % 100 == 0) {
				this.fieldViewManager.setDebugMessage(this.frameRateManager
						.fpsDebugInfo());
				this.setAverageFPS(this.frameRateManager
						.currentFramesPerSecond());
			}
		}
	}

}
