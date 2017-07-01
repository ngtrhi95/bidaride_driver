package com.example.luanvan.appluanvan;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import com.bumptech.glide.load.resource.SimpleResource;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.HttpGet;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
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
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.entity.StrictContentLengthStrategy;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

import static com.example.luanvan.appluanvan.UserSession.KEY_FNAME;
import static com.example.luanvan.appluanvan.UserSession.KEY_ID;
import static com.example.luanvan.appluanvan.UserSession.KEY_TOKEN;
import static com.example.luanvan.appluanvan.UserSession.PREFER_NAME;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public static android.content.SharedPreferences SharedPreferences = null;

    // Google client to interact with Google API

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters


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
    private static String EXCHANGE_NAME_NOTIFICATION = "notification_logs";
    private double longitude;
    private double latitude;

    private boolean shouldLoadHomeFragOnBackPress = true;

    private LocationManager locationManager;

    private static int rideBtnStatus;

    UserSession session;

    private static Trip data;

    private Button rideBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new UserSession(getApplicationContext());

        SharedPreferences = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);

        session.checkLogin();

        String driverID = SharedPreferences.getString(KEY_ID, "");

        initializeContent();

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

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        togglePeriodicLocationUpdates();
                    }
                },
                5000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text


            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d("Location", "Periodic location updates started!");

        } else {
            // Changing the button text

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d("Loc", "Periodic location updates stopped!");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
    }
    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("location", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

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
        rideBtn = (Button) findViewById(R.id.btn_startRide);
        if (rideBtnStatus == 0) {
            String driverID = SharedPreferences.getString(KEY_ID, "");
            String token = SharedPreferences.getString(KEY_TOKEN, "");
            //String url = "https://appluanvan-apigateway.herokuapp.com/api/updateStatus";
            //https://appluanvan-apigateway.herokuapp.com/api/trip/create


            CreateTrip createTrip = new CreateTrip();
            createTrip.execute("https://appluanvan-apigateway.herokuapp.com/api/trip/create");

        } else {
            data = null;
            rideBtnStatus = 0;
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.main_content);
            viewGroup.removeAllViews();
            viewGroup.addView(View.inflate(this, R.layout.waiting_customer, null));

            UpdateStatusDriver n = new UpdateStatusDriver();
            n.execute("https://appluanvan-apigateway.herokuapp.com/api/driver/updateStatus");

        }
    }

    private void initializeContent() {

        if (data != null) {
            ViewGroup viewGroupContent = (ViewGroup) findViewById(R.id.main_content);
            viewGroupContent.removeAllViews();
            viewGroupContent.addView(View.inflate(MainActivity.this, R.layout.ride_info, null));
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
            TextView price =
                    (TextView) findViewById(R.id.costM);
            userFullName.setText(data.getUsername());
            if (data.getUserPhone() != null) {
                userPhone.setText(data.getUserPhone());
            }
            from.setText(data.getTripFrom());
            to.setText(data.getTripTo());
            time.setText(data.getCreatedDate().toString());
            price.setText(String.valueOf(data.getPrice()));
        } else {
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.main_content);
            viewGroup.addView(View.inflate(this, R.layout.waiting_customer, null));
        }
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

//    @Override
//    public void onLocationChanged(Location location) {
//        String driverID = SharedPreferences.getString(KEY_ID, "");
//
//        JSONObject obj = new JSONObject();
//        Toast.makeText(getApplicationContext(), "latitude:" + location.getLatitude() + ", longitude:" + location.getLongitude(), Toast.LENGTH_LONG).show();
//        try {
//            obj.put("latitude", location.getLatitude());
//            obj.put("longitude", location.getLongitude());
//            obj.put("driverID", driverID);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        String message = obj.toString();
//
//        EmitLocationLogs locationTask = new EmitLocationLogs();
//        locationTask.execute(message);
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        startActivity(intent);
//        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
//                Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 1: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }

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
                //drawer.closeDrawers();
                startActivity(new Intent(MainActivity.this, AboutusActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    public void directionBtnOnclick(View view) {
        double fromlng = data.getFromLong();
        double fromlat = data.getFromLat();
        double tolng = data.getToLong();
        double tolat = data.getToLat();

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + fromlat + "," + fromlng + "&daddr=" + tolat + "," + tolng));
        startActivity(intent);
    }

    public void callPhoneToCustommer(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + data.getUserPhone()));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 10);
            return;
        }else {
            try{
                startActivity(callIntent);
            }
            catch (android.content.ActivityNotFoundException ex){
                Toast.makeText(getApplicationContext(),"yourActivity is not founded",Toast.LENGTH_SHORT).show();
            }
        }
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
                channel.exchangeDeclare(EXCHANGE_NAME_NOTIFICATION, "direct");
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
                channel.queueBind(queueName, EXCHANGE_NAME_NOTIFICATION, params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    Gson g = new Gson();
                    data = g.fromJson(message, Trip.class);
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(MainActivity.this)
                                    .setSmallIcon(R.drawable.scooter)
                                    .setContentTitle("Confirm")
                                    .setPriority(Notification.PRIORITY_HIGH)
                                    .setContentText("A customer has chosen you to be your driver. Please confirm your ride now!");
                    Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
                    stackBuilder.addParentStack(LogsActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent contentIntent =
                            PendingIntent.getActivity(MainActivity.this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(contentIntent);
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    mBuilder.setSound(alarmSound);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    mNotificationManager.notify(0, mBuilder.build());

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
                            TextView price =
                                    (TextView) findViewById(R.id.costM);
                            userFullName.setText(data.getUsername());
                            if (data.getUserPhone() != null) {
                                userPhone.setText(data.getUserPhone());
                            }
                            from.setText(data.getTripFrom());
                            to.setText(data.getTripTo());
                            time.setText(data.getCreatedDate().toString());
                            price.setText(String.valueOf(data.getPrice()) + " VNĐ");
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

    private class UpdateStatusDriver extends  AsyncTask<String, Void, String>{
        String driverID = SharedPreferences.getString(KEY_ID, "");
        String token = SharedPreferences.getString(KEY_TOKEN, "");

        @Override
        protected String doInBackground(String... params) {
            URI website = null;
            HttpClient client = new DefaultHttpClient();

            List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("driverID", driverID));
            postParameters.add(new BasicNameValuePair("token", token));
            try {
                website = new URI((String) params[0]);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            HttpPost request = new HttpPost((String)params[0]);
            UrlEncodedFormEntity entity = null;
            try {
                entity = new UrlEncodedFormEntity(postParameters, HTTP.UTF_8);
                request.setEntity(entity);
                HttpResponse response_http = client.execute(request);
                StringBuffer stringBuffer = new StringBuffer("");
                BufferedReader bufferedReader = null;
                bufferedReader = new BufferedReader(new InputStreamReader(response_http.getEntity().getContent()));

                String line = "";
                String LineSeparator = System.getProperty("line.separator");

                while ((line = bufferedReader.readLine())!= null) {
                    stringBuffer.append(line + LineSeparator);
                }
                bufferedReader.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class CreateTrip  extends  AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            String driverID = SharedPreferences.getString(KEY_ID, "");
            String token = SharedPreferences.getString(KEY_TOKEN, "");
            URI website = null;
            HttpClient client = new DefaultHttpClient();
            List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("userID", data.getUserID()));
            postParameters.add(new BasicNameValuePair("driverID", driverID));
            postParameters.add(new BasicNameValuePair("tripFrom", data.getTripFrom()));
            postParameters.add(new BasicNameValuePair("tripTo", data.getTripTo()));
            postParameters.add(new BasicNameValuePair("fromLong", String.valueOf(data.getFromLong())));
            postParameters.add(new BasicNameValuePair("fromLat", String.valueOf(data.getFromLat())));
            postParameters.add(new BasicNameValuePair("toLong", String.valueOf(data.getToLong())));
            postParameters.add(new BasicNameValuePair("toLat", String.valueOf(data.getToLat())));
            postParameters.add(new BasicNameValuePair("price", String.valueOf(data.getPrice())));
            postParameters.add(new BasicNameValuePair("token", token));
            try {
                website = new URI((String) params[0]);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            HttpPost request = new HttpPost((String)params[0]);
            UrlEncodedFormEntity entity = null;
            try {
                entity = new UrlEncodedFormEntity(postParameters,HTTP.UTF_8);
                request.setEntity(entity);
                HttpResponse response_http = client.execute(request);

                StringBuffer stringBuffer = new StringBuffer("");
                BufferedReader bufferedReader = null;
                bufferedReader = new BufferedReader(new InputStreamReader(response_http.getEntity().getContent()));

                String line = "";
                String LineSeparator = System.getProperty("line.separator");

                while ((line = bufferedReader.readLine())!= null) {
                    stringBuffer.append(line + LineSeparator);
                }
                bufferedReader.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (rideBtnStatus == 0) {
                UpdateStatusDriver n = new UpdateStatusDriver();
                n.execute("https://appluanvan-apigateway.herokuapp.com/api/driver/updateStatus");
            }
            rideBtnStatus = 1;
            rideBtn.setText("Finish Ride");
        }
    }
}
