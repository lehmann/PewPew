package com.lehmann.pewpew;

import java.util.List;

import com.badlogic.gdx.physics.box2d.Body;
import com.lehmann.pewpew.Field.Delegate;
import com.lehmann.pewpew.box2d.AgrupamentoElementos;
import com.lehmann.pewpew.box2d.Alavanca;
import com.lehmann.pewpew.box2d.BarreiraForca;
import com.lehmann.pewpew.box2d.BasicElement;
import com.lehmann.pewpew.box2d.TrilhaElementos;

/**
 * Classe base para todas as implementações de mesas para as partidas.
 * Esta classe é só uma classe para auxiliar na implementação das mesas, permitindo que a mesa concreta não tenha que sobrescrever todos os métodos da interface {@link Delegate}
 * 
 * @author limao
 * 
 */
public class BaseFieldDelegate implements Field.Delegate {

	@Override
	public void allDropTargetsInGroupHit(final Field field,
			final AgrupamentoElementos targetGroup) {
	}

	@Override
	public void allRolloversInGroupActivated(final Field field,
			final TrilhaElementos rolloverGroup) {
	}

	@Override
	public void ballInSensorRange(final Field field,
			final BarreiraForca sensor, final Body ball) {
	}

	@Override
	public void ballLost(final Field field) {
	}

	@Override
	public void flippersActivated(final Field field,
			final List<Alavanca> flippers) {
	}

	@Override
	public void gameEnded(final Field field) {
	}

	@Override
	public void gameStarted(final Field field) {
	}

	@Override
	public boolean isFieldActive(final Field field) {
		return false;
	}

	@Override
	public void processCollision(final Field field, final BasicElement element,
			final Body hitBody, final Body ball) {
	}

	@Override
	public void tick(final Field field, final long nanos) {
	}
}
