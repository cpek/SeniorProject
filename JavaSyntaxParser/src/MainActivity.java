package edu.calpoly.halicej;

import java.io.BufferedReader;
import java.io.StringReader;

import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private String code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id) {
			case R.id.cut:
				Toast.makeText(this, "Cut", Toast.LENGTH_LONG).show();
				break;
			case R.id.copy:
				Toast.makeText(this, "Copy", Toast.LENGTH_LONG).show();
				break;
			case R.id.paste:
				Toast.makeText(this, "Paste", Toast.LENGTH_LONG).show();
				break;
			case R.id.find:
				Toast.makeText(this, "Find", Toast.LENGTH_LONG).show();
				break;
			case R.id.compile:
				Toast.makeText(this, "Compile", Toast.LENGTH_LONG).show();
				EditText editor = (EditText)findViewById(R.id.editor);
				code = editor.getText().toString();
				BufferedReader br = new BufferedReader(new StringReader(code));
				code = SyntaxParser.parseTypeDeclaration(br);
				editor.setText(Html.fromHtml(code));
				/*Intent intent = new Intent(this, OutputActivity.class);
				intent.putExtra("OutputString", outputString);
				startActivity(intent);*/
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  savedInstanceState.putString("code", code);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  code = savedInstanceState.getString("inputString");
	  EditText editor = (EditText)findViewById(R.id.editor);
	  editor.setText(Html.fromHtml(code));
	}
}
