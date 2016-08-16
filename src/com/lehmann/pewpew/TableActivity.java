package com.lehmann.pewpew;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

/**
 * Principal {@link Activity} do app. É a responsável por gerenciar o mundo do
 * Box2D, sabe onde desenhar todas as figuras, cuida do estado do jogo e realiza
 * todas as chamadas à API de som.
 * 
 * @author limao
 * 
 */
public class TableActivity extends Activity {

	// libgdx 0.7 não mais carrega a biblioteca nativa sozinho
	static {
		System.loadLibrary("gdx");
	}

	private CanvasFieldView canvasFieldView;
	private ScoreView scoreView;

	private View buttonPanel;
	private MenuItem aboutMenuItem;
	private MenuItem endGameMenuItem;
	private MenuItem preferencesMenuItem;
	private static final int ACTIVITY_PREFERENCES = 1;

	private final Handler handler = new Handler();

	private final Runnable callTick = new Runnable() {
		@Override
		public void run() {
			TableActivity.this.tick();
		}
	};

	private final Field field = new Field();
	private int level = 1;
	private long highScore = 0;
	private static final String HIGHSCORE_PREFS_KEY = "highScore";
	private static final String INITIAL_LEVEL_PREFS_KEY = "initialLevel";
	private boolean useZoom = true;

	private static final long END_GAME_DELAY = 1000;
	private long endGameTime = System.currentTimeMillis()
			- TableActivity.END_GAME_DELAY;

	private final FieldDriver fieldDriver = new FieldDriver();
	private final FieldViewManager fieldViewManager = new FieldViewManager();
	private OrientationListener orientationListener;

	public void doAbout(final View view) {
		this.gotoAbout();
	}

	public void doPreferences(final View view) {
		this.gotoPreferences();
	}

	public void doStartGame(final View view) {
		if (System.currentTimeMillis() < this.endGameTime
				+ TableActivity.END_GAME_DELAY) {
			return;
		}
		this.buttonPanel.setVisibility(View.GONE);
		this.field.resetForLevel(this, this.level);
		this.field.startGame();
		SoundsHelper.playStart();
	}

	public void doSwitchTable(final View view) {
		this.level = this.level == FieldLayout.numberOfLevels() ? 1
				: this.level + 1;
		synchronized (this.field) {
			this.field.resetForLevel(this, this.level);
		}
		this.setInitialLevel(this.level);
		this.highScore = this.highScoreFromPreferencesForCurrentLevel();
		this.scoreView.setHighScore(this.highScore);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.main);

		FieldLayout.setContext(this);
		this.level = this.getInitialLevel();
		this.field.resetForLevel(this, this.level);
		this.field.setGameAchievements(new GameAchievements(this, field.getGameState()));

		this.canvasFieldView = (CanvasFieldView) this
				.findViewById(R.id.canvasFieldView);

		this.fieldViewManager.setField(this.field);
		this.fieldViewManager.setStartGameAction(new Runnable() {
			@Override
			public void run() {
				TableActivity.this.doStartGame(null);
			}
		});

		this.scoreView = (ScoreView) this.findViewById(R.id.scoreView);
		this.scoreView.setField(this.field);

		this.fieldDriver.setFieldViewManager(this.fieldViewManager);
		this.fieldDriver.setField(this.field);

		this.highScore = this.highScoreFromPreferencesForCurrentLevel();
		this.scoreView.setHighScore(this.highScore);

		this.buttonPanel = this.findViewById(R.id.buttonPanel);

		this.updateFromPreferences();
		SoundsHelper.initSounds(this);
		SoundsHelper.loadSounds();

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.aboutMenuItem = menu.add(R.string.about_menu_item);
		this.endGameMenuItem = menu.add(R.string.end_game_menu_item);
		this.preferencesMenuItem = menu.add(R.string.preferences_menu_item);
		return true;
	}

	@Override
	public void onDestroy() {
		SoundsHelper.cleanup();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item == this.aboutMenuItem) {
			this.gotoAbout();
		} else if (item == this.endGameMenuItem) {
			this.field.endGame();
		} else if (item == this.preferencesMenuItem) {
			this.gotoPreferences();
		}
		return true;
	}

	@Override
	public void onWindowFocusChanged(final boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (!hasWindowFocus) {
			if (this.orientationListener != null) {
				this.orientationListener.stop();
			}

			this.fieldDriver.stop();
			SoundsHelper.pauseMusic();
		} else {
			this.handler.postDelayed(this.callTick, 75);
			if (this.orientationListener != null) {
				this.orientationListener.start();
			}

			this.fieldDriver.start();
		}
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
			case ACTIVITY_PREFERENCES:
				this.updateFromPreferences();
				break;
		}
	}

	int getInitialLevel() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getBaseContext());
		int startLevel = prefs.getInt(TableActivity.INITIAL_LEVEL_PREFS_KEY, 1);
		if (startLevel < 1 || startLevel > FieldLayout.numberOfLevels()) {
			startLevel = 1;
		}
		return startLevel;
	}

	void gotoAbout() {
		AboutActivity.startForLevel(this, this.level);
	}

	void gotoPreferences() {
		final Intent settingsActivity = new Intent(this.getBaseContext(),
				TablePreferences.class);
		this.startActivityForResult(settingsActivity,
				TableActivity.ACTIVITY_PREFERENCES);
	}

	long highScoreFromPreferences(final int theLevel) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getBaseContext());
		long score = prefs.getLong(this.highScorePrefsKeyForLevel(theLevel), 0);
		if (score == 0 && theLevel == 1) {
			score = prefs.getLong(TableActivity.HIGHSCORE_PREFS_KEY, 0);
		}
		return score;
	}

	long highScoreFromPreferencesForCurrentLevel() {
		return this.highScoreFromPreferences(this.level);
	}

	String highScorePrefsKeyForLevel(final int theLevel) {
		return TableActivity.HIGHSCORE_PREFS_KEY + "." + theLevel;
	}

	void setInitialLevel(final int level) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getBaseContext());
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(TableActivity.INITIAL_LEVEL_PREFS_KEY, level);
		editor.commit();
	}

	void tick() {
		this.scoreView.invalidate();
		this.scoreView.setFPS(this.fieldDriver.getAverageFPS());
		this.updateHighScoreAndButtonPanel();
		this.handler.postDelayed(this.callTick, 100);
	}

	void updateFromPreferences() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getBaseContext());
		this.scoreView.setShowFPS(prefs.getBoolean("showFPS", false));

		final boolean previousHighQuality = this.fieldViewManager
				.isHighQuality();
		this.fieldViewManager.setHighQuality(prefs.getBoolean("highQuality",
				false));
		if (previousHighQuality != this.fieldViewManager.isHighQuality()) {
			this.fieldDriver.resetFrameRate();
		}

		if (this.canvasFieldView.getVisibility() != View.VISIBLE) {
			this.canvasFieldView.setVisibility(View.VISIBLE);
			this.fieldViewManager.setFieldView(this.canvasFieldView);
			this.fieldDriver.resetFrameRate();
		}

		this.useZoom = prefs.getBoolean("zoom", true);
		this.fieldViewManager.setZoom(this.useZoom ? 1.4f : 1.0f);

		SoundsHelper.setSoundEnabled(prefs.getBoolean("sound", true));
		SoundsHelper.setMusicEnabled(prefs.getBoolean("music", true));
	}

	void updateHighScore(final int theLevel, final long score) {
		this.highScore = score;
		this.scoreView.setHighScore(score);
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getBaseContext());
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(this.highScorePrefsKeyForLevel(theLevel), score);
		editor.commit();
	}

	void updateHighScoreAndButtonPanel() {
		if (this.buttonPanel.getVisibility() == View.VISIBLE) {
			return;
		}
		synchronized (this.field) {
			if (!this.field.getGameState().isGameInProgress()) {
				this.endGameTime = System.currentTimeMillis();
				this.buttonPanel.setVisibility(View.VISIBLE);

				final long score = this.field.getGameState().getScore();
				if (score > this.highScore) {
					this.updateHighScoreForCurrentLevel(score);
				}
			}
		}
	}

	void updateHighScoreForCurrentLevel(final long score) {
		this.updateHighScore(this.level, score);
	}
}