package com.lehmann.pewpew;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationListener implements SensorEventListener {

	public static interface Delegate {

		public void receivedOrientationValues(float azimuth, float pitch,
				float roll);
	}

	private int rate;
	private Delegate delegate;
	private SensorManager sensorManager;

	private final float[] R = new float[16];
	private final float[] I = new float[16];
	private float[] mags = null;
	private float[] accels = null;
	private final float[] orientationValues = { 0f, 0f, 0f };

	public OrientationListener(final Context context, final int rate,
			final Delegate delegate) {
		this.rate = rate;
		this.delegate = delegate;
		this.sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		switch (event.sensor.getType()) {
			case Sensor.TYPE_MAGNETIC_FIELD:
				this.mags = event.values.clone();
				break;
			case Sensor.TYPE_ACCELEROMETER:
				this.accels = event.values.clone();
				break;
		}

		if (this.mags != null && this.accels != null) {
			SensorManager.getRotationMatrix(this.R, this.I, this.accels,
					this.mags);
			SensorManager.getOrientation(this.R, this.orientationValues);
			//calculando o azimute
			this.delegate.receivedOrientationValues(this.orientationValues[0],
					this.orientationValues[1], this.orientationValues[2]);
		}
	}

	public void start() {
		this.sensorManager.registerListener(this,
				this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				this.rate);
		this.sensorManager
				.registerListener(this, this.sensorManager
						.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
						this.rate);
	}

	public void stop() {
		this.sensorManager.unregisterListener(this);
	}

}
