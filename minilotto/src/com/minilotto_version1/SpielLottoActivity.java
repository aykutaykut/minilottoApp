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
		
	}
//*****************************************************************************************************		
//-----------------------------------------------------------------------------------------------------
	public void Gewinner_NachDiesnt(int LoginID)
	{
		this.Gewinner_Gefunden = true;
		String Logname="",Pass= "",Email="";
		double Bank_Double= 0;
		
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
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
    		SoapObject soapArray1=(SoapObject) envelope.getResponse();
    		
    		for(int i=0; i<soapArray1.getPropertyCount(); i++)
    		 {
    			SoapObject soapItem =(SoapObject) soapArray1.getProperty(i);
    			if (LoginID == Integer.valueOf(soapItem.getProperty("LoginID").toString()))
    			{
	    			Logname=soapItem.getProperty("Username").toString().trim();
	    			Pass=soapItem.getProperty("Passwords").toString().trim();	
	    			
	    			Email=soapItem.getProperty("Email").toString().trim();
	    			if (Email.contains("#"))
	    			{
	    				Email = Email.replace((Email.substring(Email.indexOf("#")))," ").trim();
	    			}
	    			else{}
	    			Email=(Email+"#"+packageFromSpiel.getInt("ID_Spiel")+"#"+" Mit "+
	    							(packageFromSpiel.getDouble("EinSatzGeld_DoubleWert")*
	    									packageFromSpiel.getInt("MaxSpieler"))).trim();
	    			
	    			String B =soapItem.getProperty("Bank").toString().trim();
	    			Bank_Double = Double.valueOf(B) + (packageFromSpiel.getDouble("EinSatzGeld_DoubleWert")*packageFromSpiel.getInt("MaxSpieler"));
    			}	
    		 } 
    		
    		Update_Login(LoginID,Logname,Pass,Email,Bank_Double);
    		
    	}
    	catch (Exception ex)
    	{
    		FehlerMeldung("Fehler bei Internet Verbinden");
    	}
		Toast.makeText(this, Ergebnis_String, Toast.LENGTH_LONG).show();
	}
//*******************************************************************************************************	
//-------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------
	// Random funktion to find Ergebnis 
	public void RandomToFindTheRightNumber()
	{
		this.einmal_ausführen_drücken = true;
		
		int zahl1 = Integer.parseInt(z1.getText().toString());
		int zahl2 = Integer.parseInt(z2.getText().toString());
		int zahl3 = Integer.parseInt(z3.getText().toString());

		
			
		}
	}
	
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------	
	public boolean pruefen(int a, int b, int c)
	{
		if ((a<0)||(a>9)||(b<0)||(b>9)||(c<0)||(c>9)){return false;}
		else {return true;}
	}
	
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------
	
	
	public void AusPacken_LoginUndSpiel_Informationen(String Str)
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromLogin = callerIntent.getBundleExtra("LogInformationen");
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		Information.setText("LogID: "+packageFromLogin.getInt("ID_Login")
				+" -- "+packageFromLogin.getString("Username_Login")
				+ "\t Spiel_ID: "+packageFromSpiel.getInt("ID_Spiel")
				+"\t ActuelleSpieler = "+packageFromSpiel.getInt("ActuellSpieler")
				+"\tMaxSpieler= "+packageFromSpiel.getInt("MaxSpieler")
				+"\tGelPol= "+packageFromSpiel.getDouble("EinSatzGeld_DoubleWert")
				+"\tGeldSum= "+(packageFromSpiel.getDouble("EinSatzGeld_DoubleWert")*packageFromSpiel.getInt("MaxSpieler"))
				+"\n"+String.valueOf(Username_VorratErgebnis_zurueck_geben())
				+"\n\n" + Str);
	}
	

//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------
	
	public boolean Pruefen_Ob_Der_Spieler_der_SpielHersteller_ist()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		if (packageFromSpiel.getInt("ID_Spiel")==0){return true;}
		else return false;
	}


//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------
	
	public void FehlerMeldung(String Meldung)
	{
		AlertDialog.Builder b = new AlertDialog.Builder(EinfachSpielActivity.this);
		b.setMessage(Meldung);
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
			}
		});
		b.create().show();
	}
	
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------	
	public boolean LetzteSpieler()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		if (packageFromSpiel.getInt("ActuellSpieler")==packageFromSpiel.getInt("MaxSpieler")){return true;}
		else {return false;}
	}
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------	
	
	public HashMap GewinnerSuchen()
	{
		Toast.makeText(this, "GewinnerSuchen", Toast.LENGTH_LONG).show(); // ####test #######
		
		HashMap ID = new HashMap();
		String GewinnerID = String.valueOf(Vergleich_VorratErgebnis_und_Ergebnis()).trim();
		// first we have to test String GewinnerID. if this String is'nt Zero than 
		if (GewinnerID.equals("")){ID.put("GewinnerMenge", 0);}
		else
		{
			String [] arr_GewinnerID = GewinnerID.split(" ");  // Array of Gewinner_ID 
			//Information.setText("List GewinnerID "+ GewinnerID);
			for (int i=1; i<=arr_GewinnerID.length;i++)
			{
				ID.put("Gewinner"+i, arr_GewinnerID[i-1]);
			}
			
			ID.put("GewinnerMenge", arr_GewinnerID.length);
		}
		return ID;
	}
	
//**************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------
	public 	StringBuilder Vergleich_VorratErgebnis_und_Ergebnis() // result is a String of LoginID, these win this Game
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		// implement Array VorrErg and LogID 
		String [] VorErg = new String [packageFromSpiel.getInt("MaxSpieler")+5];		
		int [] LogID = new int [packageFromSpiel.getInt("MaxSpieler")+5];
		
		String Ergebnis_to_Compare = get_Ergebnis_der_SpielID();
		
		Object [] array_VorErg = getList_VorratErgebnis_Hat_gleich_SpielID().toArray();	
		
		for (int i=0; i<array_VorErg.length;i++)
		{
			// split elements in array_VorrErg to 2 new Array 
			String [] arr = array_VorErg[i].toString().trim().split(" "); 
			LogID[i]= Integer.parseInt(arr [0]); //##################################################
			VorErg[i] = arr[1];
		}
		
		
		StringBuilder List_IDGewinner = new StringBuilder();
				
		for (int j=0; j<packageFromSpiel.getInt("MaxSpieler"); j++)
		{		
			if((Ergebnis_to_Compare.trim()).equals(VorErg[j].trim()))
			{
				List_IDGewinner.append(LogID[j]+" ");
			}
			else {continue;}
		}
		return List_IDGewinner ; 
	}
	
	
//**************************************************************************************************************
//-----------------------------------------------------------------------------------------------------------------
	//lay ket qua chinh xac cua tro choi (da dc upload len truoc do)
	public String get_Ergebnis_der_SpielID()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		String Ergebnis="";
		try 
    	{
    		final String METHOD_NAME="getListErgebnis";
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
    			if(packageFromSpiel.getInt("ID_Spiel")==Integer.parseInt(soapItem.getProperty("SpielID").toString()))
    			{
    				Ergebnis = soapItem.getProperty("Ergebnis").toString().trim();
    				
    			}
    			else continue;
    		 }
    		
    	}
    	catch (Exception ex)
    	{
    		Toast.makeText(this, "GetList von Ergebnis fail ", Toast.LENGTH_LONG).show();
    	}
		return Ergebnis;
	}
//*************************************************************************************************************
//-------------------------------------------------------------------------------------------------------------
	// ket qua tra ve la mot list (LoID+" "+VorratErg)
	public ArrayList getList_VorratErgebnis_Hat_gleich_SpielID()
	{	
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		ArrayList LoginID_VorratErg = new ArrayList();
		LoginID_VorratErg.add("");
		try 
    	{
    		final String METHOD_NAME="getListVorratErgebnisse ";
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
    		
    		LoginID_VorratErg.clear();
    		
    		for(int i=0; i<soapArray.getPropertyCount(); i++)
    		 {
    			SoapObject soapItem =(SoapObject) soapArray.getProperty(i);
    			if(packageFromSpiel.getInt("ID_Spiel")==Integer.parseInt(soapItem.getProperty("SpielID").toString()))
    			{
    				String LogID = soapItem.getProperty("LoginID").toString();
    				String VorErg = soapItem.getProperty("VorratErgebnis").toString();
    				LoginID_VorratErg.add(new String(LogID+" "+VorErg).trim());
    			}
    			else continue;
    		 }		
    	}
    	catch (Exception ex)
    	{
    		Toast.makeText(this, "GetList VorratErgebnis fail ", Toast.LENGTH_LONG).show(); 
    	}
		
		
		return (LoginID_VorratErg);
	}
	
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------	
	public boolean pruefen_Ob_Alle_EditText_Erfüllen_Werden() 
	{
		if((z1.getText().toString().equals(""))
				||(z2.getText().toString().equals(""))
				||(z3.getText().toString().equals(""))){return false;}
		else {return true;}
	}

	
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------	
		
	public void Insert_Spiel ()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		double y = (packageFromSpiel.getInt("MaxSpieler")*packageFromSpiel.getDouble("EinSatzGeld_DoubleWert"));
		
		try
		 {
			//Toast.makeText(this, "Insert_Spiel", Toast.LENGTH_LONG).show();
			
			final String METHOD_NAME = "insertSpiel";
			final String SOAP_ACTION = NAMESPACE + METHOD_NAME;			
			SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
			SoapObject newSpieler=new SoapObject(NAMESPACE, "inputspiel");
		
			newSpieler.addProperty("Schwerrate",packageFromSpiel.getInt("SchwerRate"));			
			newSpieler.addProperty("MaxSpieler",packageFromSpiel.getInt("MaxSpieler"));			
			newSpieler.addProperty("ActuellSpieler",packageFromSpiel.getInt("ActuellSpieler"));
			newSpieler.addProperty("GeldPol",BigDecimal.valueOf(packageFromSpiel.getDouble("EinSatzGeld_DoubleWert")));
			newSpieler.addProperty("GeldSum",BigDecimal.valueOf(y));
			
			request.addSoapObject(newSpieler);		
			
			SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet=true;
			envelope.setOutputSoapObject(request);
			MarshalFloat marshal=new MarshalFloat();
    		marshal.register(envelope);
		
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			androidHttpTransport.call(SOAP_ACTION, envelope);					
			SoapPrimitive soapPrimitive= (SoapPrimitive)envelope.getResponse();

			/*
			int ret=Integer.parseInt(soapPrimitive.toString());		
			if(ret<=0)
				{Toast.makeText(this, "Insert Spiel fail", Toast.LENGTH_LONG).show();}
			else{Toast.makeText(this, "Insert  Spiel Successful", Toast.LENGTH_LONG).show();}
			*/		 
		 }
		catch (Exception ex)
		{
			Toast.makeText(this, "Insert Spiel Fehler Exception", Toast.LENGTH_LONG).show();
		}
		
	}
	
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------	
	public void Insert_Ergebnis() 
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");

		try {
			//Toast.makeText(this, "Insert_Ergebnis",Toast.LENGTH_LONG).show();
			
			final String METHOD_NAME = "insertErgebnis";
			final String SOAP_ACTION = NAMESPACE + METHOD_NAME;

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapObject newErgebnis = new SoapObject(NAMESPACE, "inputErgebnis");
		
			newErgebnis.addProperty("Ergebnis",this.Ergebnis_String);
			newErgebnis.addProperty("SpielID", (packageFromSpiel.getInt("MaxSpielID")+1));
			request.addSoapObject(newErgebnis);

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			MarshalFloat marshal = new MarshalFloat();
			marshal.register(envelope);
		
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			androidHttpTransport.call(SOAP_ACTION, envelope);			
			SoapPrimitive soapPrimitive = (SoapPrimitive) envelope.getResponse();

		} catch (Exception ex) {
			Toast.makeText(this, "Insert Ergebnis Fehler Exception",
					Toast.LENGTH_LONG).show();
		}

	}
		
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------	
	public void Insert_VorratErgebnis() 
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromLogin = callerIntent.getBundleExtra("LogInformationen");
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");

		try {
			//Toast.makeText(this, "Insert_VorratErgebnis",Toast.LENGTH_LONG).show();
			
			final String METHOD_NAME = "insertVorrateEgebnis";
			final String SOAP_ACTION = NAMESPACE + METHOD_NAME;

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapObject newErgebnis = new SoapObject(NAMESPACE, "inputvorratergebnis");
		
			newErgebnis.addProperty("LoginID",packageFromLogin.getInt("ID_Login"));
			if (Pruefen_Ob_Der_Spieler_der_SpielHersteller_ist()==true){
				newErgebnis.addProperty("SpielID", (packageFromSpiel.getInt("MaxSpielID")+1));}
			else {newErgebnis.addProperty("SpielID", packageFromSpiel.getInt("ID_Spiel"));}
			newErgebnis.addProperty("VorratErgebnis",this.VorratErgebnis_String);
			
			request.addSoapObject(newErgebnis);
		
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			MarshalFloat marshal = new MarshalFloat();
			marshal.register(envelope);

			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			androidHttpTransport.call(SOAP_ACTION, envelope);

			SoapPrimitive soapPrimitive = (SoapPrimitive) envelope.getResponse();
			/*
			int ret = Integer.parseInt(soapPrimitive.toString());
			if (ret <= 0) {
				Toast.makeText(this, "Insert_VorratErgebnis fail", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Insert_VorratErgebnis Successful",Toast.LENGTH_LONG).show();
			}
			*/

		} catch (Exception ex) {
			Toast.makeText(this, "Insert_VorratErgebnis Fehler Exception",
					Toast.LENGTH_LONG).show();
		}

	}

//*********************************************************************************************************	
//---------------------------------------------------------------------------------------------------------
	//##############################################################################
	public void Update_Login()
    {
		Intent callerIntent = getIntent(); 
		Bundle packageFromLogin = callerIntent.getBundleExtra("LogInformationen");
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
	    try
	    {
		    
		    final String METHOD_NAME="UpdateSpieler";
		    final String SOAP_ACTION=NAMESPACE+METHOD_NAME;
		    
		    SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
		    SoapObject newSpieler=new SoapObject(NAMESPACE, "_login");
		    
			newSpieler.addProperty("LoginID",packageFromLogin.getInt("ID_Login"));			
			newSpieler.addProperty("Username",packageFromLogin.getString("Username_Login"));			
			newSpieler.addProperty("Passwords",packageFromLogin.getString("Passwords_Login"));
			newSpieler.addProperty("Email",packageFromLogin.getString("Email_Login"));
			newSpieler.addProperty("Bank",(packageFromLogin.getDouble("Bank_Login")-packageFromSpiel.getDouble("EinSatzGeld_DoubleWert")));
			request.addSoapObject(newSpieler);
		    
		    SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
		    envelope.dotNet=true;
		    envelope.setOutputSoapObject(request);
		    MarshalFloat marshal=new MarshalFloat();
    		marshal.register(envelope);
    		
		  //tạo đối tượng HttpTransportSE
		    HttpTransportSE androidHttpTransport= new HttpTransportSE(URL);
		    androidHttpTransport.call(SOAP_ACTION, envelope);

		    SoapPrimitive soapPrimitive= (SoapPrimitive)  envelope.getResponse();
		    //Toast.makeText(this, soapPrimitive.toString(), Toast.LENGTH_LONG).show();
	    }
	    catch(Exception e)
	    {
	    	Toast.makeText(this, "Fehler Login Update", Toast.LENGTH_LONG).show();
	    }
    }
//******************************************************************************************************
//------------------------------------------------------------------------------------------------------
	public void Update_Login(int LoginID, String User, String Pass, String Email, Double Bank)
    {
	    try
	    {
		    
		    //Toast.makeText(this, "Update_Login ", Toast.LENGTH_LONG).show();
		    
		    final String METHOD_NAME="UpdateSpieler";
		    final String SOAP_ACTION=NAMESPACE+METHOD_NAME;
		    
		    SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
		    SoapObject newSpieler=new SoapObject(NAMESPACE, "_login");
		    
			newSpieler.addProperty("LoginID",LoginID);			
			newSpieler.addProperty("Username",User);			
			newSpieler.addProperty("Passwords",Pass);
			newSpieler.addProperty("Email",Email);
			newSpieler.addProperty("Bank",Bank);
		    //#############################################################################
			request.addSoapObject(newSpieler);
		    
		    SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
		    envelope.dotNet=true;
		    envelope.setOutputSoapObject(request);
		    MarshalFloat marshal=new MarshalFloat();
    		marshal.register(envelope);
    		
		  //tạo đối tượng HttpTransportSE
		    HttpTransportSE androidHttpTransport= new HttpTransportSE(URL);
		    androidHttpTransport.call(SOAP_ACTION, envelope);

		    SoapPrimitive soapPrimitive= (SoapPrimitive)  envelope.getResponse();
		    
		    //Toast.makeText(this, soapPrimitive.toString(), Toast.LENGTH_LONG).show();
	    }
	    catch(Exception e)
	    {
	    	Toast.makeText(this, "Fehler Login Update", Toast.LENGTH_LONG).show();
	    }
    }
	
//-------------------------------------------------------------------------------------------------------
	public void Update_Spiel()
    {
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
	    try
	    {    	 
		    final String METHOD_NAME="UpdateSpiel";
		    final String SOAP_ACTION=NAMESPACE+METHOD_NAME;
		    
		    SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
		    SoapObject newSpiel=new SoapObject(NAMESPACE, "_spiel");
		    
		    newSpiel.addProperty("SpielID",packageFromSpiel.getInt("ID_Spiel"));			
		    newSpiel.addProperty("Schwerrate",packageFromSpiel.getInt("SchwerRate"));			
		    newSpiel.addProperty("MaxSpieler",packageFromSpiel.getInt("MaxSpieler"));
		    newSpiel.addProperty("ActuellSpieler",packageFromSpiel.getInt("ActuellSpieler"));
		    newSpiel.addProperty("GeldPol",packageFromSpiel.getDouble("EinSatzGeld_DoubleWert"));
		    newSpiel.addProperty("GeldSum",(packageFromSpiel.getDouble("EinSatzGeld_DoubleWert"))*packageFromSpiel.getInt("MaxSpieler"));
		    request.addSoapObject(newSpiel);
		    
		    SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
		    envelope.dotNet=true;
		    envelope.setOutputSoapObject(request);
		    MarshalFloat marshal=new MarshalFloat();
    		marshal.register(envelope);
    		
		    HttpTransportSE androidHttpTransport= new HttpTransportSE(URL);
		    androidHttpTransport.call(SOAP_ACTION, envelope);
		    
		    SoapPrimitive soapPrimitive= (SoapPrimitive)  envelope.getResponse();
	
		    //Toast.makeText(this, soapPrimitive.toString(), Toast.LENGTH_LONG).show();
	    }
	    catch(Exception e)
	    {
	    	Toast.makeText(this, "Fehler Spiel Update", Toast.LENGTH_LONG).show();
	    }
    }
	
	
//*********************************************************************************************************

	
	public boolean Spieler_schon_Ausführen_gedrückt_hat()  //#################
	{
		boolean gedrueckt = false;
		
		Intent callerIntent = getIntent(); 
		Bundle packageFromLogin = callerIntent.getBundleExtra("LogInformationen");
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
			
		int [] LogID = new int [packageFromSpiel.getInt("MaxSpieler")+5];	
		Object [] array_VorErg = getList_VorratErgebnis_Hat_gleich_SpielID().toArray();	
		
		for (int i=0; i<array_VorErg.length;i++)
		{
			String [] arr = array_VorErg[i].toString().trim().split(" "); 
			LogID[i]= Integer.parseInt(arr [0]);		
			if (LogID[i]==packageFromLogin.getInt("ID_Login")){gedrueckt = true;}
			else {gedrueckt = false;}
		}
		
		return gedrueckt;
	}
	
	
//---------------------------------------------------------------------------------------------------------
	
	public StringBuilder Username_VorratErgebnis_zurueck_geben()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		String [] VorErg = new String [packageFromSpiel.getInt("MaxSpieler")+5];		
		int [] LogID = new int [packageFromSpiel.getInt("MaxSpieler")+5];
		
		Object [] array_VorErg = getList_VorratErgebnis_Hat_gleich_SpielID().toArray();	
		
		for (int i=0; i<array_VorErg.length;i++)
		{
			// split elements in array_VorrErg to 2 new Array 
			String [] arr = array_VorErg[i].toString().trim().split(" "); 
			LogID[i]= Integer.parseInt(arr [0]);
			VorErg[i] = arr[1];
		
			
		}
		
		
		StringBuilder list_Spieler_Username_VorratErgebnis = new StringBuilder();
		list_Spieler_Username_VorratErgebnis.append("\n\nMit Spieler"+":\t");
		
	
		
		for (int j = 0; j <LogID.length; j++ )
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
	    			if(LogID[j]==Integer.parseInt(soapItem.getProperty("LoginID").toString()))
	    			{
	    				
	    				list_Spieler_Username_VorratErgebnis.append("\n"+LogID[j]+": "+(soapItem.getProperty("Username")+"--\tVorrat:\t"+VorErg[j])).toString().trim();
	    				
	    			}
	    			else continue;
	    		 }
	    		   		
	    	}
	    	catch (Exception ex)
	    	{
	    		Toast.makeText(this, "Username_VorratErgebnis_zurueck_geben fail ", Toast.LENGTH_LONG).show();
	    	}
		}
		
		
		return list_Spieler_Username_VorratErgebnis;
	}
	
//************************************************************************************************************
//------------------------------------------------------------------------------------------------------------
	
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
		
		Str = Str +"\tGewonnen";
		return Str;
	}
	
	
//#########################################################################################################
	
	public boolean ActuellSpieler_Gleich_MaxSpieler(int SpielID)
	{
		boolean Str=false;
		try {
			final String METHOD_NAME = "getSpiel";
			final String SOAP_ACTION = NAMESPACE + METHOD_NAME;

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapObject newSpiel = new SoapObject(NAMESPACE, "id");
			newSpiel.addProperty("id", SpielID);
			request.addSoapObject(newSpiel);

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			MarshalFloat marshal = new MarshalFloat();
			marshal.register(envelope);

			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			androidHttpTransport.call(SOAP_ACTION, envelope);
			newSpiel = (SoapObject) envelope.getResponse();

			String Max=(newSpiel.getProperty("MaxSpieler").toString());
			String Actuell=(newSpiel.getProperty("ActuellSpieler").toString());
			if(Max.toString().trim().equals(Actuell.toString().trim())){Str=true;FehlerMeldung(" Get_Spieler_from_Database gut \t");}
			else {Str = false;}

		} catch (Exception ex) {
			FehlerMeldung(" Get_Spieler_from_Database hat Fehler \t");
		}

		return Str;
	}
	
	public void Update_packageFromSpiel()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		packageFromSpiel.putInt("ID_Spiel", packageFromSpiel.getInt("MaxSpielID")+1);
		
	}
}
