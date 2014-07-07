package com.minilotto;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.minilotto.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity { 
	

	// ------------------------------------------------------------------------ Deklarationen
	
	private Button login,register;
	private EditText username,password;
	public boolean erfolg;
	
	public int loginID;
	public double doubleGuthaben;
	public String loginUsername,email,pass,gratulation;
	public BigDecimal guthaben;
	
	ArrayList<String> arrLog=new ArrayList<String>();
	ArrayAdapter<String>adapter=null;
	
	final String NAMESPACE="http://AndroidMinilottoDatabaseService.com/";
	final String URL="http://viendatabaseservice.somee.com/Webservice.asmx?WSDL";
	
	// ------------------------------------------------------------------------ onCreat -> activity_main
	/*
	 * layout wird gestartet, EditText, Buttons und co. werden zugeordnet.
	 */
		
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        login = (Button) findViewById(R.id.btnLogin);
        register = (Button) findViewById(R.id.btnRegister);
        
        username = (EditText) findViewById(R.id.editUsername);
        password = (EditText) findViewById(R.id.editPassword);
   
        login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				doGetList();
				emailLesen();
				updateLogin();
				
				if(erfolg == true)
				{
					//herstellen (SpielFernsterActivity) und starten 
					Intent Wellcome_Activity = new Intent (MainActivity.this, WelcomeActivity.class);
					// weiter liefern Login_Datei für näschte Activity (SpielFernsterActivity)				
					Wellcome_Activity.putExtra("LogInformationen", packenLoginInformationen());
					
					startActivity(Wellcome_Activity);
				}
				else {
					fehlermeldung("Falsche Login Daten eingegeben!");
				}
			}
			
			
		});
  
        
     // ------------------------------------------------------------------------ onClick -> register
        /*
         * Button zur Registration
         */
        	
        
        register.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent RegisterFernsterActivity = new Intent (MainActivity.this, RegistrierungActivity.class);
				startActivity(RegisterFernsterActivity);
				
			}
		});
       
    }
    
 // ------------------------------------------------------------------------ doGetList()
    /*
     * Logindaten erhalten
     */
    	
    
    
    
    public void doGetList() 
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
    		SoapObject soapArray1=(SoapObject) envelope.getResponse();
    		arrLog.clear();   		
    		String LogInformation = (username.getText().toString()).trim() + (password.getText().toString()).trim();
    		LogInformation.trim();
    		
    		for(int i=0; i<soapArray1.getPropertyCount(); i++)
    		 {
    			SoapObject soapItem =(SoapObject) soapArray1.getProperty(i);
    			String LogID=soapItem.getProperty("LoginID").toString();
    			String Logname=soapItem.getProperty("Username").toString();
    			String Pass=soapItem.getProperty("Passwords").toString();
    			String Email=soapItem.getProperty("Email").toString();
    			String Bank=soapItem.getProperty("Bank").toString();
    			
    			LogID.trim();
    			Logname.trim();
    			Pass.trim();
    			Email.trim();
    			Bank.trim();
    			
    			this.loginID = Integer.parseInt(LogID);
    			this.loginUsername = Logname;
    			this.pass = Pass;
    			this.email = Email;
    			this.doubleGuthaben = Double.valueOf(Bank);
    			this.guthaben = BigDecimal.valueOf(doubleGuthaben);
    			
    			arrLog.add((Logname+Pass).trim());
    			if (arrLog.get(i).equals(LogInformation)){erfolg = true; break;}
    			else {erfolg = false;}

    		 }
    		
    		//adapter.notifyDataSetChanged();
    	}
    	catch (Exception ex)
    	{
    		fehlermeldung("Internet Verbindungsfehler");
    	}
    }
 // ------------------------------------------------------------------------ emailLesen()
    /*
     * Nachrichten aus E-Mail auslesen; Benachrichtigung über Gewinn wird hier ausgegeben, wenn gewonnnen wurde!
     */
    	
    
    
    
    
    public String emailLesen()
    {
    	try
		{
    		String [] arr = this.email.split("#");
    		if(arr[1].equals("")){}
    		else 
    		{
    			this.email = arr[0]+"#"+arr[1];
    			this.gratulation = ("Sie haben bei folgendem Game\t" +arr[1]+arr[2]+" Euro gewonnen");
    		}
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			this.gratulation = "#";
		}
    	return gratulation;
    }
    
 // ------------------------------------------------------------------------ packenLoginInformationen()
    /*
     * Informationen werden zum Paket gepackt.
     */
    	
   
    public Bundle packenLoginInformationen()
    {
    	Bundle LogInfor = new Bundle();
    	LogInfor.putInt("ID_Login", this.loginID);
    	LogInfor.putString("Username_Login", this.loginUsername);
    	LogInfor.putString("Passwords_Login", this.pass);
    	LogInfor.putString("Email_Login", this.email);
    	LogInfor.putDouble("Bank_Login", this.doubleGuthaben);
    	LogInfor.putString("Message", this.gratulation);
		return LogInfor;
    }
    

    // ------------------------------------------------------------------------ Fehlermeldung
       /*
        * Fehlermeldung Generierung
        */
       	
    
    
    public void fehlermeldung(String Meldung)
	{
		AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
		b.setMessage(Meldung);
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
			}
		});
		b.create().show();
	}

    

    // ------------------------------------------------------------------------ updateLogin()
       /*
        * Login Daten werden aktualisiert
        */
       	
    
    public void updateLogin()
    {
    	try
	    {		    
		    //Toast.makeText(this, "Update_Login ", Toast.LENGTH_LONG).show();
		    
		    final String METHOD_NAME="UpdateSpieler";
		    final String SOAP_ACTION=NAMESPACE+METHOD_NAME;
		    
		    SoapObject request=new SoapObject(NAMESPACE, METHOD_NAME);
		    SoapObject newSpieler=new SoapObject(NAMESPACE, "_login");
		    
			newSpieler.addProperty("LoginID",this.loginID);			
			newSpieler.addProperty("Username",this.loginUsername);			
			newSpieler.addProperty("Passwords",this.pass);
			newSpieler.addProperty("Email",this.email);
			newSpieler.addProperty("Bank",this.doubleGuthaben);
			request.addSoapObject(newSpieler);
		    
		    SoapSerializationEnvelope envelope= new SoapSerializationEnvelope(SoapEnvelope.VER11);
		    envelope.dotNet=true;
		    envelope.setOutputSoapObject(request);
		    MarshalFloat marshal=new MarshalFloat();
    		marshal.register(envelope);
    		
		  //tạo đối tượng HttpTransportSE
		    HttpTransportSE androidHttpTransport= new HttpTransportSE(URL);
		    androidHttpTransport.call(SOAP_ACTION, envelope);

		    //SoapPrimitive soapPrimitive= (SoapPrimitive)  envelope.getResponse();
		    //Toast.makeText(this, soapPrimitive.toString(), Toast.LENGTH_LONG).show();
	    }
	    catch(Exception e)
	    {
	    	Toast.makeText(this, "Fehler Login Update", Toast.LENGTH_LONG).show();
	    }
    }
}
