package com.lehmann.pewpew.boards;

import com.badlogic.gdx.physics.box2d.Body;
import com.lehmann.pewpew.BaseFieldDelegate;
import com.lehmann.pewpew.Field;
import com.lehmann.pewpew.box2d.AgrupamentoElementos;
import com.lehmann.pewpew.box2d.BarreiraForca;
import com.lehmann.pewpew.box2d.Parede;
import com.lehmann.pewpew.box2d.TrilhaElementos;

/**
 * Implementação das ações da Mesa 1.
 * 
 * @author limao
 * 
 */
public class Mesa1Delegate extends BaseFieldDelegate {

	@Override
	public void allDropTargetsInGroupHit(final Field field,
			final AgrupamentoElementos targetGroup) {
		final String id = targetGroup.getElementID();
		if ("BloqueioEsquerda".equals(id)) {
			((Parede) field.getFieldElementByID("ParedeTempEsquerda"))
					.setRetracted(false);
			field.showGameMessage("Bloqueio esquerdo ativado", 1500);
		} else if ("BloqueioDireita".equals(id)) {
			((Parede) field.getFieldElementByID("ParedeTempDireita"))
					.setRetracted(false);
			field.showGameMessage("Bloqueio direito ativado", 1500);
		}
		final TrilhaElementos extraBallRollovers = (TrilhaElementos) field
				.getFieldElementByID("TrilhaBolaExtra");
		if (!extraBallRollovers.allRolloversActive()) {
			extraBallRollovers.activateFirstUnactivatedRollover();
			if (extraBallRollovers.allRolloversActive()) {
				field.showGameMessage("Bola extra disponível", 1500);
			}
		}
	}

	@Override
	public void allRolloversInGroupActivated(final Field field,
			final TrilhaElementos rolloverGroup) {
		rolloverGroup.setAllRolloversActivated(false);
		field.getGameState().incrementScoreMultiplier();
		field.showGameMessage(field.getGameState().getScoreMultiplier()
				+ "x", 1500);

		if ("RampaBolaExtra".equals(rolloverGroup.getElementID())) {
			final TrilhaElementos extraBallRollovers = (TrilhaElementos) field
					.getFieldElementByID("TrilhaBolaExtra");
			if (extraBallRollovers.allRolloversActive()) {
				extraBallRollovers.setAllRolloversActivated(false);
				this.startMultiball(field);
			}
		}
	}

	@Override
	public void ballInSensorRange(final Field field,
			final BarreiraForca sensor, final Body ball) {
		if ("SensorLancamento".equals(sensor.getElementID())) {
			this.setLaunchBarrierEnabled(field, true);
		} else if ("SensorRetracao".equals(sensor.getElementID())) {
			this.setLaunchBarrierEnabled(field, false);
		}
	}

	@Override
	public void ballLost(final Field field) {
		this.setLaunchBarrierEnabled(field, false);
	}

	@Override
	public void gameStarted(final Field field) {
		this.setLaunchBarrierEnabled(field, false);
	}

	void setLaunchBarrierEnabled(final Field field, final boolean enabled) {
		final Parede barrier = (Parede) field
				.getFieldElementByID("BarreiraLancamento");
		barrier.setRetracted(!enabled);
	}

	void startMultiball(final Field field) {
		field.showGameMessage("Multiplas bolas!", 2000);
		final Runnable launchBall = new Runnable() {
			@Override
			public void run() {
				if (field.getBalls().size() < 3) {
					field.launchBall();
				}
			}
		};
		field.scheduleAction(1000, launchBall);
		field.scheduleAction(3500, launchBall);
	}

}
