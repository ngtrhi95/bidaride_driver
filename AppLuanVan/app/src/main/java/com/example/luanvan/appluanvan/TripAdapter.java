package com.example.luanvan.appluanvan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kirihara Miu on 4/7/2017.
 */

public class TripAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Trip> mDataSource;

    public TripAdapter(Context context, ArrayList<Trip> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //1
    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Trip trip = (Trip) getItem(position);
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.trip_list, parent, false);

        TextView userFullName =
                (TextView) rowView.findViewById(R.id.userFullName);
        TextView userPhone =
                (TextView) rowView.findViewById(R.id.phone);
        TextView from =
                (TextView) rowView.findViewById(R.id.from);
        TextView to =
                (TextView) rowView.findViewById(R.id.to);
        TextView time =
                (TextView) rowView.findViewById(R.id.time);

        final double fromLong = trip.getFromLong();
        final double fromLat = trip.getFromLat();
        final double toLong = trip.getToLong();
        final double toLat = trip.getToLat();


        userFullName.setText(trip.getUsername());
        userPhone.setText(trip.getUserPhone());
        from.setText(trip.getTripFrom());
        to.setText(trip.getTripTo());
        time.setText(trip.getCreatedDate().toString());

        return rowView;
    }
}
