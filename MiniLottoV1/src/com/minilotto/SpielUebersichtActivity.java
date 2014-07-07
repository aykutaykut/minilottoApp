package com.minilotto;

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
	
	ArrayList<String> arrSpiel = new ArrayList<String>();
	ArrayList<String> arrSpiel_copy = new ArrayList<String>(); ///#################################################
	ArrayAdapter<String> adapter = null;

	final String NAMESPACE = "http://AndroidMinilottoDatabaseService.com/";
	//public final String URL="http://viendatabaseservice.somee.com//mywebservice.asmx?WSDL";
	final String URL = "http://viendatabaseservice.somee.com/WebService.asmx?WSDL";

	private Button neuesSpiel, mitSpielen;
	private ListView lvSpiel;
	
	public int spielID, schwierigRate, maxSpieler, aktuelleSpieler, maxSpielID = 0;;
	public BigDecimal einsatzGeld;
	public double einsatzGeldDoubleWert;
	public boolean mitspielenVerboten;
	public String gewinner="Ohne";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spiel_uebersicht);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		information = (TextView) findViewById(R.id.txtInformation);		
		lvSpiel = (ListView) findViewById(R.id.ListView);
		
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arrSpiel_copy);
		lvSpiel.setAdapter(adapter);
		
	
		 //Abruf der Daten aus der Datenbank --> emailLesen();
		 
			
		
		doGetList(); 
		
		
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
				{fehlermeldung("Sie haben schon getippt!");}
				else 
				{
					if (aktuelleSpieler == maxSpieler)
					{
						fehlermeldung("Kein Platz mehr vorhanden!\n Wählen Sie ein anderes Spiel!");
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
	
	// ------------------------------------------------------------------------ 	doGetList(); 
	/*
	 * Abfruf der Daten des Spiels
	 * Fälle:
	 * 1 - Spielplatz nicht vorhanden, Gewinner nicht vorhanden --> Spiel zu Ende -> Kein Gewinner!
	 * 2 - Spielplatz nicht vorhanden, Gewinner vorhanden -> Spiel zu Ende -> Gewinner: Spieler XY!
	 * 3 - Spielplatz vorhanden -> Spielplätze verfügbar! 
	 * 
	 */
	
	
	
	public void doGetList() {
		String str = "";
		try {
			final String METHOD_NAME = "getListSpiels";
			final String SOAP_ACTION = NAMESPACE + METHOD_NAME;
			
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			MarshalFloat marshal = new MarshalFloat();
			marshal.register(envelope);
			
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			androidHttpTransport.call(SOAP_ACTION, envelope);
			
			SoapObject soapArray2 = (SoapObject) envelope.getResponse();
			arrSpiel.clear();
			for (int i = 0; i < soapArray2.getPropertyCount(); i++) {
				SoapObject soapItem = (SoapObject) soapArray2.getProperty(i);
				String SpielID = soapItem.getProperty("SpielID").toString().trim();
				String Schwerrate = soapItem.getProperty("Schwerrate").toString();
				String MaxSpieler = soapItem.getProperty("MaxSpieler").toString();
				String ActuellSpieler = soapItem.getProperty("ActuellSpieler").toString();
				String GeldPol = soapItem.getProperty("GeldPol").toString();
				arrSpiel.add(SpielID + " " + Schwerrate + " " + MaxSpieler
						+ " " + ActuellSpieler + " " + GeldPol);
				
				// Ist das Spiel voll?
				if (MaxSpieler.equals(ActuellSpieler))
				{
					// Hat ein Spieler gewonnen
					if (emailLesen(SpielID)=="Nein"){
					str="\t - Spiel ist zu Ende! \nKein Sieger";}
					else {
						str="\t - Spiel ist zu Ende! \nSieger: " + emailLesen(SpielID);
					}
			   	}
				else {
				str="\t - Spielplätze vorhanden!";
				}
					
				arrSpiel_copy.add("Game: "+SpielID+" "+str);// #############################################################
				if (this.maxSpielID <Integer.parseInt(SpielID)){this.maxSpielID = Integer.parseInt(SpielID);}
				else {}
				
			}

			adapter.notifyDataSetChanged();
		} catch (Exception ex) {
			fehlermeldung("Fehler bei der Internetverbindung");
			information.setText(ex.toString());
		}
	}
	
	// ------------------------------------------------------------------------ 	informationenVonString(...)
		/*
		 * Informationen für das Informationsfenster bekommen:
		 * Spieleranzahl Aktuelle/Maximale + Erforderliche Geldeinsatz für das Spiel
		 */
		

	public String informationenVonString(String VerarbeiteteInformationen) {
		VerarbeiteteInformationen.trim();
		String[] arr = VerarbeiteteInformationen.split(" "); // cat mot chuoi
																// thanh nhieu
																// chuoi
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
	
	// ------------------------------------------------------------------------ 	getListLoginIDSpielSchonGespielt()
	/*
	 * Erhalten der LoginID und ob dieser schon gespielt hat
	 */	
	public StringBuilder getListLoginIDSpielSchonGespielt()
	{
		
		StringBuilder listSpielerUsername= new StringBuilder();
		listSpielerUsername.append("Aktuelle Spieler"+":\n");
		
		String [] arraySpielerID = String.valueOf(getListIDVonSpieler()).split("#");
		
		for (int j = 0; j <arraySpielerID.length; j++)
		{
			if((packenLoginInformationen().getInt("ID_Login"))==Integer.parseInt(arraySpielerID[j].toString().trim()))
			{this.mitspielenVerboten=true; break;}
			else this.mitspielenVerboten=false;
		}

		for (int j = 0; j <arraySpielerID.length; j++ )
		{
			try 
	    	{
	    		final String METHOD_NAME="getListLogin";
	    		final String SOAP_ACTION=NAMESPACE+METHOD_NAME;
	    		SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
	    		SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
	    		envelope.dotNet=true;
	    		envelope.setOutputSoapObject(request);
	    		MarshalFloat marshal=new MarshalFloat();
	    		marshal.register(envelope);
	    		
	    		HttpTransportSE androidHttpTransport= new HttpTransportSE(URL);
	    		androidHttpTransport.call(SOAP_ACTION, envelope);
	    		SoapObject soapArray=(SoapObject) envelope.getResponse();
	    		
	    		for(int i=0; i<soapArray.getPropertyCount(); i++)
	    		 {
	    			SoapObject soapItem =(SoapObject) soapArray.getProperty(i);
	    			if(Integer.parseInt(arraySpielerID[j]) ==Integer.parseInt((soapItem.getProperty("LoginID").toString())))
	    			{
	    				listSpielerUsername.append(arraySpielerID[j]+": "+(soapItem.getProperty("Username")+"\n")).toString().trim();
	    				
	    			}
	    			else continue;
	    		 }
	    	}
	    	catch (Exception ex)
	    	{
	    		Toast.makeText(this, "GetList VorratErgebnisse fail ", Toast.LENGTH_LONG).show();
	    	}
		}
		//Toast.makeText(this, "this.Gewinner= "+ this.Gewinner, Toast.LENGTH_LONG).show();
	
		return listSpielerUsername;
	}
	
	// ------------------------------------------------------------------------ 	getListIDVonSpieler()
	/*
	 * SpielerID erhalten
	 */	
	public StringBuilder getListIDVonSpieler()
	{
		StringBuilder list_LoginID= new StringBuilder();
		list_LoginID.append("");
		
		try 
    	{
    		final String METHOD_NAME="getListVorratErgebnisse";
    		final String SOAP_ACTION=NAMESPACE+METHOD_NAME;
    		SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
    		SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
    		envelope.dotNet=true;
    		envelope.setOutputSoapObject(request);
    		MarshalFloat marshal=new MarshalFloat();
    		marshal.register(envelope);
    		
    		HttpTransportSE androidHttpTransport= new HttpTransportSE(URL);
    		androidHttpTransport.call(SOAP_ACTION, envelope);
    		SoapObject soapArray=(SoapObject) envelope.getResponse();
    		
    		for(int i=0; i<soapArray.getPropertyCount(); i++)
    		 {
    			SoapObject soapItem =(SoapObject) soapArray.getProperty(i);
    			if(this.spielID ==Integer.parseInt(soapItem.getProperty("SpielID").toString()))
    			{
    				list_LoginID.append((soapItem.getProperty("LoginID")+"#")).toString().trim();
    				
    			}
    			else continue;
    		 }
    		   		
    	}
    	catch (Exception ex)
    	{
    		Toast.makeText(this, "GetList VorratErgebnisse fail ", Toast.LENGTH_LONG).show();
    	}
		
		return list_LoginID;
	}

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

	// ------------------------------------------------------------------------ 	emailLesen(String SpielID)
			/*
			 * Zusätzlichen String in der E-Mail lesen
			 */	
		
	public String emailLesen(String SpielID)
	{
		String Str= "Nein";
		try 
    	{
    		final String METHOD_NAME="getListLogin";
    		final String SOAP_ACTION=NAMESPACE+METHOD_NAME;
    		SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
    		SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
    		envelope.dotNet=true;
    		envelope.setOutputSoapObject(request);
    		MarshalFloat marshal=new MarshalFloat();
    		marshal.register(envelope);
    		
    		HttpTransportSE androidHttpTransport= new HttpTransportSE(URL);
    		androidHttpTransport.call(SOAP_ACTION, envelope);
    		SoapObject soapArray=(SoapObject) envelope.getResponse();
    		for(int i=0; i<soapArray.getPropertyCount(); i++)
    		 {
    			SoapObject soapItem =(SoapObject) soapArray.getProperty(i);
    	
    			if (soapItem.getProperty("Email").toString().contains("#"+SpielID))
    			{
    				// Wenn ein Spieler gewonnen hat, ersetze den String mit seinem Namen
    				Str = Str.replace("Nein", "");
    				if(Str.contains(soapItem.getProperty("Username").toString())){}
    				else{Str = Str+ soapItem.getProperty("Username") +",\t";}
    			}
    			else {}
    			
    		 }
    		   		
    	}
    	catch (Exception ex)
    	{
    		Toast.makeText(this, "GetList VorratErgebnisse fail ", Toast.LENGTH_LONG).show();
    	}
		return Str;
	}
}
