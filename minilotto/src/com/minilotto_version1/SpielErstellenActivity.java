package com.minilotto;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.minilotto_version1.R;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;

public class SpielErstellenActivity extends ActionBarActivity {
	

	// ------------------------------------------------------------------------ Deklarationen
	
	
	private TextView logInfo;
	public EditText maxSpieler,einsatz;
	
	public Button erstellen;
	
	public int anzahlAktuellerSpieler,schwierigRate,anzahlMaxSpieler,spielID;
	public double einsatzGeldDoubleWert;
	public BigDecimal totalGeld,einsatzGeld; 
	
	

	// ------------------------------------------------------------------------ onCreate -> new_spiel_erstellen
		/*
		 * layout wird gestartet, EditText, Buttons und co. werden zugeordnet.
		 */
			
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_spiel_erstellen);
		
		showInformation();// TextView5(Botton TextView) Erscheint (ID : Username) des Login (Spieler)
		
		erstellen = (Button) findViewById(R.id.btnErstellen);	
		erstellen.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				spielEigenschaftRechnen();
		
		
					Intent EinfachSpielActivity = new Intent (SpielErstellenActivity.this, SpielLottoActivity.class);
					EinfachSpielActivity.putExtra("SpielInformationen", packenSpielInformationen());					
					EinfachSpielActivity.putExtra("LogInformationen", packenLoginInformationen());
					
					startActivity(EinfachSpielActivity);
			
				
			}	
		});
	}
	
	
	// ------------------------------------------------------------------------ spielEigenschaftRechnen()
		/*
		 * Konvertierung der Daten vom Nutzer --> Datenformat eingeben
		 */
			
	

	public void spielEigenschaftRechnen()
	{
		//Toast.makeText(this, "SpielEigenschaft_Rechnen Anfang", Toast.LENGTH_LONG).show();
		
		maxSpieler = (EditText) findViewById(R.id.editMaxSpieler);
		einsatz = (EditText) findViewById(R.id.editEinsatz);
		if ((maxSpieler.getText().toString().equals(""))||(einsatz.getText().toString().equals("")))
		{
			fehlermeldung("Sie müssen alle Informationen eingeben");
		}
		
		else 
		{
			this.anzahlMaxSpieler = Integer.valueOf(maxSpieler.getText().toString());
			this.anzahlAktuellerSpieler = 1;
			this.einsatzGeldDoubleWert =Double.valueOf(einsatz.getText().toString());
			this.einsatzGeld = BigDecimal.valueOf(einsatzGeldDoubleWert);
			double y = this.anzahlMaxSpieler* einsatzGeldDoubleWert ;
			this.totalGeld = BigDecimal.valueOf(y);
			 
			this.schwierigRate=1;
			
			
			
			//Toast.makeText(this, "SpielEigenschaft_Rechnen Ende", Toast.LENGTH_LONG).show();
		}
		
	}
	
	

	// ------------------------------------------------------------------------ showInformation()
	/*
	 * Anzeigen der Informationen
	 */
		
	
	
	public void showInformation()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromLogin = callerIntent.getBundleExtra("LogInformationen");
		Bundle packageMaxSpielID = callerIntent.getBundleExtra("MaxSpielID_Paket");
		int LogID = packageFromLogin.getInt("ID_Login");
		String Username= packageFromLogin.getString("Username_Login");
		int MaxSpielID = packageMaxSpielID.getInt("MaxSpielID");
		
		logInfo = (TextView) findViewById(R.id.textView5);		
		logInfo.setText("Eingeloggt mit:  "+Username);
	}
	
	// ------------------------------------------------------------------------ packenSpielInformationen()
		/*
		 * Spielinformationen werden gepackt (Bundle)
		 */
			
	
	
	public Bundle packenSpielInformationen() {
		Intent callerIntent = getIntent(); 
		Bundle packageMaxSpielID = callerIntent.getBundleExtra("MaxSpielID_Paket");
		int MaxSpielID = packageMaxSpielID.getInt("MaxSpielID");
		
		Bundle SpielInfor = new Bundle();
		SpielInfor.putInt("ID_Spiel", this.spielID);
		SpielInfor.putInt("SchwerRate", this.schwierigRate);
		SpielInfor.putInt("MaxSpieler", this.anzahlMaxSpieler);
		SpielInfor.putInt("ActuellSpieler", this.anzahlAktuellerSpieler);
		SpielInfor.putDouble("EinSatzGeld_DoubleWert",this.einsatzGeldDoubleWert);
		SpielInfor.putInt("MaxSpielID", MaxSpielID);
		return SpielInfor;
	}
	
	// ------------------------------------------------------------------------ Fehlermeldung
	/*
	 * Generierung der Fehlermeldung
	 */
		
	

	public void fehlermeldung(String Meldung) {
		AlertDialog.Builder b = new AlertDialog.Builder(
				SpielErstellenActivity.this);
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
