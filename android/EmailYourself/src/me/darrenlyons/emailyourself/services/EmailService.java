package me.darrenlyons.emailyourself.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import me.darrenlyons.emailyourself.activities.AuthActivity;
import me.darrenlyons.emailyourself.email.GmailSender;

public class EmailService extends Service {

    public static final String TAG = "EmailService";
    private String sharedText;
    private String sharedSubject;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get intent, action and MIME type
        Intent shareIntent = (Intent)intent.getExtras().get("shareIntent");
        String action = shareIntent.getAction();
        String type = shareIntent.getType();
        Log.i(TAG, "Service started with intent action: " + action + ", and type: " + type);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                sharedText = shareIntent.getStringExtra(Intent.EXTRA_TEXT);
                sharedSubject = shareIntent.getStringExtra(Intent.EXTRA_SUBJECT);
                if (sharedText != null) {
                    Log.i(TAG, "Handling share intent with text: " + sharedText + " ---- and subject: " + sharedSubject);
                    AuthoriseAndSendEmailTask task = new AuthoriseAndSendEmailTask();
                    task.execute(new Context[] { this });
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class AuthoriseAndSendEmailTask extends AsyncTask<Context, Void, GmailSender> {
        Service parentService;
        @Override
        protected GmailSender doInBackground(Context... ctx) {
            parentService = (Service) ctx[0];
            GmailSender gmailSender = new GmailSender(parentService);
            return gmailSender;
        }

        @Override
        protected void onPostExecute(GmailSender gmailSender) {
            Log.i("EmailActivity", "Authorised: " + gmailSender.isAuthorised());
            if (gmailSender.isAuthorised()){
                try {
                    gmailSender.sendMail(sharedSubject, sharedText, gmailSender.getUser(), gmailSender.getToken(), gmailSender.getUser());
                } catch (Exception e) {
                    Log.d(TAG, "Exception thrown while trying to send email.");
                    Intent intent = new Intent(parentService, AuthActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("sharedSubject", sharedSubject);
                    intent.putExtra("sharedText", sharedText);
                    startActivity(intent);
                }
            }
            parentService.stopSelf();
        }
    }
}
