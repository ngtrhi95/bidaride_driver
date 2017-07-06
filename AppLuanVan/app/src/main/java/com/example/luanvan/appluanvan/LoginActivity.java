package com.example.luanvan.appluanvan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static com.example.luanvan.appluanvan.MainActivity.navItemIndex;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.btnLogin) Button btnLogin;
    @BindView(R.id.username) EditText username;
    @BindView(R.id.password) EditText password;

    UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        session = new UserSession(getApplicationContext());

        ButterKnife.bind(this);
    }


    String usernameText, passwordText;
    int status;
    @OnClick(R.id.btnLogin) void Login() {
        usernameText = username.getText().toString();
        passwordText = password.getText().toString();
        if (usernameText.length() == 0 || passwordText.length() == 0){
            Toast.makeText(this, "Please fill in username and password!", Toast.LENGTH_SHORT).show();
        }
        else {
            Networking n = new Networking();
            n.execute("https://appluanvan-apigateway.herokuapp.com/api/driver/login");
        }
    }
    public  class Networking extends AsyncTask {
        private ProgressDialog progressDialog;
        JSONObject response;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this, "Please wait.",
                    "Log in..!", true);
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
                Toast.makeText(LoginActivity.this, "Username or Password is incorrect!", Toast.LENGTH_SHORT).show();
            }
            else {
                JSONObject payload = null;
                String uID = "";
                String uPhone = "";
                String uEmail = "";
                String uFname = "";
                String uToken = "";
                try {
                    payload = response.getJSONObject("payload");
                    uID = payload.getString("driverID");
                    uPhone = payload.getString("driverPhone");
                    uEmail = payload.getString("driverEmail");
                    uFname = payload.getString("driverFullname");
                    uToken = response.getString("token");
                    Logger.d(uToken);
                } catch (JSONException err) {
                    Log.e("MYAPP", "JSON exception error", err);
                }

                session.createUserLoginSession(usernameText, uID, uPhone, uEmail, uFname,
                        passwordText, uToken);

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

                finish();
            }
        }
    }

    private JSONObject getJson(String url) throws JSONException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();


                postParameters.add(new BasicNameValuePair("username", usernameText));
                postParameters.add(new BasicNameValuePair("password", passwordText));


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
    @Override
    public void onBackPressed() {
    }
}