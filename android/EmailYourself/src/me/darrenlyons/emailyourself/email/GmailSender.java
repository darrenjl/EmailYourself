package me.darrenlyons.emailyourself.email;

/**
 * Code copied from stackoverflow answer here: http://stackoverflow.com/a/18297311/1374923
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;

import static android.accounts.AccountManager.get;

public class GmailSender {
    public static final String TAG = "GmailSender";
    private Session session;
    private String user;
    private String token;
    private boolean authorised;

    public String getUser() {
        return user;
    }


    public String getToken() {
        return token;
    }

    public GmailSender(Context ctx) {
        super();
        initToken(ctx);
    }

    public void initToken(Context ctx) {

        AccountManager am = get(ctx);

        Account[] accounts = am.getAccountsByType("com.google");

        if (accounts.length == 0) {
            Log.i("accounts", "no google accounts available");
            return;
        }
        for (Account account : accounts) {
            Log.d("getToken", "account=" + account);
        }

        Account me = accounts[0]; //You need to get a google account on the device, it changes if you have more than one

        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> result) {
                try {
                    Bundle bundle = result.getResult();
                    if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                        Log.i(TAG, "Need to authorise");
                    }
                    token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    user = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d("initToken callback", "token=" + token);

                } catch (Exception e) {
                    Log.d("Exception thrown while authenticating.", e.getMessage());
                    return;
                }
            }
        };

        if (ctx instanceof Service) {
            am.getAuthToken(me, "oauth2:https://mail.google.com/", true, callback, null);
        } else if (ctx instanceof Activity) {
            Activity activity = (Activity) ctx;
            Log.i(TAG, "authenticating from activity");
            // Get token
            AccountManagerFuture<Bundle> future = am.getAuthToken(me, "oauth2:https://mail.google.com/", null, activity, null, null);
            Bundle bundle = null;
            try {
                bundle = future.getResult();
                String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                Log.i(TAG, "Account type: " + bundle.getString(AccountManager.KEY_ACCOUNT_TYPE));

                // invalidate the token since it may have expired.
                am.invalidateAuthToken("com.google", authToken);

                // Get token again
                future = am.getAuthToken(me, "oauth2:https://mail.google.com/", null, activity, null, null);
                bundle = future.getResult();
                token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.d("getToken", "token=" + token);
        authorised = true;
    }


    public SMTPTransport connectToSmtp(String host, int port, String userEmail,
                                       String oauthToken, boolean debug) throws Exception {

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.sasl.enable", "false");

        session = Session.getInstance(props);
        session.setDebug(debug);

        final URLName unusedUrlName = null;
        SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
        // If the password is non-null, SMTP tries to do AUTH LOGIN.
        final String emptyPassword = null;

//        /* enable if you use this code on an Activity (just for test) or use the AsyncTask
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//         */

        transport.connect(host, port, userEmail, emptyPassword);

        byte[] response = String.format("user=%s\1auth=Bearer %s\1\1",
                userEmail, oauthToken).getBytes();
        response = BASE64EncoderStream.encode(response);

        transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);

        return transport;
    }

    public synchronized void sendMail(String subject, String body, String user,
                                      String oauthToken, String recipients) throws Exception {
        if (authorised) {
            SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com", 587,
                    user, oauthToken, true);
            Log.i(TAG, "finished connecting to smtp");

            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(
                    body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress(user));
            message.setSubject(subject);
            message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(recipients));
            Log.i(TAG, "about to send email");
            smtpTransport.sendMessage(message, message.getAllRecipients());

        } else {
            Log.i("Send Email", "Not Authorised");
        }
    }

    public boolean isAuthorised() {
        return authorised;
    }
}