package com.example.luanvan.appluanvan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

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

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.ParseException;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;

import static com.example.luanvan.appluanvan.UserSession.PREFER_NAME;

/**
 * Created by Kirihara Miu on 4/6/2017.
 */

public class Trip implements Parcelable {
    private String tripID;
    private String userID;
    private String username;
    private String userPhone;
    private String tripFrom;
    private String tripTo;
    private String createdDate;
    private double fromLong;
    private double fromLat;
    private double toLong;
    private double toLat;
    private double price;

    public Trip(String tripID, String userID, String userName, String userPhone, String from, String to, double fromLong, double fromLat, double toLong, double toLat, String time, double price) {
        this.setTripID(tripID);
        this.setUserID(userID);
        this.setUsername(userName);
        this.setUserPhone(userPhone);
        this.setTripFrom(from);
        this.setTripTo(to);
        this.setFromLong(fromLong);
        this.setFromLat(fromLat);
        this.setToLong(toLong);
        this.setToLat(toLat);
        this.setCreatedDate(time);
        this.setPrice(price);
    }

    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getTripFrom() {
        return tripFrom;
    }

    public void setTripFrom(String tripFrom) {
        this.tripFrom = tripFrom;
    }

    public String getTripTo() {
        return tripTo;
    }

    public void setTripTo(String tripTo) {
        this.tripTo = tripTo;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public double getFromLong() {
        return fromLong;
    }

    public void setFromLong(double fromLong) {
        this.fromLong = fromLong;
    }

    public double getFromLat() {
        return fromLat;
    }

    public void setFromLat(double fromLat) {
        this.fromLat = fromLat;
    }

    public double getToLong() {
        return toLong;
    }

    public void setToLong(double toLong) {
        this.toLong = toLong;
    }

    public double getToLat() {
        return toLat;
    }

    public void setToLat(double toLat) {
        this.toLat = toLat;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tripID);
        dest.writeString(this.userID);
        dest.writeString(this.username);
        dest.writeString(this.userPhone);
        dest.writeString(this.tripFrom);
        dest.writeString(this.tripTo);
        dest.writeString(String.valueOf(this.createdDate != null ? this.createdDate : -1));
        dest.writeDouble(this.fromLong);
        dest.writeDouble(this.fromLat);
        dest.writeDouble(this.toLong);
        dest.writeDouble(this.toLat);
    }

    protected Trip(Parcel in) {
        this.tripID = in.readString();
        this.userID = in.readString();
        this.username = in.readString();
        this.userPhone = in.readString();
        this.tripFrom = in.readString();
        this.tripTo = in.readString();
        long tmpCreatedDate = in.readLong();
        this.createdDate = String.valueOf(tmpCreatedDate == -1 ? null : new Date(tmpCreatedDate));
        this.fromLong = in.readDouble();
        this.fromLat = in.readDouble();
        this.toLong = in.readDouble();
        this.toLat = in.readDouble();
    }

    public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel source) {
            return new Trip(source);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
