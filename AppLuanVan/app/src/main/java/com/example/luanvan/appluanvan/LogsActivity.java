package com.example.luanvan.appluanvan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

import static com.example.luanvan.appluanvan.UserSession.KEY_ID;
import static com.example.luanvan.appluanvan.UserSession.KEY_TOKEN;
import static com.example.luanvan.appluanvan.UserSession.PREFER_NAME;

public class LogsActivity extends AppCompatActivity {

    public static android.content.SharedPreferences SharedPreferences = null;

    private Toolbar toolbar;

    final ArrayList<Trip> listTrip = new ArrayList<Trip>();
    TripAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        SharedPreferences = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);

        String driverID = SharedPreferences.getString(KEY_ID, "");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getTripInfo(driverID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){
            Intent i= new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayTrips() {

        adapter = new TripAdapter(this, listTrip);
        ListView listView = (ListView) findViewById(R.id.listTrip);
        listView.setAdapter(adapter);
    }

    public void getTripInfo(String driverID) {
        String url = "https://appluanvan-apigateway.herokuapp.com/api/trip/getTrip";
        String token = SharedPreferences.getString(KEY_TOKEN, "");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("driverID", driverID);
        params.put("token", token);
        RequestHandle post = client.post(url, params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                JSONArray returnData;
                try {
                    returnData = json.getJSONArray("payload");
                    for (int i = 0; i < returnData.length(); i++) {
                        JSONObject tempJSON = returnData.getJSONObject(i);

                        String tripID = tempJSON.getString("tripID");
                        String userID = tempJSON.getString("userID");
                        String username = tempJSON.getString("userFullname");
                        String userPhone = tempJSON.getString("phone");
                        String from = tempJSON.getString("tripFrom");
                        String to = tempJSON.getString("tripTo");
                        double fromLong = tempJSON.getDouble("fromLong");
                        double fromLat = tempJSON.getDouble("fromLat");
                        double toLong = tempJSON.getDouble("toLong");
                        double toLat = tempJSON.getDouble("toLat");

                        String dateString = tempJSON.getString("createdDate");

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.s'Z'");
                        format.setTimeZone(TimeZone.getTimeZone("GMT"));

                        Date date = new Date();

                        try {
                            date = format.parse(dateString);
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }

                        Trip temp = new Trip(tripID, userID, username, userPhone, from, to, fromLong, fromLat, toLong, toLat, date);
                        listTrip.add(temp);
                    }
                    displayTrips();
                } catch (JSONException err) {
                    Log.e("MYAPP", "JSON exception error", err);
                }
            }

            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject e) {
                try {
                    throw (t);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }
}
