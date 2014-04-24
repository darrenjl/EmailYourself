package me.darrenlyons.emailyourself.activities;

/**
 * Code copied from stackoverflow answer here: http://stackoverflow.com/a/18297311/1374923
 */
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

import static android.accounts.AccountManager.get;

public class GmailSender {
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

    public GmailSender(Activity ctx) {
        super();
        initToken(ctx);
    }

    public void initToken(Activity ctx) {

        AccountManager am = get(ctx);

        Account[] accounts = am.getAccountsByType("com.google");

        if(accounts.length==0){
            Log.i("accounts", "no google accounts available");
            return;
        }
        for (Account account : accounts) {
            Log.d("getToken", "account="+account);
        }

        Account me = accounts[0]; //You need to get a google account on the device, it changes if you have more than one


        am.getAuthToken(me, "oauth2:https://mail.google.com/", null, ctx, new AccountManagerCallback<Bundle>(){
            @Override
            public void run(AccountManagerFuture<Bundle> result){
                try{
                    Bundle bundle = result.getResult();
                    token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    user = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d("initToken callback", "token="+token);

                } catch (Exception e){
                    Log.d("test", e.getMessage());
                }
            }
        }, null);

        Log.d("getToken", "token="+token);
        authorised=true;
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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
//         */

        transport.connect(host, port, userEmail, emptyPassword);

        byte[] response = String.format("user=%s\1auth=Bearer %s\1\1",
                userEmail, oauthToken).getBytes();
        response = BASE64EncoderStream.encode(response);

        transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);

        return transport;
    }

    public synchronized void sendMail(String subject, String body, String user,
                                      String oauthToken, String recipients) {
        if(authorised){
        try {

            SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com", 587,
                    user, oauthToken, true);

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
            smtpTransport.sendMessage(message, message.getAllRecipients());

        } catch (Exception e) {
            Log.d("test", e.getMessage(), e);
        }
        }  else {
            Log.i("Send Email", "Not Authorised");
        }
    }

    public boolean isAuthorised() {
        return authorised;
    }
}