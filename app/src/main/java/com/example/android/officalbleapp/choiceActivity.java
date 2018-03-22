package com.example.android.officalbleapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class choiceActivity extends Activity implements BeaconConsumer {
    private Customer customer;
    // TODO: 8/1/17 CHANGE TO THE MINOR VALUE OF THE BEACON YOU WANT TO USE.
    private static int BEACON_NUMBER = 2;

    RequestQueue queue;
    private boolean hasNeverBeenInQueue = true;
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSerializedObject();
        setContentView(R.layout.activity_choice);
        setStartScreen();
        queue = Volley.newRequestQueue(this );


        /*Changes the sampling rate of Beacon:
        Faster Sampling = less accurate measurement
        Slower Sampling = more accurate measurement
         */
        RangedBeacon.setSampleExpirationMilliseconds(500);


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

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    //EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
                    Beacon firstBeacon = beacons.iterator().next();
                    Log.i("Beacon ID3", "Beacon Minor: " + firstBeacon.getId3());
                    if(firstBeacon.getId3().toInt() == BEACON_NUMBER && hasNeverBeenInQueue){
                        postData(customer.getCustomerName());
                        hasNeverBeenInQueue = false;
                    }

                }
            }

        });

        try {
            // TODO: 8/1/17 CHANGE UUID
            beaconManager.startRangingBeaconsInRegion(new Region("44444444-4444-4444-4444-44444444BEAC", null, null, null));
        } catch (RemoteException e) {   }
    }

    public void onVestibuleClicked(View view) {
        Intent i = new Intent();
        Bundle b = new Bundle();

        b.putSerializable("Customer",customer);
        i.putExtras(b);
        i.setClass(this,RangingActivity.class);

        i.putExtra("Customer",customer);
        startActivity(i);

    }

    public void onTransactionClicked(View view) {
        Intent i = new Intent();
        Bundle b = new Bundle();

        b.putSerializable("Customer",customer);
        i.putExtras(b);
        i.setClass(this,FingerprintActivity.class);

        i.putExtra("Customer",customer);
        startActivity(i);
        finish();

    }

    public void onBranchClicked (View view) {

        beaconManager.bind(this);



    }

    public void onLogoutClicked(View view) {
        Intent i = new Intent();
        i.setClass(this,CreateCustomerActivity.class);
        startActivity(i);
        showToast("You are now logged out");
        finish();


    }

    public void showToast(String toastMessage) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, toastMessage, duration);
        toast.show();
    }

    //Sends the customer information to server for queue. Server: /queue
    private void postData(final String name) {
        // TODO: 8/1/17 CHANGE SERVER URL TO YOUR URL YOU WANT TO USE
        StringRequest sr = new StringRequest(Request.Method.POST,"http://beaconapp-abdallahozaifa.c9users.io:8080/queue", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e("IDK",response);
                displayQueueNumber(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("name", name);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
        sr.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void displayQueueNumber(String data) {

        try {
            JSONObject reader = new JSONObject(data);
            String queueNumber = reader.getString("queueNum");
            String waitTime = reader.getString("waitTime");
            String message = "Queue number:" + queueNumber + " \nEstimated wait time: " + waitTime + " minutes";
            sendNotification(message);



        } catch (Exception e) {

        }
    }

    private void sendNotification(String notificationText) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("ChaseWay IoT")
                        .setSmallIcon(R.drawable.ic_launcher).setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationText));


        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, choiceActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);*/
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());



    }

    private void getSerializedObject() {
        Bundle b = this.getIntent().getExtras();
        if (b != null)
            customer = (Customer)b.getSerializable("Customer");

    }

    private void setStartScreen() {

        TextView tCustomerGreeting = (TextView) findViewById(R.id.hello_customer);
        TextView tCustomerName = (TextView) findViewById(R.id.customer_name);
        TextView tCustomerBalance = (TextView) findViewById(R.id.customer_balance);

        tCustomerName.setText(customer.getCustomerName());
        tCustomerBalance.setText(customer.getAccountBalance());

        String language = customer.getLanguage();

        if (language.equals("English"))
            tCustomerGreeting.setText("Hello  ");

        else if (language.equals("French"))
            tCustomerGreeting.setText("Bonjour  ");
        else
            tCustomerGreeting.setText("Hola  ");

    }


}
