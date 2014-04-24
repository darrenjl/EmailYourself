package me.darrenlyons.emailyourself.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import me.darrenlyons.emailyourself.email.GmailSender;
import me.darrenlyons.emailyourself.services.EmailService;

public class EmailActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, EmailService.class);
        intent.putExtra("shareIntent", getIntent());
        startService(intent);

        finish();
    }
}
