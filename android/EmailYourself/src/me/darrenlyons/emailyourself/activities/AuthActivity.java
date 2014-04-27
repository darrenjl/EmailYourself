package me.darrenlyons.emailyourself.activities;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import me.darrenlyons.emailyourself.email.GmailSender;

public class AuthActivity extends Activity {
    private String sharedSubject;
    private String sharedText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedSubject = getIntent().getStringExtra("sharedSubject");
        sharedText = getIntent().getStringExtra("sharedText");
        AuthoriseAndSendEmailTask task = new AuthoriseAndSendEmailTask();
        task.execute(new Context[] { this });
    }


    private class AuthoriseAndSendEmailTask extends AsyncTask<Context, Void, GmailSender> {
        Activity parentActivity;
        @Override
        protected GmailSender doInBackground(Context... ctx) {
            parentActivity = (Activity) ctx[0];
            GmailSender gmailSender = new GmailSender(parentActivity);
            return gmailSender;
        }

        @Override
        protected void onPostExecute(GmailSender gmailSender) {
            Log.i("EmailActivity", "Authorised: " + gmailSender.isAuthorised());
            if (gmailSender.isAuthorised()){
                try {
                    gmailSender.sendMail(sharedSubject, sharedText, gmailSender.getUser(), gmailSender.getToken(), gmailSender.getUser());
                } catch (Exception e) {
                    Log.d("Exception thrown while trying to send email.", e.getMessage(), e);
                }
            }
            parentActivity.finish();
        }
    }
}