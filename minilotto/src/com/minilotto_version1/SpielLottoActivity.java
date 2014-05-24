package com.minilotto_version1;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;

public class EinfachSpielActivity extends ActionBarActivity {
	
	public final String NAMESPACE="http://AndroidMinilottoDatabaseService.com/";
	public final String URL="http://viendatabaseservice.somee.com/Webservice.asmx?WSDL";
	
	private String Ergebnis_String;
	private String VorratErgebnis_String;
	private boolean Gewinner_Gefunden;
	private boolean einmal_ausführen_drücken=false;
	
	
	private Button ausführen,refresh;
	private EditText z1,z2,z3;
	private TextView Ergebnis;
	
	public TextView Information;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ergebnisse_layout);
		
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		ausführen = (Button) findViewById(R.id.btnAusfuehren);
		refresh = (Button) findViewById(R.id.btnRefresh);
		
		z1 = (EditText) findViewById(R.id.editZahl1);
		z2 = (EditText) findViewById(R.id.editZahl2);
		z3 = (EditText) findViewById(R.id.editZahl3);
		
		Information = (TextView) findViewById(R.id.textView2);
		Ergebnis = (TextView) findViewById(R.id.txtErgebnis_zeigen);
		
		
		AusPacken_LoginUndSpiel_Informationen(""); 	
		
//----------------------- ausführen BUTTON ------------------------------------------------------------		
		ausführen.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if ((pruefen_Ob_Alle_EditText_Erfüllen_Werden()==true)&&(einmal_ausführen_drücken==false))
				{
					RandomToFindTheRightNumber();
					if(LetzteSpieler()==true)
					{
						HashMap GewinnerID = new HashMap();
						GewinnerID= GewinnerSuchen(); //###############################################
						if (GewinnerID.get("GewinnerMenge").equals(0)){Information.setText("Es Gibt Kein Gewinnner..! ");}
						else 
						{
							for (int i=1; i <= (Integer.valueOf(GewinnerID.get("GewinnerMenge").toString()));i++)
							{
								Gewinner_NachDiesnt(Integer.valueOf(GewinnerID.get("Gewinner"+i).toString()));
							}
						}
					}
					else {Information.setText("Informationen der MitSpieler");}
				}
				else {FehlerMeldung("Alles EditText erfüllen und nur einmal ausführen Click");}											
			}
			
		});
		
//***************************************************************************************************			
		
//------------------- EXIT BUTTON -------------------------------------------------------------------	
		
		
		refresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				refresh.refreshDrawableState();	
				
				Intent callerIntent = getIntent(); 
				Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
				if (ActuellSpieler_Gleich_MaxSpieler(packageFromSpiel.getInt("ID_Spiel"))==true)//########################################################
				{
					String emailLesen=Email_Lesen(String.valueOf(packageFromSpiel.getInt("ID_Spiel")));
					AusPacken_LoginUndSpiel_Informationen(emailLesen); 
					Ergebnis.setText("Lotto Zahl: "+ get_Ergebnis_der_SpielID());
				}
				else{
					AusPacken_LoginUndSpiel_Informationen("");
					Ergebnis.setText("Vorrat= "+VorratErgebnis_String);
				}
				
			}
		});
		
	}}