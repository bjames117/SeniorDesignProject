package com.example.android.officalbleapp;
//package info.androidhive.fingerprint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;


/**
 * Created by whit3hawks on 11/16/16.
 */
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {


    private Context context;
    private Customer customer;


    // Constructor
    public FingerprintHandler(Context mContext) {
        context = mContext;
    }
    //customer= mCustomer;


    public FingerprintHandler(Context mContext, Customer mCustomer) {
        context = mContext;
        customer = mCustomer;
    }


    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }


    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        this.update("Fingerprint Authentication error\n" + errString, false);
    }


    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        this.update("Fingerprint Authentication help\n" + helpString, false);
    }


    @Override
    public void onAuthenticationFailed() {
        this.update("Fingerprint Authentication failed.", false);
    }


    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

        //Intent i = new Intent();
        Bundle b = new Bundle();

        this.update("Fingerprint Authentication succeeded.", true);
        Intent i = new Intent();

        b.putSerializable("Customer",customer);
        i.putExtras(b);
        i.setClass(context,TransactionActivity.class);
        i.putExtra("Customer",customer);

        context.startActivity(i);
        //finish();

    }

    public void update(String e, Boolean success){
        TextView textView = (TextView) ((Activity)context).findViewById(R.id.errorText);
        textView.setText(e);
        if(success){
            textView.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
        }
    }

}
/**
 * Created by Brendon James on 2/23/2018.
 */


