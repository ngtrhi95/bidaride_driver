package com.example.luanvan.appluanvan;

import android.content.Intent;
import android.content.SharedPreferences;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

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



    @OnClick(R.id.btnLogin) void Login() {
        final String usernameText = username.getText().toString();
        final String passwordText = password.getText().toString();
        String url = "https://fast-hollows-58498.herokuapp.com/driver/login";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("username", usernameText);
        params.put("password", passwordText);
        RequestHandle post = client.post(url, params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                JSONObject payload = null;
                String uID = "";
                String uPhone = "";
                String uEmail = "";
                String uFname = "";
                try {
                    payload = json.getJSONObject("payload");
                    uID = payload.getString("driverID");
                    uPhone = payload.getString("driverPhone");
                    uEmail = payload.getString("driverEmail");
                    uFname = payload.getString("driverFullname");
                } catch (JSONException err) {
                    Log.e("MYAPP", "JSON exception error", err);
                }

                session.createUserLoginSession(usernameText, uID, uPhone, uEmail, uFname,
                        passwordText);

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

                finish();
            }

            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject e) {
                String message = "";
                try {
                    message = e.getString("message");
                } catch (JSONException err) {
                    Log.e("MYAPP", "JSON exception error", err);
                }
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

}