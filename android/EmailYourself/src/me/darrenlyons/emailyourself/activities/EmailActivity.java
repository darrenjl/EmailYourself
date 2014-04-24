package me.darrenlyons.emailyourself.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class EmailActivity extends Activity {

    private String sharedText;
    private String sharedSubject;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                if (sharedText != null) {
                    Log.i(this.getLocalClassName(), "Handling share intent with text: " + sharedText + " ---- and subject: " + sharedSubject);
                    AuthoriseAndSendEmailTask task = new AuthoriseAndSendEmailTask();
                    task.execute(new Activity[] { this });
                }
            }
        }

    }

    private class AuthoriseAndSendEmailTask extends AsyncTask<Activity, Void, GmailSender> {
        @Override
        protected GmailSender doInBackground(Activity... ctx) {
            GmailSender gmailSender = new GmailSender(ctx[0]);
            return gmailSender;
        }

        @Override
        protected void onPostExecute(GmailSender gmailSender) {
            Log.i("EmailActivity", "Authorised: " + gmailSender.isAuthorised());
            if(gmailSender.isAuthorised())
                gmailSender.sendMail(sharedSubject, sharedText, gmailSender.getUser(), gmailSender.getToken(), gmailSender.getUser());
        }
    }
}
