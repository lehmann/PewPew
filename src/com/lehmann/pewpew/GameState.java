package com.lehmann.pewpew;

public class GameState {

	boolean gameInProgress;

	int ballNumber;
	int extraBalls;
	int totalBalls = 3;

	long score;
	int scoreMultiplier;
	int bonusScore;
	int bonusMultiplier;
	boolean multiplierHeld;
	GameAchievements achievements;

	public void addExtraBall() {
		achievements.extraBall();
		++this.extraBalls;
	}

	public void addScore(final long points) {
		this.score += points * this.scoreMultiplier;
		achievements.newScore();
	}

	public void doNextBall() {
		if (this.multiplierHeld) {
			this.multiplierHeld = false;
		} else {
			this.scoreMultiplier = 1;
			this.bonusMultiplier = 1;
		}

		if (this.extraBalls > 0) {
			--this.extraBalls;
		} else if (this.ballNumber < this.totalBalls) {
			++this.ballNumber;
		} else {
			this.gameInProgress = false;
		}
	}

	public int getBallNumber() {
		return this.ballNumber;
	}

	public int getExtraBalls() {
		return this.extraBalls;
	}

	public long getScore() {
		return this.score;
	}

	public int getScoreMultiplier() {
		return this.scoreMultiplier;
	}

	public int getTotalBalls() {
		return this.totalBalls;
	}

	public void incrementScoreMultiplier() {
		++this.scoreMultiplier;
		achievements.newScoreMultiplier();
	}

	public boolean isGameInProgress() {
		return this.gameInProgress;
	}

	public boolean isMultiplierHeld() {
		return this.multiplierHeld;
	}

	public void setBallNumber(final int ballNumber) {
		this.ballNumber = ballNumber;
	}

	public void setExtraBalls(final int extraBalls) {
		this.extraBalls = extraBalls;
	}

	public void setGameInProgress(final boolean gameInProgress) {
		this.gameInProgress = gameInProgress;
	}

	public void setMultiplierHeld(final boolean value) {
		this.multiplierHeld = value;
	}

	public void setScore(final long score) {
		this.score = score;
	}

	public void setScoreMultiplier(final int scoreMultiplier) {
		this.scoreMultiplier = scoreMultiplier;
	}

	public void setTotalBalls(final int totalBalls) {
		this.totalBalls = totalBalls;
	}
	
	public void setGameAchievements(GameAchievements gameAchievements) {
		this.achievements = gameAchievements;
	}

	public void startNewGame() {
		this.score = 0;
		this.ballNumber = 1;
		this.scoreMultiplier = 1;
		this.bonusMultiplier = 1;
		this.multiplierHeld = false;
		this.gameInProgress = true;
	}

}
