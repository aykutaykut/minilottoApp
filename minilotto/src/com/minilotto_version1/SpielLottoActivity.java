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

import com.minilotto_version1.R;

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

public class SpielLottoActivity extends ActionBarActivity {

// ------------------------------------------------------------------------ Deklarationen
	
	
	public final String NAMESPACE="http://AndroidMinilottoDatabaseService.com/";
	public final String URL="http://viendatabaseservice.somee.com/Webservice.asmx?WSDL";
	
	private String ergebnisString;
	private String vorratErgebnisString;
	private boolean gewinnerGefunden;
	private boolean einmalAusfuehrenDruecken=false;
	
	private Button ausfuehren,refresh;
	private EditText z1,z2,z3;
	private TextView ergebnis,ziehung;
	
	public TextView information,information2;
	
// ------------------------------------------------------------------------ onCreat -> ergebnisse_layout
/*
 * layout wird gestartet, EditText, Buttons und co. werden zugeordnet.
 */
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.minilotto_layout);
		
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		// Button zur Abgabe des Tipps
		ausfuehren = (Button) findViewById(R.id.btnAusfuehren);
		
		// Button zum Aktualisieren der Daten
		refresh = (Button) findViewById(R.id.refresh);
		
		// 3 festzulegende Lottozahlen (User)		
		z1 = (EditText) findViewById(R.id.editZahl1);
		z2 = (EditText) findViewById(R.id.editZahl2);
		z3 = (EditText) findViewById(R.id.editZahl3);
		
		information = (TextView) findViewById(R.id.textView2);
		ziehung = (TextView) findViewById(R.id.ziehung);
		ergebnis = (TextView) findViewById(R.id.txtErgebnis_zeigen);
		information2 = (TextView) findViewById(R.id.textView22);
		
		auspackenLoginSpielInformationen(""); 	
		
		// ------------------------------------------------------------------------ onClick -> ausfuehren
		/*
		 * Bei Klick auf Ausführen wird geschaut ob 1) alle EdiText Felder ausgefüllt sind -> Sonst Fehlermeldung
		 * 2) Ob der Spieler bereits auf Ausführen gedrückt hat -> Sonst Fehlermeldung
		 * 3) Ob die Tippabgabe durch Ausführen durch den letzten Spieler der Spielsitzung stattfand
		 * da erst dann die Gewinnerermittlung durchgeführt wird
		 * 
		 */
		ausfuehren.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (pruefenObAlleEditTextAusgefuelltWurden()==true)
				{
					if (einmalAusfuehrenDruecken==false){
					randomToFindTheRightNumber();
					if(letzterSpieler()==true)
					{
						HashMap GewinnerID = new HashMap();
						GewinnerID= gewinnerSuchen(); //###############################################
						if (GewinnerID.get("GewinnerMenge").equals(0)){information.setText("Es gibt keinen Gewinner!");}
						else 
						{
							for (int i=1; i <= (Integer.valueOf(GewinnerID.get("GewinnerMenge").toString()));i++)
							{
								gewinnerNotiz(Integer.valueOf(GewinnerID.get("Gewinner"+i).toString()));
							}
						}
					}
					else {information.setText("Informationen der Mitspieler");}
					}
					else {fehlermeldung("Es darf nur einmal getippt werden!");}	
					}
				else {fehlermeldung("Bitte alle Felder ausfüllen!");}											
			}
			
		});
		
		// ------------------------------------------------------------------------ onClick -> refresh
		/*
		 * Durch Drücken der refresh-Taste aktualisieren sich die Informationen.
		 */
		refresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				refresh.refreshDrawableState();	
				
				Intent callerIntent = getIntent(); 
				Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
				if (ActuellSpieler_Gleich_MaxSpieler(packageFromSpiel.getInt("ID_Spiel"))==true)//########################################################
				{
					String emailLesen=Email_Lesen(String.valueOf(packageFromSpiel.getInt("ID_Spiel")));
					auspackenLoginSpielInformationen(emailLesen); 
					ziehung.setText(getErgebnisDerSpielID());
				}
				else{
					auspackenLoginSpielInformationen("");
					ergebnis.setText(vorratErgebnisString);
				}
				
			}
		});
		
		
		
		
		
		
		
		
		
		
	}
	
	// ------------------------------------------------------------------------ gewinnerNotiz(int LoginID)
	/*
	 * Wenn der Gewinner ermittelt wurde, wird ihm eine Nachricht hinterlassen + der Gewinn auf sein Konto
	 */
	
	
	public void gewinnerNotiz(int LoginID)
	{
		this.gewinnerGefunden = true;
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
    		
    		updateLogin(LoginID,Logname,Pass,Email,Bank_Double);
    		
    	}
    	catch (Exception ex)
    	{
    		fehlermeldung("Fehler bei der Internetverbindung");
    	}
		
	}
	// ------------------------------------------------------------------------ randomToFindTheRightNumer()
	/*
	 * 3 x Zahlenabgabe von 1-3, sonst Fehlermeldung
	 * Außerdem werden hier per Zufallsgenerator 3 x 1-3 Ziffern generiert für die Lottoziehungzahl
	 */
	
	
	// Random funktion to find Ergebnis 
	public void randomToFindTheRightNumber()
	{
		
		
		int zahl1 = Integer.parseInt(z1.getText().toString());
		int zahl2 = Integer.parseInt(z2.getText().toString());
		int zahl3 = Integer.parseInt(z3.getText().toString());

		
		if (pruefen(zahl1,zahl2,zahl3) == false){
			fehlermeldung("Bitte eine Zahl zwischen 1-3 eingeben!");
		}
		else{
			this.einmalAusfuehrenDruecken = true;
			this.vorratErgebnisString = (String.valueOf(zahl1).trim()+
					String.valueOf(zahl2).trim()+String.valueOf(zahl3).trim()).trim();
			
			ergebnis.setText(this.vorratErgebnisString.toString());
			
			if (pruefenObSpielerSpielHerstellerIst()==true){																		
				int ranzahl1 = (int) (Math.random()*3+1);
				int ranzahl2 = (int) (Math.random()*3+1);
				int ranzahl3 = (int) (Math.random()*3+1);
				
				this.ergebnisString = (String.valueOf(ranzahl1).trim()+
						String.valueOf(ranzahl2).trim()+String.valueOf(ranzahl3).trim()).trim();
				
				insertSpiel();
				Update_packageFromSpiel();
				updateLogin();
				insertErgebnis();  
				insertVorratErgebnis();
			}
			else 
			{
				updateLogin();
				updateSpiel();
				insertVorratErgebnis();
			}
			
		}
	}
	
	
	// ------------------------------------------------------------------------ purefen(int a, int b, int c)
	/*
	 * Überprüfung der eingegeben Werte ob 1,2 oder 3.
	 */
	
	
	public boolean pruefen(int a, int b, int c)
	{
		if ((a<1)||(a>3)||(b<1)||(b>3)||(c<1)||(c>3)){return false;}
		else {return true;}
	}
	
	// ------------------------------------------------------------------------ auspackenLoginSpielInformationen()
	/*
	 * AUSPACKEN der LoginSpielInformationen
	 */
	
	
	public void auspackenLoginSpielInformationen(String Str)
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromLogin = callerIntent.getBundleExtra("LogInformationen");
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		information2.setText("\n Deine User ID:\n "+packageFromLogin.getInt("ID_Login")
				+" -- "+packageFromLogin.getString("Username_Login")
			//	+ "\t Spiel_ID: "+packageFromSpiel.getInt("ID_Spiel")
				+"\n\n Aktuelle Spieler = "+packageFromSpiel.getInt("ActuellSpieler")
				+"\n\n Max. Spieler= "+packageFromSpiel.getInt("MaxSpieler")
				+"\n\n Geldeinsatz= "+packageFromSpiel.getDouble("EinSatzGeld_DoubleWert")
				+"\n\n Pot= "+(packageFromSpiel.getDouble("EinSatzGeld_DoubleWert")*packageFromSpiel.getInt("MaxSpieler"))
				+""+Str);
		information.setText(" "+String.valueOf(ruckgabeVonUsernameVorratErgebnis())+"\n"+Str);
	
	
	
	}
	

	// ------------------------------------------------------------------------ pruefenObSpielerSpielHerstellerIst()
		/*
		 * Prüfen ob Spieler auch der SpielHersteller ist.
		 */
	
	public boolean pruefenObSpielerSpielHerstellerIst()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		if (packageFromSpiel.getInt("ID_Spiel")==0){return true;}
		else return false;
	}


	// ------------------------------------------------------------------------ fehlermeldung( String fMeldung)
		/*
		 * Generierung der Fehlermeldung
		 */
	
	public void fehlermeldung(String fMeldung)
	{
		AlertDialog.Builder b = new AlertDialog.Builder(SpielLottoActivity.this);
		b.setMessage(fMeldung);
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
			}
		});
		b.create().show();
	}
	
	// ------------------------------------------------------------------------ letzterSpieler())
		/*
		 * Prüfung ob aktueller Spieler = letzter Spieler ist
		 */	
	public boolean letzterSpieler()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		if (packageFromSpiel.getInt("ActuellSpieler")==packageFromSpiel.getInt("MaxSpieler")){return true;}
		else {return false;}
	}

	// ------------------------------------------------------------------------ gewinnerSuchen()
		/*
		 * Gewinnersuche
		 */
	public HashMap gewinnerSuchen()
	{
		//Toast.makeText(this, "GewinnerSuchen", Toast.LENGTH_LONG).show(); // ####test #######
		
		HashMap ID = new HashMap();
		String GewinnerID = String.valueOf(vergleichVorratErgebnisErgebnis()).trim();
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
	
	// ------------------------------------------------------------------------ vergleichVorratErgebnisErgebnis()
		/*
		 * Vergleich der abgegebenen 3 Zahlen mit den (random/zufall) generierten 3 Lotto Zahlen
		 */
	public 	StringBuilder vergleichVorratErgebnisErgebnis() // result is a String of LoginID, these win this Game
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		// implement Array VorrErg and LogID 
		String [] VorErg = new String [packageFromSpiel.getInt("MaxSpieler")+5];		
		int [] LogID = new int [packageFromSpiel.getInt("MaxSpieler")+5];
		
		String Ergebnis_to_Compare = getErgebnisDerSpielID();
		
		Object [] array_VorErg = getListHatVorratErgebnisGleicheSpielID().toArray();	
		
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
	
	

	// ------------------------------------------------------------------------ getErgebnisDerSpielID()
		/*
		 * Das zur Spiel ID dazugehörige Ergebnis entnehmen
		 */
	
	public String getErgebnisDerSpielID()
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
	// ------------------------------------------------------------------------ getListHatVorratErgebnisGleicheSpielID()
	/*
	 * Aus der Tabelle VorratErgebnis die Tipps entnehmen die zu Spiel ID XY gehören
	 */
	public ArrayList getListHatVorratErgebnisGleicheSpielID()
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
	
	// ------------------------------------------------------------------------  pruefenObAlleEditTextAusgefuelltWurden() 
	/*
	 * Prüfen ob alle Eingaben / EditText ausgefüllt wurden --> Zahlenabgabe vollständig?
	 */
	
	public boolean pruefenObAlleEditTextAusgefuelltWurden() 
	{
		if((z1.getText().toString().equals(""))
				||(z2.getText().toString().equals(""))
				||(z3.getText().toString().equals(""))){return false;}
		else {return true;}
	}

	
	// ------------------------------------------------------------------------  insertSpiel() 
		/*
		 * Spieldaten in Database einpflegen
		 */	
	public void insertSpiel ()
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
	
	
	// ------------------------------------------------------------------------  insertErgebnis() 
		/*
		 * Random/Lotto Ergebnis in Database einpflegen
		 */	
	public void insertErgebnis() 
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");

		try {
			//Toast.makeText(this, "Insert_Ergebnis",Toast.LENGTH_LONG).show();
			
			final String METHOD_NAME = "insertErgebnis";
			final String SOAP_ACTION = NAMESPACE + METHOD_NAME;

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapObject newErgebnis = new SoapObject(NAMESPACE, "inputErgebnis");
		
			newErgebnis.addProperty("Ergebnis",this.ergebnisString);
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
			
			/*
			int ret = Integer.parseInt(soapPrimitive.toString());
			if (ret <= 0) {
				Toast.makeText(this, "Insert Ergebnis fail", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Insert  Ergebnis Successful",Toast.LENGTH_LONG).show();
			}
			*/

		} catch (Exception ex) {
			Toast.makeText(this, "Insert Ergebnis Fehler Exception",
					Toast.LENGTH_LONG).show();
		}

	}
		
	
	// ------------------------------------------------------------------------  insertVorratErgebnis() 
		/*
		 * Abgebener Tipp wird in Database eingepflegt
		 */	
	public void insertVorratErgebnis() 
		 
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
			if (pruefenObSpielerSpielHerstellerIst()==true){
				newErgebnis.addProperty("SpielID", (packageFromSpiel.getInt("MaxSpielID")+1));}
			else {newErgebnis.addProperty("SpielID", packageFromSpiel.getInt("ID_Spiel"));}
			newErgebnis.addProperty("VorratErgebnis",this.vorratErgebnisString);
			
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

	// ------------------------------------------------------------------------  updateLogin() 
			/*
			 * Login Informationen werden aktualisiert
			 */	
	
	public void updateLogin()
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

	public void updateLogin(int LoginID, String User, String Pass, String Email, Double Bank)
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
	
	// ------------------------------------------------------------------------  updateSpiel() 
	/*
	 * Spiel Informationen werden aktualisiert
	 */	
	public void updateSpiel()
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
	
	// ------------------------------------------------------------------------  hatSpielerSchonAusfuehrenGedrueckt() 
	/*
	 * Prüfen ob Spieler schon im selben Spiel auf die Ausführentaste geklickt hat, also bereit einen Tipp abgebeben hat
	 */	
	public boolean hatSpielerSchonAusfuehrenGedrueckt()  //#################
	{
		boolean gedrueckt = false;
		
		Intent callerIntent = getIntent(); 
		Bundle packageFromLogin = callerIntent.getBundleExtra("LogInformationen");
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
			
		int [] LogID = new int [packageFromSpiel.getInt("MaxSpieler")+5];	
		Object [] array_VorErg = getListHatVorratErgebnisGleicheSpielID().toArray();	
		
		for (int i=0; i<array_VorErg.length;i++)
		{
			String [] arr = array_VorErg[i].toString().trim().split(" "); 
			LogID[i]= Integer.parseInt(arr [0]);		
			if (LogID[i]==packageFromLogin.getInt("ID_Login")){gedrueckt = true;}
			else {gedrueckt = false;}
		}
		
		return gedrueckt;
	}
	
	
	// ------------------------------------------------------------------------  ruckgabeVonUsernameVorratErgebnis() 
		/*
		 * Entsprechend zum Tipp wird der dazugehörige Username entnommen.
		 */	
	public StringBuilder ruckgabeVonUsernameVorratErgebnis()
	{
		Intent callerIntent = getIntent(); 
		Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
		
		String [] VorErg = new String [packageFromSpiel.getInt("MaxSpieler")+5];		
		int [] LogID = new int [packageFromSpiel.getInt("MaxSpieler")+5];
		
		Object [] array_VorErg = getListHatVorratErgebnisGleicheSpielID().toArray();	
		
		for (int i=0; i<array_VorErg.length;i++)
		{
			// split elements in array_VorrErg to 2 new Array 
			String [] arr = array_VorErg[i].toString().trim().split(" "); 
			LogID[i]= Integer.parseInt(arr [0]);
			VorErg[i] = arr[1];
		
			
		}
		
		
		StringBuilder listSpielerUsernameVorratErgebnis = new StringBuilder();
		listSpielerUsernameVorratErgebnis.append("\n Mitspieler"+":\t");
		
	
		
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
	    				
	    				listSpielerUsernameVorratErgebnis.append("\n"+LogID[j]+": "+(soapItem.getProperty("Username")+"--\tVorrat:\t"+VorErg[j])).toString().trim();
	    				
	    			}
	    			else continue;
	    		 }
	    		   		
	    	}
	    	catch (Exception ex)
	    	{
	    		Toast.makeText(this, "Username_VorratErgebnis_zurueck_geben fail ", Toast.LENGTH_LONG).show();
	    	}
		}
		
		
		return listSpielerUsernameVorratErgebnis;
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
				if(Max.toString().trim().equals(Actuell.toString().trim())){Str=true;}
				else {Str = false;}

			} catch (Exception ex) {
				fehlermeldung(" Get_Spieler_from_Database hat Fehler \t");
			}

			return Str;
		}
		
		public void Update_packageFromSpiel()
		{
			Intent callerIntent = getIntent(); 
			Bundle packageFromSpiel = callerIntent.getBundleExtra("SpielInformationen");
			packageFromSpiel.putInt("ID_Spiel", packageFromSpiel.getInt("MaxSpielID")+1);
			
		}

		
		//************************************************************************************************************
		//------------------------------------------------------------------------------------------------------------
			
			public String Email_Lesen(String SpielID)
			{
				String Str= "Kein";
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
		    				Str = Str.replace("Kein", "");
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
				
				if (Str=="Kein"){
				Str = Str +"\tGewinner!";
				}
				else 
				{
					Str = Str + " ist der Gewinner!";
				}
				return Str;
			}
		
		
		
		
}
