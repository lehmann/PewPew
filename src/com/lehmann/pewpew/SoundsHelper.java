package com.lehmann.pewpew;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

/**
 * API responsável por iniciar, pausar e interromper todos os sons envolvidos no jogo.
 * @author limao
 *
 */
public class SoundsHelper {

	private static SoundPool soundPool;
	private static HashMap<Integer, Integer> soundPoolMap;
	private static AudioManager manager;
	private static Context ctx;
	//random para dar uma pitada de sabor ao jogo
	private static final Random RND = new Random();

	private static boolean soundEnabled = true;
	private static boolean musicEnabled = true;
	private static int score = 0;
	private static int prevDing, currentDing = 0;
	private static int andrModAmt = 10;
	private static MediaPlayer drumbass;
	private static boolean drumbassPlaying = false;
	private static MediaPlayer androidpad;

	static int ID_DING_START = 0;
	static int NUM_DINGS = 6;

	static int ID_LAUNCH = 100;
	static int ID_FLIPPER = 101;
	static int ID_MESSAGE = 102;
	static int ID_START = 103;

	static int ID_ROLLOVER = 200;

	static int ID_ANDROIDPAD = 300;
	static int ID_DRUMBASSLOOP = 301;

	public static void cleanup() {
		SoundsHelper.soundPool.release();
		SoundsHelper.soundPool = null;
		SoundsHelper.soundPoolMap.clear();
		SoundsHelper.manager.unloadSoundEffects();
		SoundsHelper.drumbass.release();
		SoundsHelper.drumbass = null;
		SoundsHelper.androidpad.release();
		SoundsHelper.androidpad = null;
	}

	public static void initSounds(final Context theContext) {
		Log.v("SoundsHelper", "initSounds");
		SoundsHelper.ctx = theContext;
		SoundsHelper.soundPool = new SoundPool(32, AudioManager.STREAM_MUSIC, 0);
		SoundsHelper.soundPoolMap = new HashMap<Integer, Integer>();
		SoundsHelper.manager = (AudioManager) SoundsHelper.ctx
				.getSystemService(Context.AUDIO_SERVICE);
	}

	public static void loadSounds() {
		SoundsHelper.soundPoolMap.clear();
		final AssetManager assets = SoundsHelper.ctx.getAssets();
		try {
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_DING_START + 0,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/bumper/dinga1.ogg"), 1));
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_DING_START + 1,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/bumper/dingc1.ogg"), 1));
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_DING_START + 2,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/bumper/dingc2.ogg"), 1));
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_DING_START + 3,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/bumper/dingd1.ogg"), 1));
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_DING_START + 4,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/bumper/dinge1.ogg"), 1));
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_DING_START + 5,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/bumper/dingg1.ogg"), 1));

			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_LAUNCH,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/misc/andBounce2.ogg"), 1));
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_FLIPPER,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/misc/flipper1.ogg"), 1));
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_MESSAGE,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/misc/message2.ogg"), 1));
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_START,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/misc/startup1.ogg"), 1));
			SoundsHelper.soundPoolMap.put(
					SoundsHelper.ID_ROLLOVER,
					SoundsHelper.soundPool.load(
							assets.openFd("audio/misc/rolloverE.ogg"), 1));

			SoundsHelper.drumbass = MediaPlayer.create(SoundsHelper.ctx,
					R.raw.drumbassloop);
			SoundsHelper.androidpad = MediaPlayer.create(SoundsHelper.ctx,
					R.raw.androidpad);

		} catch (final IOException ex) {
			Log.e("SoundsHelper", "Error loading sounds", ex);
		}
		SoundsHelper.resetMusicState();
	}

	public static void pauseMusic() {
		if (SoundsHelper.drumbass != null && SoundsHelper.drumbass.isPlaying()) {
			SoundsHelper.drumbass.pause();
		}
		if (SoundsHelper.androidpad != null
				&& SoundsHelper.androidpad.isPlaying()) {
			SoundsHelper.androidpad.pause();
		}
	}

	public static void playBall() {
		SoundsHelper.playSound(SoundsHelper.ID_LAUNCH, 1, 1);
	}

	public static void playFlipper() {
		SoundsHelper.playSound(SoundsHelper.ID_FLIPPER, 1, 1);
	}

	public static void playMessage() {
		SoundsHelper.playSound(SoundsHelper.ID_MESSAGE, 0.66f, 1);
	}

	public static void playRollover() {
		final float pitch[] = { 0.7937008f, 0.8908991f, 1f, 1.1892079f,
				1.3348408f, 1.5874025f };
		final int pitchDx[] = { 0, 0, 0 };

		for (int i = 0; i < 3; i++) {
			switch (i) {
				case 0:
					pitchDx[i] = SoundsHelper.RND.nextInt(6);
					SoundsHelper.playSound(SoundsHelper.ID_ROLLOVER, .3f,
							pitch[pitchDx[i]]);
					break;
				case 1:
					pitchDx[i] = SoundsHelper.RND.nextInt(6);
					if (pitchDx[i] != pitchDx[i - 1]) {
						SoundsHelper.playSound(SoundsHelper.ID_ROLLOVER, .3f,
								pitch[pitchDx[i]]);
					}
					break;
				case 2:
					pitchDx[i] = SoundsHelper.RND.nextInt(6);
					if (pitchDx[i] != pitchDx[i - 1]
							&& pitchDx[i] != pitchDx[i - 2]) {
						SoundsHelper.playSound(SoundsHelper.ID_ROLLOVER, .3f,
								pitch[pitchDx[i]]);
					}
					break;
				default:
					Log.e("SoundsHelper", "=(");
					break;
			}
		}
	}

	public static void playScore() {
		while (SoundsHelper.currentDing == SoundsHelper.prevDing) {
			SoundsHelper.currentDing = SoundsHelper.RND
					.nextInt(SoundsHelper.NUM_DINGS);
		}
		SoundsHelper.playSound(SoundsHelper.ID_DING_START
				+ SoundsHelper.currentDing, 0.5f, 1);
		SoundsHelper.prevDing = SoundsHelper.currentDing;

		SoundsHelper.score++;
		if (SoundsHelper.musicEnabled && SoundsHelper.score % 20 == 0
				&& SoundsHelper.drumbass != null
				&& !SoundsHelper.drumbass.isPlaying()) {
			SoundsHelper.drumbass.setVolume(1.0f, 1.0f);
			SoundsHelper.drumbass.start();
			SoundsHelper.drumbassPlaying = true;
		}
		if (SoundsHelper.musicEnabled && SoundsHelper.androidpad != null
				&& SoundsHelper.score % SoundsHelper.andrModAmt == 0) {
			SoundsHelper.androidpad.setVolume(0.5f, 0.5f);
			SoundsHelper.androidpad.start();
			SoundsHelper.andrModAmt += 42;
		}
	}

	public static void playStart() {
		SoundsHelper.resetMusicState();
		SoundsHelper.playSound(SoundsHelper.ID_START, 0.5f, 1);
	}

	public static void resetMusicState() {
		SoundsHelper.pauseMusic();
		SoundsHelper.drumbassPlaying = false;
		SoundsHelper.score = 0;
		SoundsHelper.andrModAmt = 10;
		if (SoundsHelper.drumbass != null) {
			SoundsHelper.drumbass.seekTo(0);
		}
		if (SoundsHelper.androidpad != null) {
			SoundsHelper.androidpad.seekTo(0);
		}
	}

	public static void resumeMusic() {
		if (SoundsHelper.drumbass != null && SoundsHelper.drumbassPlaying) {
			SoundsHelper.drumbass.start();
		}
	}

	public static void setMusicEnabled(final boolean enabled) {
		SoundsHelper.musicEnabled = enabled;
		if (!SoundsHelper.musicEnabled) {
			SoundsHelper.resetMusicState();
		}
	}

	public static void setSoundEnabled(final boolean enabled) {
		SoundsHelper.soundEnabled = enabled;
	}

	public static void stopMusic() {
		if (SoundsHelper.drumbass != null) {
			SoundsHelper.drumbass.stop();
		}
		SoundsHelper.drumbassPlaying = false;
		if (SoundsHelper.androidpad != null) {
			SoundsHelper.androidpad.stop();
		}
	}

	static void playSound(final int soundKey, final float volume,
			final float pitch) {
		if (SoundsHelper.soundEnabled && SoundsHelper.soundPoolMap != null) {
			final Integer soundID = SoundsHelper.soundPoolMap.get(soundKey);
			if (soundID != null) {
				SoundsHelper.soundPool.play(soundID, volume, volume, 1, 0,
						pitch);
			}
		}
	}
}
