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
					if (istSpielerSchonRegistriert()==true){FehlerMeldung("Username wird schon benutzt!");}
					else insertLogin();
				}
				
			}
		});

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
