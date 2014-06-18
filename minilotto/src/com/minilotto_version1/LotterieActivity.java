package com.minilotto_version1;

import java.util.Random;



import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LotterieActivity extends Activity implements
		SensorEventListener {

	private final float SCHUETTEL_SCHWELLWERT = 2.0f;

	private final int X_ACHSE = 0;
	private final int Y_ACHSE = 1;
	private final int Z_ACHSE = 2;

	private TextView textView1;
	private TextView textView2;
	private TextView textView3;
	

	private Random zufallsGenerator;

	private SensorManager sensorManager;

	private boolean neuGestartet = true;
	private boolean bewegt = false;

	private float xErsteBeschleunigung;
	private float yErsteBeschleunigung;
	private float zErsteBeschleunigung;

	private float xLetzteBeschleunigung;
	private float yLetzteBeschleunigung;
	private float zLetzteBeschleunigung;

	private boolean geschuettelt = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dreschen_lassen);

		textView1 = (TextView) findViewById(R.id.txt_text_teil1);
		textView2 = (TextView) findViewById(R.id.txt_text_teil2);
		textView3 = (TextView) findViewById(R.id.txt_text_teil3);
		

		zufallsGenerator = new Random();

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	public void onButtonClick(View view) {

		erzeugePhrase();

	}

	private void erzeugePhrase() {

		final String[] textTeil1 = getResources().getStringArray(
				R.array.Zahl1);
		final String[] textTeil2 = getResources().getStringArray(
				R.array.Zahl2);
		final String[] textTeil3 = getResources().getStringArray(
				R.array.Zahl3);
		

		textView1
				.setText(textTeil1[zufallsGenerator.nextInt(textTeil1.length)]);
		textView2
				.setText(textTeil2[zufallsGenerator.nextInt(textTeil2.length)]);
		textView3
				.setText(textTeil3[zufallsGenerator.nextInt(textTeil3.length)]);
		
	}

	public void onSensorChanged(SensorEvent event) {
		aktualisiereBeschleunigungswerte(event.values[X_ACHSE],
				event.values[Y_ACHSE], event.values[Z_ACHSE]);

		bewegt = bewegenErkannt();

		if (bewegt && !geschuettelt) {
			geschuettelt = true;
		} else if (bewegt && geschuettelt) {
			erzeugePhrase();
		} else if (!bewegt && geschuettelt) {
			geschuettelt = false;
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// nichts tun
	}

	protected void aktualisiereBeschleunigungswerte(
			float xAktuelleBeschleunigung, float yAktuelleBeschleunigung,
			float zAktuelleBeschleunigung) {

		if (neuGestartet) {
			// da die Werte zu Beginn jeweils auf 0 gesetzt werden,
			// warten wir die ersten tatsŠchlichen Daten vom
			// SensorManager ab
			xErsteBeschleunigung = xAktuelleBeschleunigung;
			yErsteBeschleunigung = yAktuelleBeschleunigung;
			zErsteBeschleunigung = zAktuelleBeschleunigung;

			neuGestartet = false;
		} else {
			// bei jedem weiteren Aufruf verwerfen wir die ersten
			// Beschleunigungswerte und ersetzen sie durch die
			// letzten Beschleunigungswerte
			xErsteBeschleunigung = xLetzteBeschleunigung;
			yErsteBeschleunigung = yLetzteBeschleunigung;
			zErsteBeschleunigung = zLetzteBeschleunigung;
		}

		// jetzt werden die aktuellen Werte zwischengespeichert
		xLetzteBeschleunigung = xAktuelleBeschleunigung;
		yLetzteBeschleunigung = yAktuelleBeschleunigung;
		zLetzteBeschleunigung = zAktuelleBeschleunigung;

	}

	protected boolean bewegenErkannt() {

		final float xDifferenz = Math.abs(xErsteBeschleunigung
				- xLetzteBeschleunigung);
		final float yDifferenz = Math.abs(yErsteBeschleunigung
				- yLetzteBeschleunigung);
		final float zDifferenz = Math.abs(zErsteBeschleunigung
				- zLetzteBeschleunigung);

		return (xDifferenz > SCHUETTEL_SCHWELLWERT
				|| yDifferenz > SCHUETTEL_SCHWELLWERT || zDifferenz > SCHUETTEL_SCHWELLWERT);
	}
}
