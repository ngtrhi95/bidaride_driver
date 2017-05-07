package com.example.luanvan.appluanvan;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import cz.msebera.android.httpclient.Header;

import static com.example.luanvan.appluanvan.UserSession.KEY_FNAME;
import static com.example.luanvan.appluanvan.UserSession.KEY_ID;
import static com.example.luanvan.appluanvan.UserSession.PREFER_NAME;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    public static android.content.SharedPreferences SharedPreferences = null;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtWebsite;
    private Toolbar toolbar;

    private static final String urlNavHeaderBg = "https://s-media-cache-ak0.pinimg.com/originals/52/5c/ff/525cffb2ab3a179aeae213a3e9417a7b.jpg";
    private static final String urlProfileImg = "https://i.ytimg.com/vi/tntOCGkgt98/maxresdefault.jpg";

    public static int navItemIndex = 0;

    private static final String TAG_HOME = "home";
    public static String CURRENT_TAG = TAG_HOME;
    private static String AMQP_URL = "amqp://imtqjgzz:LQWyhmVxKBMgV6ROObew36G07DUs6ZYZ@white-mynah-bird.rmq.cloudamqp.com/imtqjgzz";
    private static String EXCHANGE_NAME_TRIP = "trip_logs";
    private static String EXCHANGE_NAME_LOCATION = "location_logs";

    private boolean shouldLoadHomeFragOnBackPress = true;

    private LocationManager locationManager;

    private int rideBtnStatus = 0;

    UserSession session;

    private Trip data[];

    private Button rideBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new UserSession(getApplicationContext());

        SharedPreferences = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        5000, 10, this);

            } else {
                //Request Location Permission

            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 10, this);

        }

        session.checkLogin();

        initializeContent();

        String driverID = SharedPreferences.getString(KEY_ID, "");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);


        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtWebsite = (TextView) navHeader.findViewById(R.id.website);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        loadNavHeader();

        setUpNavigationView();

        ReceiveDirectLog tripTask = new ReceiveDirectLog();
        tripTask.execute(driverID);
    }

    private void loadNavHeader() {
        String fullname = SharedPreferences.getString(KEY_FNAME, "");
        txtName.setText(fullname);
        txtWebsite.setText("Navigations");

        Glide.with(this).load(urlNavHeaderBg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgNavHeaderBg);

        Glide.with(this).load(urlProfileImg)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);

    }

    public void rideButtonOnClick(View v) {
        final Button rideBtn = (Button) findViewById(R.id.btn_startRide);
        if (rideBtnStatus == 0) {
            String driverID = SharedPreferences.getString(KEY_ID, "");
            String url = "https://fast-hollows-58498.herokuapp.com/driver/updateStatus";
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("driverID", driverID);

            RequestHandle post = client.post(url, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    Logger.d("FUCKU1");
                    rideBtn.setText("Finish Ride");
                    rideBtnStatus = 1;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    Logger.d("FUCKU2");
//                    String message = "";
//                    try {
//                        message = e.getString("message");
//                    } catch (JSONException err) {
//                        Log.e("MYAPP", "JSON exception error", err);
//                    }
//                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.main_content);
            viewGroup.removeAllViews();
            viewGroup.addView(View.inflate(this, R.layout.waiting_customer, null));
        }
    }

    private void initializeContent() {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.main_content);
        viewGroup.addView(View.inflate(this, R.layout.waiting_customer, null));
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawer.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onLocationChanged(Location location) {
        String driverID = SharedPreferences.getString(KEY_ID, "");

        JSONObject obj = new JSONObject();

        try {
            obj.put("latitude", location.getLatitude());
            obj.put("longitude", location.getLongitude());
            obj.put("driverID", driverID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String message = obj.toString();

        EmitLocationLogs locationTask = new EmitLocationLogs();
        locationTask.execute(message);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        if (shouldLoadHomeFragOnBackPress) {
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_logs:
                startActivity(new Intent(MainActivity.this, LogsActivity.class));
                break;
            case R.id.nav_logout:
                session.logoutUser();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            case R.id.nav_about_us:
                drawer.closeDrawers();
                break;
            default:
                break;
        }
        return true;
    }

    public void directionBtnOnclick(View view) {
        double fromlng = data[0].getFromLong();
        double fromlat = data[0].getFromLat();
        double tolng = data[0].getToLong();
        double tolat = data[0].getToLat();

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + fromlat + "," + fromlng + "&daddr=" + tolat +"," +tolng));
        startActivity(intent);
    }

    private class ReceiveDirectLog extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params)  {

            ConnectionFactory factory = new ConnectionFactory();
            try {
                factory.setUri(AMQP_URL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            Connection connection = null;
            try {
                connection = factory.newConnection();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            Channel channel = null;
            try {
                channel = connection.createChannel();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                channel.exchangeDeclare(EXCHANGE_NAME_TRIP, "direct");
            } catch (IOException e) {
                e.printStackTrace();
            }
            String queueName = null;
            try {
                queueName = channel.queueDeclare().getQueue();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                channel.queueBind(queueName, EXCHANGE_NAME_TRIP, params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    Gson g = new Gson();
                    data = g.fromJson(message, Trip[].class);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.main_content);
                            viewGroup.removeAllViews();
                            viewGroup.addView(View.inflate(MainActivity.this, R.layout.ride_info, null));
                            TextView userFullName =
                                    (TextView) findViewById(R.id.userFullNameM);
                            TextView userPhone =
                                    (TextView) findViewById(R.id.phoneM);
                            TextView from =
                                    (TextView) findViewById(R.id.fromM);
                            TextView to =
                                    (TextView) findViewById(R.id.toM);
                            TextView time =
                                    (TextView) findViewById(R.id.timeM);
                            userFullName.setText(data[0].getUsername());
                            if (data[0].getUserPhone() != null) {
                                userPhone.setText(data[0].getUserPhone());
                            }
                            from.setText(data[0].getTripFrom());
                            to.setText(data[0].getTripTo());
                            time.setText(data[0].getCreatedDate().toString());
                        }
                    });
                }
            };
            try {
                channel.basicConsume(queueName, true, consumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class EmitLocationLogs extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            ConnectionFactory factory = new ConnectionFactory();
            try {
                factory.setUri(AMQP_URL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            Connection connection = null;
            try {
                connection = factory.newConnection();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            Channel channel = null;
            try {
                channel = connection.createChannel();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                channel.exchangeDeclare(EXCHANGE_NAME_LOCATION, "direct");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                channel.basicPublish(EXCHANGE_NAME_LOCATION, "location", null, params[0].getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
