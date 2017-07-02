package com.example.luanvan.appluanvan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Networking n = new Networking();
        n.execute("https://appluanvan-apigateway.herokuapp.com/api/trip/getTrip");
    }
    int status;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    public  class Networking extends AsyncTask {
        private ProgressDialog progressDialog;
        JSONObject response;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LogsActivity.this, "Please wait.",
                    "Getting History..!", true);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                response = getJson((String) params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressDialog.dismiss();
            try {
                status = response.getInt("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (status != 200) {
                Toast.makeText(LogsActivity.this, "We can't get the history, sorry...", Toast.LENGTH_SHORT).show();
            }
            else {
                JSONArray returnData;
                try {
                    returnData = response.getJSONArray("payload");
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
                        double price = tempJSON.getDouble("price");

                        String dateString = tempJSON.getString("createdDate");

                        Trip temp = new Trip(tripID, userID, username, userPhone, from, to, fromLong, fromLat, toLong, toLat, dateString, price);
                        listTrip.add(temp);
                    }
                    displayTrips();
                } catch (JSONException err) {
                    Log.e("MYAPP", "JSON exception error", err);
                }
            }
        }
    }

    private JSONObject getJson(String url) throws JSONException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();

        String driverID = SharedPreferences.getString(KEY_ID, "");
        String token = SharedPreferences.getString(KEY_TOKEN, "");


        postParameters.add(new BasicNameValuePair("driverID", driverID));
        postParameters.add(new BasicNameValuePair("token", token));


        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer("");
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);

            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            String LineSeparator = System.getProperty("line.separator");

            while ((line = bufferedReader.readLine())!= null) {
                stringBuffer.append(line + LineSeparator);
            }
            bufferedReader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject(stringBuffer.toString());
    }
}
