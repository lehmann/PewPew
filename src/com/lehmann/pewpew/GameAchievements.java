package com.lehmann.pewpew;

import android.content.Context;

import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

public class GameAchievements {

	private GoogleApiClient client;
	private GameState gameState;

	public GameAchievements(Context ctx, GameState gameState) {
		this.gameState = gameState;
		this.client = new GoogleApiClient.Builder(ctx).addApi(Plus.API)
				.addApi(Games.API).addApi(AppStateManager.API)
				.addScope(Games.SCOPE_GAMES).addScope(Plus.SCOPE_PLUS_LOGIN)
				.addScope(AppStateManager.SCOPE_APP_STATE).build();
		client.connect();
	}

	public void extraBall() {
		Games.Achievements
				.unlock(client, Achievements.FIRST_EXTRA_BALL.getId());
	}

	public void newScore() {
		Games.Leaderboards.submitScore(client, Leaderboards.RANKING.getId(),
				gameState.getScore());
	}

	public void newScoreMultiplier() {
		switch (gameState.scoreMultiplier) {
			case 2: {
				Games.Achievements.unlock(client,
						Achievements.DOUBLE_POINTS.getId());
				break;
			}
			case 3: {
				Games.Achievements.unlock(client,
						Achievements.TRIPLE_POINTS.getId());
				break;
			}
			case 4: {
				Games.Achievements.unlock(client,
						Achievements.FOURTY_POINTS.getId());
				break;
			}
			case 5: {
				Games.Achievements.unlock(client,
						Achievements.MAMBO_NUMBER_FIVE.getId());
				break;
			}
			default:
				return;
		}
	}
}
