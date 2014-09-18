package com.kirill.linkedjobs;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
* Created by kirill on 9/18/14.
* My GitHub: https://github.com/ZhukovKirill
*/
public class JobsListVewAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final int layoutResourceId;
    private final String[] values;

    public JobsListVewAdapter(Context context, int layoutResourceId, String[] values) {
        super(context, layoutResourceId, values);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View companyView = convertView;
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        companyView = inflater.inflate(layoutResourceId, parent, false);

        TextView textView = (TextView) companyView.findViewById(R.id.company_name);
        textView.setText(values[position]);
        return companyView;
    }

}
