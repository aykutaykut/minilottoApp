package com.minilotto_version1;

import android.support.v7.app.ActionBarActivity;

import java.math.BigDecimal;
import java.util.ArrayList;





import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.minilotto_version1.R;

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

public class SpielUebersichtActivity extends ActionBarActivity {

	// ------------------------------------------------------------------------ Deklarationen
	
	public TextView information;
	
	private Button neuesSpiel, mitSpielen;
	private ListView lvSpiel;
	
	public int spielID, schwierigRate, maxSpieler, aktuelleSpieler, maxSpielID = 0;;
	public BigDecimal einsatzGeld;
	public double einsatzGeldDoubleWert;
	public boolean mitspielenVerboten;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spiel_uebersicht);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		information = (TextView) findViewById(R.id.txtInformation);		
		lvSpiel = (ListView) findViewById(R.id.ListView);
		
		
	
			
	
		
		
		// ------------------------------------------------------------------------ 	lvSpiel 
		/*
		 * Spiel Informationen list view
		 */

		lvSpiel.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				arrSpiel_copy.get(position);			
				informationenVonString(arrSpiel.get(position));
				String list_LoginID = String.valueOf((getListLoginIDSpielSchonGespielt()));
				information.setText((informationenVonString(arrSpiel.get(position)))+"\n"+list_LoginID); 

			}
		});

		neuesSpiel = (Button) findViewById(R.id.btnNeuSpiel);
		mitSpielen = (Button) findViewById(R.id.btnMitspielen);
	
		
		// ------------------------------------------------------------------------ 	Button neues Spiel 
		/*
		 * Button um neues Spiel zu eröffnen
		 */
	
		neuesSpiel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent NewSpielFernsterActivity = new Intent(SpielUebersichtActivity.this,SpielErstellenActivity.class);
			
				// Informationen von Login Activity wird hier weiter geliefert.
				NewSpielFernsterActivity.putExtra("LogInformationen", packenLoginInformationen());
				NewSpielFernsterActivity.putExtra("MaxSpielID_Paket", maxSpielIDPaket());
				
				startActivity(NewSpielFernsterActivity);

			}
		});

		
		
		// ------------------------------------------------------------------------ 	Button Mitspielen 
		/*
		 * Buttom um mitzuspielen
		 * 
		 * Fälle: Wenn man schon getippt hat --> Fehlermeldung
		 * Wenn Spiel schon voll --> Fehlermeldung
		 */
		mitSpielen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (spielerHatSchonGetippt()==true)
				{fehlermeldung("Sie haben schon ein Tipp für dieses Spiel gegeben");}
				else 
				{
					if (aktuelleSpieler == maxSpieler)
					{
						fehlermeldung("Kein Platz mehr");
					}
					else{			
																	
					
						Intent EinfachSpielActivity = new Intent(SpielUebersichtActivity.this,SpielLottoActivity.class);
						
						// Informationen von Spiel Activity wird hier weiter geliefert
						EinfachSpielActivity.putExtra("SpielInformationen", packenSpielInformationen());					
						// Informationen von Login Activity wird hier weiter geliefert.
						EinfachSpielActivity.putExtra("LogInformationen", packenLoginInformationen());
						
						startActivity(EinfachSpielActivity);
						
					}
				}
				// Code um das Ganze Spiel zu erweitern und mehrere Schwierigkeitsgrade einzubauen 
	            // So lassen!
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
	
	
	// ------------------------------------------------------------------------ 	informationenVonString(...)
		/*
		 * Informationen für das Informationsfenster bekommen:
		 * Spieleranzahl Aktuelle/Maximale + Erforderliche Geldeinsatz für das Spiel
		 */
		

	public String informationenVonString(String VerarbeiteteInformationen) {
		VerarbeiteteInformationen.trim();
		String[] arr = VerarbeiteteInformationen.split(" "); 
																
																
		this.spielID = Integer.valueOf(arr[0]);
		this.schwierigRate = Integer.valueOf(arr[1]);
		this.maxSpieler = Integer.valueOf(arr[2]);
		this.aktuelleSpieler = Integer.valueOf(arr[3]);
		this.einsatzGeldDoubleWert = Double.valueOf(arr[4]);
		this.einsatzGeld = BigDecimal.valueOf(einsatzGeldDoubleWert);

		String SID = String.valueOf(this.spielID);
		String SR = String.valueOf(this.schwierigRate);
		String MS = String.valueOf(this.maxSpieler);
		String AS = String.valueOf(this.aktuelleSpieler);
		String EG = String.valueOf(this.einsatzGeldDoubleWert);
		
		return ("Spieler:\t " + AS + "/" +MS + " Erforderlicher Geldeinsatz: " + EG  + " Euro");
			
	}
	
	
	// ------------------------------------------------------------------------ 	packenSpielInformationen()
	/*
	 * Alle SpielInformationen werden gepackt --> zur Weiterlieferung
	 */
	

	public Bundle packenSpielInformationen()
    {
    	Bundle SpielInfor = new Bundle();
    	SpielInfor.putInt("ID_Spiel", this.spielID);
    	SpielInfor.putInt("SchwerRate", this.schwierigRate);
    	SpielInfor.putInt("MaxSpieler", this.maxSpieler);
    	SpielInfor.putInt("ActuellSpieler", this.aktuelleSpieler + 1);  // diser Fall wir spielen mit dh.  das Spiel schon vorher Exitiert
    	SpielInfor.putDouble("EinSatzGeld_DoubleWert", this.einsatzGeldDoubleWert);
    	SpielInfor.putInt("MaxSpielID", this.maxSpielID);
		return SpielInfor;
    }
	
	// ------------------------------------------------------------------------ 	packenLoginInformationen()
		/*
		 * Alle LoginInformationen werden gepackt --> zur Weiterlieferung
		 */
	
	public Bundle packenLoginInformationen()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromCaller = callerIntent.getBundleExtra("LogInformationen");
		return packageFromCaller;
	}
	
	// ------------------------------------------------------------------------ 	maxSpielIDPaket()
	/*
	 * MaxSpielerID packen
	 */	
	

	public Bundle maxSpielIDPaket()
	{Bundle MaxSpielID_P 	= new Bundle(); MaxSpielID_P.putInt("MaxSpielID", this.maxSpielID); return MaxSpielID_P;}
	
	

	// ------------------------------------------------------------------------ 	spielerHatSchonGetippt()
		/*
		 * Hat der Spieler schon seinen Tipp abgegeben? true false?
		 */	
	
	
	public boolean spielerHatSchonGetippt()
	{
		if (this.mitspielenVerboten == true){return true;}
		else return false;
	}
	

	// ------------------------------------------------------------------------ 	Fehlermeldung
			/*
			 * Fehlermeldung Generierung
			 */	
		
	
	public void fehlermeldung(String Meldung)
	{
		AlertDialog.Builder b = new AlertDialog.Builder(SpielUebersichtActivity.this);
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
