package com.example.android.officalbleapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class TransactionActivity extends Activity implements BeaconConsumer {
    private Customer customer;
    RequestQueue queue;
    private int transAmount;
    private int balance;

    private static int DENOMINATION_MIN = 10;
    private static int WITHDRAWAL_LIMIT = 10000;
    
    private boolean isWithinBeaconRange = false;

    // TODO: 8/1/17 CHANGE THE MINOR TO THE BEACON YOU WANT TO USE. 
    private static int BEACON_NUMBER = 1;
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        queue = Volley.newRequestQueue(this);
        beaconManager.bind(this);

        getSerializedObject();
        //RangedBeacon.setSampleExpirationMilliseconds(500);



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }


    // Beacon search and actions taken here.
    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {

                    Beacon firstBeacon = beacons.iterator().next();
                    Log.i("Beacon ID", "Beacon Minor: " + firstBeacon.getId3());
                    if(firstBeacon.getId3().toInt() == BEACON_NUMBER){
                        Integer distance = (int)firstBeacon.getDistance();
                        logToDisplay(distance.toString());

                        if (distance < 2 ) {
                           isWithinBeaconRange = true;
                        }

                        else
                            isWithinBeaconRange = false;

                    }

                }
            }

        });

        try {
            // TODO: 8/1/17 CHANGE UUID TO THE ONE YOU WANT TO USE 
            beaconManager.startRangingBeaconsInRegion(new Region("44444444-4444-4444-4444-44444444BEAC", null, null, null));
        } catch (RemoteException e) {   }
    }

    public void onWithdrawalClicked(View view) {
        EditText transactionField = (EditText) TransactionActivity.this.findViewById(R.id.withdrawal_amount);

        String transactionAmount =  transactionField.getText().toString();



            if (isTransactionFieldEmpty(transactionAmount)) {
                showToast("Please enter an amount to withdraw");


            }

            else if (notMinDenomination(transactionAmount)) {

                showToast("Please enter an amount in increments of $10 ");
                transactionField.setText("");
            }

            else if (exceedsWithdrawalLimit(transactionAmount)) {

                showToast("Withdrawal limit is $10,000");
                transactionField.setText("");
            }

            else if (!hasEnoughMoney(transactionAmount)) {
                showToast("Insufficient funds");
                transactionField.setText("");
            }

            else if (hasEnoughMoney(transactionAmount) && isWithinBeaconRange ) {

                balance = Integer.parseInt(customer.getAccountBalance());
                transAmount = Integer.parseInt(transactionAmount);

                sendToServer(customer.getCustomerName(), customer.getLanguage(), transactionAmount);


            }

            else {
                showToast("Please get within range of ATM");
                transactionField.setText("");
            }



    }



    private boolean hasEnoughMoney(String amount) {

        int transAmount = Integer.parseInt(amount);
        int balance = Integer.parseInt(customer.getAccountBalance());

        if (transAmount > balance)
        {
            return false;
        }

        else
        {
            return true;
        }

    }

    private boolean notMinDenomination(String amount) {



            int withdrawalAmount = Integer.parseInt(amount);

            if (withdrawalAmount % DENOMINATION_MIN != 0) {
                return true;
            } else
                return false;


    }

    private boolean isTransactionFieldEmpty (String amount) {

        return amount.isEmpty();
    }

    private boolean exceedsWithdrawalLimit(String amount) {
        int withdrawalAmount  =  Integer.parseInt(amount);

        if (withdrawalAmount > WITHDRAWAL_LIMIT) {
            return true;
        }
        else
            return false;
    }

    public void completeTransaction(int balance, int amount) {

        String newBalance = ((Integer)(balance - amount)).toString();
        customer.setAccountBalance(newBalance);

    }

    public void sendToServer(final String name, final String languages, final String amount) {

        // TODO: 8/1/17 CHANGE THE URL TO THE SERVER YOU WANT TO USE 
        StringRequest sr = new StringRequest(Request.Method.POST, "http://beaconapp-abdallahozaifa.c9users.io:8080/transaction", new Response.Listener<String>() {

            @Override
                public void onResponse(String response) {

                Log.e("ON RESPONSE" , response);
                completeTransaction(balance, transAmount);
                showToast("Transaction Complete");
                startNewActivity(choiceActivity.class);
                finish();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Error: ", error);

                }

            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("name", name);
                    params.put("languages", languages);
                    params.put("amount", amount);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };
            queue.add(sr);
          sr.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }

    public void showToast(String toastMessage)
    {
        Context context = getApplicationContext();
        CharSequence text = toastMessage;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void startNewActivity(Class newAct) {

        Intent i = new Intent();
        Bundle b = new Bundle();

        b.putSerializable("Customer", customer);
        i.putExtras(b);
        i.setClass(this, newAct);
        i.putExtra("Customer", customer);
        startActivity(i);
    }

    private void getSerializedObject() {
        Bundle b = this.getIntent().getExtras();
        if (b != null)
            customer = (Customer)b.getSerializable("Customer");

    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView textView = (TextView) TransactionActivity.this.findViewById(R.id.test);
                textView.setText(line);
            }
        });
    }


}
