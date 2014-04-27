package me.darrenlyons.emailyourself.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import me.darrenlyons.emailyourself.R;

public class MainActivity extends Activity {

    private EditText emailText;
    private Button button;
    private SharedPreferences preferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        emailText = (EditText) findViewById(R.id.email);
        button = (Button) findViewById(R.id.button);
        preferences = getPreferences(MODE_PRIVATE);
        String email = preferences.getString("email", "");
        emailText.setText(email);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable text = emailText.getText();
                if(isValidEmail(text)){
                    Log.i("MainActivity", "Saving email: " + text);
                    preferences.edit().putString("email", text.toString());
                    Toast.makeText(getApplicationContext(), "New email saved.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("MainActivity", "Invalid email: " + text);
                    Toast.makeText(getApplicationContext(), "Invalid email please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}