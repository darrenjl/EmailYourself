package me.darrenlyons.emailyourself.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import me.darrenlyons.emailyourself.R;

public class MainActivity extends Activity {

    private EditText emailText;
    private Button button;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        emailText = (EditText) findViewById(R.id.email);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("MainActivity", "Saving email: " + emailText.getText());
            }
        });
    }
}