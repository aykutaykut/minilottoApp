package com.minilotto_version1;

import android.support.v7.app.ActionBarActivity;

import java.math.BigDecimal;
import java.util.ArrayList;




import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SpielUebersichtActivity extends ActionBarActivity implements funktion{

	public TextView Information;
	
	ArrayList<String> arrSpiel = new ArrayList<String>();
	ArrayList<String> arrSpiel_copy = new ArrayList<String>(); ///#################################################
	ArrayAdapter<String> adapter = null;


	private Button NewSpiel, MitSpiel;
	private ListView lvSpiel;
	
	public int Spiel_ID, Schwer_Rate, Max_Spieler, Actuell_Spieler, MaxSpielID = 0;;
	public BigDecimal EinSatz_Geld;
	public double EinSatz_Geld_DoubleWert;
	public boolean mit_spielen_verboten;
	public String Gewinner="Ohne";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spielen_fernster);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		Information = (TextView) findViewById(R.id.txtInformation);		
		lvSpiel = (ListView) findViewById(R.id.ListView);
		
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arrSpiel_copy);
		lvSpiel.setAdapter(adapter);
		
		
		
		//Email_Lesen();
		doGetList();  
		lvSpiel.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				arrSpiel_copy.get(position);			
				InformationenVonString(arrSpiel.get(position));
				String list_LoginID = String.valueOf((getList_LoginID_die_das_Spiel_Schon_gespielt_haben()));
				Information.setText((InformationenVonString(arrSpiel.get(position)))+"\n"+list_LoginID); 

			}
		});

		NewSpiel = (Button) findViewById(R.id.btnNeuSpiel);
		MitSpiel = (Button) findViewById(R.id.btnMitspielen);
		
//******************************************************************************************************
		NewSpiel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent NewSpielFernsterActivity = new Intent(SpielFernsterActivity.this,NewSpielFernsterActivity.class);
			
				// Informationen von Login Activity wird hier weiter geliefert.
				NewSpielFernsterActivity.putExtra("LogInformationen", Packen_LoginInformationen());
				NewSpielFernsterActivity.putExtra("MaxSpielID_Paket", MaxSpielID_Paket());
				
				startActivity(NewSpielFernsterActivity);

			}
		});

		
		
//************************************************************************************************************
		MitSpiel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (Spieler_hat_schon_ein_Vorrat_gegeben()==true)
				{FehlerMeldung("Error");}
				else 
				{
					if (Actuell_Spieler == Max_Spieler)
					{
						FehlerMeldung("Error");
					}
					else{			
																	
					
						Intent EinfachSpielActivity = new Intent(SpielFernsterActivity.this,EinfachSpielActivity.class);
						
						// Informationen von Spiel Activity wird hier weiter geliefert
						EinfachSpielActivity.putExtra("SpielInformationen", Packen_SpielInformationen());					
						// Informationen von Login Activity wird hier weiter geliefert.
						EinfachSpielActivity.putExtra("LogInformationen", Packen_LoginInformationen());
						
						startActivity(EinfachSpielActivity);
						
					}
				}
				
				/*
				case 2:
					Intent MittleSpielActivity = new Intent(SpielFernsterActivity.this,MittleSpielActivity.class);
					
					// Informationen von Spiel Activity wird hier weiter geliefert
					MittleSpielActivity.putExtra("SpielInformationen", Packen_SpielInformationen());					
					// Informationen von Login Activity wird hier weiter geliefert.
					MittleSpielActivity.putExtra("LogInformationen", Packen_LoginInformationen());
					
					startActivity(MittleSpielActivity);
					break;
				case 3:
					Intent SchwerSpielActivity = new Intent(SpielFernsterActivity.this,SchwerSpielActivity.class);
					
					// Informationen von Spiel Activity wird hier weiter geliefert
					SchwerSpielActivity.putExtra("SpielInformationen", Packen_SpielInformationen());					
					// Informationen von Login Activity wird hier weiter geliefert.
					SchwerSpielActivity.putExtra("LogInformationen", Packen_LoginInformationen());
					
					startActivity(SchwerSpielActivity);
					
					*/
			//		break;
			//	default: 
			//		FehlerMeldung("Wählen Sie bitte ein Spiel!");
					
				}
		
		});
	}


	
	public String InformationenVonString(String VerarbeiteteInformationen) {
		VerarbeiteteInformationen.trim();
		String[] arr = VerarbeiteteInformationen.split(" ");
		
		this.Spiel_ID = Integer.valueOf(arr[0]);
		this.Schwer_Rate = Integer.valueOf(arr[1]);
		this.Max_Spieler = Integer.valueOf(arr[2]);
		this.Actuell_Spieler = Integer.valueOf(arr[3]);
		this.EinSatz_Geld_DoubleWert = Double.valueOf(arr[4]);
		this.EinSatz_Geld = BigDecimal.valueOf(EinSatz_Geld_DoubleWert);

		String SID = String.valueOf(this.Spiel_ID);
		String SR = String.valueOf(this.Schwer_Rate);
		String MS = String.valueOf(this.Max_Spieler);
		String AS = String.valueOf(this.Actuell_Spieler);
		String EG = String.valueOf(this.EinSatz_Geld_DoubleWert);
		
		return ("Spieler:\t" + AS + "/" +MS + "\tGeldeinsatz: " + EG);
			
	}
	
//*******************************************************************************************************	

// Alles SpielInformationen packen um weiter zu liefern  	
	public Bundle Packen_SpielInformationen()
    {
    	Bundle SpielInfor = new Bundle();
    	SpielInfor.putInt("ID_Spiel", this.Spiel_ID);
    	SpielInfor.putInt("SchwerRate", this.Schwer_Rate);
    	SpielInfor.putInt("MaxSpieler", this.Max_Spieler);
    	SpielInfor.putInt("ActuellSpieler", this.Actuell_Spieler + 1); 
    	SpielInfor.putDouble("EinSatzGeld_DoubleWert", this.EinSatz_Geld_DoubleWert);
    	SpielInfor.putInt("MaxSpielID", this.MaxSpielID);
		return SpielInfor;
    }
	
//*****************************************************************************************************	

// Informationen von Login Activity wird hier geholt.
	public Bundle Packen_LoginInformationen()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromCaller = callerIntent.getBundleExtra("LogInformationen");
		return packageFromCaller;
	}
//****************************************************************************************************	
	public Bundle MaxSpielID_Paket()
	{Bundle MaxSpielID_P 	= new Bundle(); MaxSpielID_P.putInt("MaxSpielID", this.MaxSpielID); return MaxSpielID_P;}
	
//*********************************************************************************************************

	
//*********************************************************************************************************
//---------------------------------------------------------------------------------------------------------
	public boolean Spieler_hat_schon_ein_Vorrat_gegeben()
	{
		if (this.mit_spielen_verboten == true){return true;}
		else return false;
	}
	
//**********************************************************************************************************
//-------------------------------------- FehlerMeldung -----------------------------------------------------
	
	public void FehlerMeldung(String Meldung)
	{
		AlertDialog.Builder b = new AlertDialog.Builder(SpielFernsterActivity.this);
		b.setMessage(Meldung);
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
			}
		});
		b.create().show();
	}

interface funktion
{
	public String Email_Lesen(String SpielID);
}
