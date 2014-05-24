
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
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------	
	public boolean pruefen(int a, int b, int c)
	{
		if ((a<0)||(a>9)||(b<0)||(b>9)||(c<0)||(c>9)){return false;}
		else {return true;}
	}
	
	
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
	


	}
		
//****************************************************************************************************************
//---------------------------------------------------------------------------------------------------------------	
	
	

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
