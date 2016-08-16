package com.lehmann.pewpew.util;

import java.util.LinkedList;

public class FPSRateManager {

	double[] targetFrameRates;
	double[] minimumFrameRates;
	int currentRateIndex = 0;
	long currentNanosPerFrame;

	double targetFrameRateFudgeFactor = 1.015;
	double[] unfudgedTargetFrameRates;

	LinkedList<Long> previousFrameTimestamps = new LinkedList<Long>();

	int frameHistorySize = 10;
	boolean allowReducingFrameRate = true;
	boolean allowLockingFrameRate = true;

	boolean frameRateLocked = false;

	int maxGoodFrames = 500;
	int maxSlowFrames = 150;

	double currentFPS = -1;
	int goodFrames = 0;
	int slowFrames = 0;

	long totalFrames = 0;

	final static long BILLION = 1000000000L; // nanoseconds per second
	final static long MILLION = 1000000L; // nanoseconds per millisecond

	public FPSRateManager(final double frameRate) {
		this(new double[] { frameRate }, new double[0]);
	}

	public FPSRateManager(final double[] targetRates, final double[] minRates) {
		if (targetRates == null || minRates == null
				|| minRates.length < targetRates.length - 1) {
			throw new IllegalArgumentException(
					"Must specify as many minimum rates as target rates minus one");
		}

		this.unfudgedTargetFrameRates = targetRates;
		this.minimumFrameRates = minRates;

		this.targetFrameRates = new double[targetRates.length];
		for (int i = 0; i < targetRates.length; i++) {
			this.targetFrameRates[i] = this.targetFrameRateFudgeFactor
					* targetRates[i];
		}

		this.setCurrentRateIndex(0);
	}

	public boolean allowLockingFrameRate() {
		return this.allowLockingFrameRate;
	}

	public boolean allowReducingFrameRate() {
		return this.allowReducingFrameRate;
	}

	public void clearTimestamps() {
		this.previousFrameTimestamps.clear();
		this.goodFrames = 0;
		this.slowFrames = 0;
		this.currentFPS = -1;
	}

	public double currentFramesPerSecond() {
		return this.currentFPS;
	}

	public String formattedCurrentFramesPerSecond() {
		return String.format("%.1f", this.currentFPS);
	}

	public String fpsDebugInfo() {
		return String.format("FPS: %.1f target: %.1f %s", this.currentFPS, this
				.targetFramesPerSecond(), this.frameRateLocked ? "(locked)"
				: "");
	}

	public void frameStarted() {
		this.frameStarted(System.nanoTime());
	}

	public void frameStarted(final long time) {
		++this.totalFrames;
		this.previousFrameTimestamps.add(time);
		if (this.previousFrameTimestamps.size() > this.frameHistorySize) {
			final long firstTime = this.previousFrameTimestamps.removeFirst();
			final double seconds = (time - firstTime)
					/ (double) FPSRateManager.BILLION;
			this.currentFPS = this.frameHistorySize / seconds;

			if (!this.frameRateLocked
					&& this.currentRateIndex < this.minimumFrameRates.length) {
				if (this.currentFPS < this.minimumFrameRates[this.currentRateIndex]) {
					++this.slowFrames;
					if (this.slowFrames >= this.maxSlowFrames) {
						this.reduceFPS();
					}
				} else {
					++this.goodFrames;
					if (this.maxGoodFrames > 0
							&& this.goodFrames >= this.maxGoodFrames) {
						if (this.allowLockingFrameRate) {
							this.frameRateLocked = true;
						}
						this.slowFrames = 0;
						this.goodFrames = 0;
					}
				}
			}
		}
	}

	public long getTotalFrames() {
		return this.totalFrames;
	}

	public long lastFrameStartTime() {
		return this.previousFrameTimestamps.getLast();
	}

	public long nanosToWaitUntilNextFrame() {
		return this.nanosToWaitUntilNextFrame(System.nanoTime());
	}

	public long nanosToWaitUntilNextFrame(final long time) {
		final long lastStartTime = this.previousFrameTimestamps.getLast();
		final long singleFrameGoalTime = lastStartTime
				+ this.currentNanosPerFrame;
		long waitTime = singleFrameGoalTime - time;
		if (this.previousFrameTimestamps.size() == this.frameHistorySize) {
			final long multiFrameGoalTime = this.previousFrameTimestamps
					.getFirst()
					+ this.frameHistorySize
					* this.currentNanosPerFrame;
			final long behind = singleFrameGoalTime - multiFrameGoalTime;
			if (behind > 0) {
				waitTime -= behind;
			}
		}

		if (waitTime < FPSRateManager.MILLION) {
			waitTime = FPSRateManager.MILLION;
		}
		return waitTime;
	}

	public void resetFrameRate() {
		this.clearTimestamps();
		this.setCurrentRateIndex(0);
		this.frameRateLocked = false;
	}

	public void setAllowLockingFrameRate(final boolean value) {
		this.allowLockingFrameRate = value;
	}

	public void setAllowReducingFrameRate(final boolean value) {
		this.allowReducingFrameRate = value;
	}

	public long sleepUntilNextFrame() {
		final long nanos = this.nanosToWaitUntilNextFrame(System.nanoTime());
		try {
			Thread.sleep(nanos / FPSRateManager.MILLION,
					(int) (nanos % FPSRateManager.MILLION));
		} catch (final InterruptedException ignored) {
		}
		return nanos;
	}

	public double targetFramesPerSecond() {
		return this.unfudgedTargetFrameRates[this.currentRateIndex];
	}

	void reduceFPS() {
		this.setCurrentRateIndex(this.currentRateIndex + 1);
		this.goodFrames = 0;
		this.slowFrames = 0;
		this.frameRateLocked = false;
	}

	void setCurrentRateIndex(final int index) {
		this.currentRateIndex = index;
		this.currentNanosPerFrame = (long) (FPSRateManager.BILLION / this.targetFrameRates[this.currentRateIndex]);
	}

}
