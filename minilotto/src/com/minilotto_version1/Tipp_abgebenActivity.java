package com.minilotto;




import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

public class Tipp_abgebenActivity extends Activity {

	private Spinner spinner1;
	private Spinner spinner2;
	private Spinner spinner3;
	

	protected OnItemSelectedListener onSpinnerItemSelect = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// wann immer ein Spinner geändert wird,
			// passe den Tipp an
			updatePhrase();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// wann immer ein Spinner geändert wird,
			// passe den Tipp an
			updatePhrase();
		}

		private void updatePhrase() {
			String text;
			text = (String) spinner1.getSelectedItem() + " "
					+ (String) spinner2.getSelectedItem() + " "
					+ (String) spinner3.getSelectedItem() + "!";
				

			TextView satz = (TextView) findViewById(R.id.txt_ganzer_satz);
			satz.setText(text);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.selbst_dreschen);

		spinner1 = (Spinner) findViewById(R.id.sp_text_teil1);
		spinner1.setOnItemSelectedListener(onSpinnerItemSelect);

		spinner2 = (Spinner) findViewById(R.id.sp_text_teil2);
		spinner2.setOnItemSelectedListener(onSpinnerItemSelect);

		spinner3 = (Spinner) findViewById(R.id.sp_text_teil3);
		spinner3.setOnItemSelectedListener(onSpinnerItemSelect);

		

	}

}
