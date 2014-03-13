package edu.calpoly.halicej;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class OutputActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_output);
		
		Intent intent = getIntent();
		String outputString = intent.getStringExtra("OutputString");
		
		TextView outputView = (TextView)findViewById(R.id.outputView);
		if(outputView != null) {
			outputView.setText(outputString);
		}
	}
}
