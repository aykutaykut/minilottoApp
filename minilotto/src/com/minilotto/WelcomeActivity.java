package com.minilotto;

import com.minilotto.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WelcomeActivity extends Activity {

	// ------------------------------------------------------------------------ Deklarationen
	
	public Button spielen, information;
	public TextView SPHinweis_PrivateInfor, textHinweis;

	
	// ------------------------------------------------------------------------ onCreate -> wellcome_layout
	/*
	 * layout wird gestartet, EditText, Buttons und co. werden zugeordnet.
	 */
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_layout);

		spielen = (Button) findViewById(R.id.btnMinilotto_Spielen);
		information = (Button) findViewById(R.id.btnPrivate_Informationen);
		

		SPHinweis_PrivateInfor = (TextView) findViewById(R.id.txtSpiel_Hinweis);
		textHinweis = (TextView) findViewById(R.id.txtHinweis);
		
		if (packenLoginInformationen().getString("Message").equals("#")){}
		else{fehlermelung(packenLoginInformationen().getString("Message"));}

		spielen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent SpielFernsterActivity = new Intent(
						WelcomeActivity.this, SpielUebersichtActivity.class);

				SpielFernsterActivity.putExtra("LogInformationen",
						packenLoginInformationen());

				startActivity(SpielFernsterActivity);

			}
		});

		textHinweis.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SPHinweis_PrivateInfor
						.setText("Spielablauf:\n"
								+ "Du eröffnest ein Spiel oder trittst in eins ein\n"
								+ "Danach gibst du deinen Tipp ab.\n"
								+ "Nun wartest du auf die Abgabe\nder Tipps der anderen Mitspieler\n"
								+ "Mit dem Aktualisieren Button kannst \ndu dir aktuelle Informationen abrufen.\n"
								+ "Dort siehst du wer mitspielt, und wer welche Zahlen getippt hat\n"
								+ "\n Beachte! Spielen kann süchtig machen!"
								+ "\n\n Viel Glück wünschen dir \n Bui\n Ince\n Graffenberger\n und Mitchell");
				
			}
		});

		information.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int LogID = packenLoginInformationen().getInt("ID_Login");
				String Username = packenLoginInformationen().getString(
						"Username_Login");
				String Passwords = packenLoginInformationen().getString(
						"Passwords_Login");
				String Email = packenLoginInformationen().getString(
						"Email_Login");
				Double Bank = packenLoginInformationen().getDouble(
						"Bank_Login");
				SPHinweis_PrivateInfor
						.setText("\nLogID:\t" + LogID + "\nUsername:\t"
								+ Username + "\nPassword:\t" + Passwords
								+ "\nEmail:\t" + Email + "\nGuthaben:\t" + Bank);
			}
		});


	}

	// ------------------------------------------------------------------------ packenLoginInformationen()
			/*
			 * LoginInformationen packen
			 */
	
	
	public Bundle packenLoginInformationen() {
		Intent callerIntent = getIntent();
		Bundle packageFromCaller = callerIntent
				.getBundleExtra("LogInformationen");
		return packageFromCaller;
	}
	
	// ------------------------------------------------------------------------ packenLoginInformationen()
	/*
	 * Fehlermeldung generieren 	
	 * 
	 *  */
	
	
	
	public void fehlermelung(String Meldung) {
		AlertDialog.Builder b = new AlertDialog.Builder(WelcomeActivity.this);
		b.setMessage(Meldung);
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

			}
		});
		b.create().show();
	}

}
