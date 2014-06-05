package edu.calpoly.halicej;

import java.io.BufferedReader;
import java.io.StringReader;

import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {
	EditText editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editor = (EditText)findViewById(R.id.editor);
		loadSavedPreferences();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		String code;
		
		switch(id) {
			case R.id.undo:
				code = editor.getText().toString();
				loadSavedPreferences();
				savePreferences(code);
				break;
			case R.id.redo:
				code = editor.getText().toString();
				loadSavedPreferences();
				savePreferences(code);
				break;
			case R.id.compile:
				UpdateEditor(editor.getText().toString());
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void savePreferences(String value) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = pref.edit();
		editor.putString("code", value);
		editor.commit();
	}
	
	private void loadSavedPreferences() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String code = pref.getString("code", null);
		if(code != null) {
			EditText editText = (EditText)findViewById(R.id.editor);
			editText.setText(Html.fromHtml(code));
		}
	}
	
	private void UpdateEditor(String code) {
		savePreferences(code);
		
		BufferedReader br = new BufferedReader(new StringReader(code));
		code = SyntaxParser.parseTypeDeclaration(br);
		
		editor.setText(Html.fromHtml(code));
	}
}
