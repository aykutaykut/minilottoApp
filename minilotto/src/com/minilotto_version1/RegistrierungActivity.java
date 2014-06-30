package com.minilotto_version1;

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
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;

public class RegistrierungActivity extends ActionBarActivity {
	
	// ------------------------------------------------------------------------ Deklarationen
	
	public EditText UsernameReg,PasswordsReg,PasswordsconfirmReg,EmailReg,BankReg;
	private Button Submit;
	public BigDecimal BankGeld;
	
	
	public final String NAMESPACE="http://AndroidMinilottoDatabaseService.com/";
	public final String URL="http://viendatabaseservice.somee.com/WebService.asmx?WSDL";

	// ------------------------------------------------------------------------ onCreate -> register_layout
	/*
	 * layout wird gestartet, EditText, Buttons und co. werden zugeordnet.
	 */
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_layout);
		
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        Submit=(Button) findViewById(R.id.button1);    
		UsernameReg=(EditText) findViewById(R.id.editUsernameReg);
		PasswordsReg=(EditText) findViewById(R.id.editPasswordsReg);
      
        EmailReg=(EditText) findViewById(R.id.editEmailReg);
        BankReg=(EditText) findViewById(R.id.editkontoReg);
    
        Submit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) // ---> false Weiter/   true ----> Fehler Meldung
			{
				if (pruefenObAlleEditTextAusgefuellt()==false)
				{
					FehlerMeldung("Alle Felder muessen ausgefuellt werden");
				}
				else 
				{
					if (istSpielerSchonRegistriert()==true){FehlerMeldung("Username wurde schon benutzt!");}
					else insertLogin();
				}
				
			}
		});

	}
	

	// ------------------------------------------------------------------------ istSpielerSchonRegistriert()
	/*
	 * prüfen ob Spieler schon in der Datenbank vorhanden ist bzw. schon mit dem username registriert ist.
	 */
		
	public boolean istSpielerSchonRegistriert()
	{
		boolean schonRegistriert=false;
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
    			if(UsernameReg.getText().toString().trim().equals((soapItem.getProperty("Username").toString()).trim()))
    			{
    				Toast.makeText(this, "Bereits Registriert", Toast.LENGTH_LONG).show();
    				schonRegistriert = true;
    			}
    			else continue;
    		 }
    		   		
    	}
    	catch (Exception ex)
    	{
    		Toast.makeText(this, "Fehler schon_registriert", Toast.LENGTH_LONG).show();
    	}
		return schonRegistriert;
	}
	

	// ------------------------------------------------------------------------ insertLogin()
		/*
		 * Login Daten einfügen
		 */
	
	public void insertLogin()
	{
		String msg="Internet verbinden...";
        
		double y = Double.valueOf(BankReg.getText().toString());
		this.BankGeld = BigDecimal.valueOf(y);
		
		try
		 {
			final String METHOD_NAME = "insertSpieler";
			final String SOAP_ACTION = NAMESPACE + METHOD_NAME;
			
			SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
			SoapObject newSpieler=new SoapObject(NAMESPACE, "inputspieler");

			newSpieler.addProperty("Username",UsernameReg.getText().toString());			
			newSpieler.addProperty("Passwords",PasswordsReg.getText().toString());			
			newSpieler.addProperty("Email",EmailReg.getText().toString());
			newSpieler.addProperty("Bank",this.BankGeld);
			newSpieler.addProperty("SpielID",2);
			request.addSoapObject(newSpieler);
			
			msg="addProperty...";
			
			SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet=true;
			envelope.setOutputSoapObject(request);
			MarshalFloat marshal=new MarshalFloat();
			marshal.register(envelope);
			
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapPrimitive soapPrimitive= (SoapPrimitive)envelope.getResponse();
			int ret=Integer.parseInt(soapPrimitive.toString());
					 
			msg="Registrierung Successful";
			if(ret<=0)
				 msg="Registrierung Failed";
					 
		 }
		catch (Exception ex)
		{
			msg="Fehler bei der Registrierung!";
		}
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	
	// ------------------------------------------------------------------------ pruefenObAlleEditTextAusgefuellt() 
			/*
			 * Prüfen ob alle erforderlichen Felder ausgefüllt wurden.
			 */
	
	public boolean pruefenObAlleEditTextAusgefuellt() 
		{
			if((UsernameReg.getText().toString().equals(""))
				||(PasswordsReg.getText().toString().equals(""))
		
				||(BankReg.getText().toString().equals(""))
				||(EmailReg.getText().toString().equals(""))){return false;}
			else {return true;}
		}

		
	// ------------------------------------------------------------------------ Fehlermeldung 
				/*
				 * Generierung der Fehlermeldung
				 */
		

	public void FehlerMeldung(String Meldung)
	{
		AlertDialog.Builder b = new AlertDialog.Builder(RegistrierungActivity.this);
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
