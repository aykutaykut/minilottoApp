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

public class SpielFernsterActivity extends ActionBarActivity implements funktion{

	public TextView Information;
	
	ArrayList<String> arrSpiel = new ArrayList<String>();
	ArrayList<String> arrSpiel_copy = new ArrayList<String>(); ///#################################################
	ArrayAdapter<String> adapter = null;

	final String NAMESPACE = "http://AndroidMinilottoDatabaseService.com/";
	//public final String URL="http://viendatabaseservice.somee.com//mywebservice.asmx?WSDL";
	final String URL = "http://viendatabaseservice.somee.com/WebService.asmx?WSDL";

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
				{FehlerMeldung("Sie hat schon ein Vorrat für dieses Spiel gegeben");}
				else 
				{
					if (Actuell_Spieler == Max_Spieler)
					{
						FehlerMeldung("leider Keinen Platz mehr\n Bitte wählen Sie andere");
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
//**********************************************
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
			
				if (MaxSpieler.equals(ActuellSpieler)){str="\tbeendet: \n"+Email_Lesen(SpielID)+" Gewonnen";}
				else {str="\t Vorgang";}
				arrSpiel_copy.add("GAME: "+SpielID+" "+str);// #############################################################
				if (this.MaxSpielID <Integer.parseInt(SpielID)){this.MaxSpielID = Integer.parseInt(SpielID);}
				else {}
				
			}

			adapter.notifyDataSetChanged();
		} catch (Exception ex) {
			FehlerMeldung("Fehler bei der Internetverbindung");
			Information.setText(ex.toString());
		}
	}
//**********************************************************************************
	public String InformationenVonString(String VerarbeiteteInformationen) {
		VerarbeiteteInformationen.trim();
		String[] arr = VerarbeiteteInformationen.split(" "); // cat mot chuoi
																// thanh nhieu
																// chuoi
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
	
	public Bundle Packen_SpielInformationen()
    {
    	Bundle SpielInfor = new Bundle();
    	SpielInfor.putInt("ID_Spiel", this.Spiel_ID);
    	SpielInfor.putInt("SchwerRate", this.Schwer_Rate);
    	SpielInfor.putInt("MaxSpieler", this.Max_Spieler);
    	SpielInfor.putInt("ActuellSpieler", this.Actuell_Spieler + 1);  // diser Fall wir spielen mit dh.  das Spiel schon vorher Exitiert
    	SpielInfor.putDouble("EinSatzGeld_DoubleWert", this.EinSatz_Geld_DoubleWert);
    	SpielInfor.putInt("MaxSpielID", this.MaxSpielID);
		return SpielInfor;
    }
	
//***************************************
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

	public StringBuilder getList_LoginID_die_das_Spiel_Schon_gespielt_haben()
	{
		
		StringBuilder list_Spieler_Username= new StringBuilder();
		list_Spieler_Username.append("Actuelle Spieler"+":\t");
		
		String [] arraySpielerID = String.valueOf(Get_list_ID_der_Spieler()).split("#");
		
		for (int j = 0; j <arraySpielerID.length; j++)
		{
			if((Packen_LoginInformationen().getInt("ID_Login"))==Integer.parseInt(arraySpielerID[j].toString().trim()))
			{this.mit_spielen_verboten=true; break;}
			else this.mit_spielen_verboten=false;
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
	    			if(Integer.parseInt(arraySpielerID[j]) ==Integer.parseInt(soapItem.getProperty("LoginID").toString()))
	    			{
	    				list_Spieler_Username.append(arraySpielerID[j]+": "+(soapItem.getProperty("Username")+",\t")).toString().trim();
	    				
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
	
		return list_Spieler_Username;
	}
	
//*****************************************************************************************************
//-----------------------------------------------------------------------------------------------------
	
	public StringBuilder Get_list_ID_der_Spieler()
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
    			if(this.Spiel_ID ==Integer.parseInt(soapItem.getProperty("SpielID").toString()))
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
//*************************************************************************************************************
//-------------------------------------------------------------------------------------------------------------
	
	public String Email_Lesen(String SpielID)
	{
		String Str= "Ohne";
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
    				Str = Str.replace("Ohne", "");
    				if(Str.contains(soapItem.getProperty("Username").toString())){}
    				else{Str = Str+ soapItem.getProperty("Username") +",\t";}
    			}
    			else {}
    			
    		 }
    		   		
    	}
    	catch (Exception ex)
    	{
    		Toast.makeText(this, "GetList Login fail ", Toast.LENGTH_LONG).show();
    	}
		return Str;
	}
}

interface funktion
{
	public String Email_Lesen(String SpielID);
}
