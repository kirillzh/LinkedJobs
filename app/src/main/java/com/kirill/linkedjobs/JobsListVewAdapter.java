package com.kirill.linkedjobs;

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
    private final String[] values;

    public JobsListVewAdapter(Context context, String[] values) {
        super(context, R.layout.job_listview, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View companyView = inflater.inflate(R.layout.job_listview, parent, false);
        TextView textView = (TextView) companyView.findViewById(R.id.company_name);
        textView.setText(values[position]);
        return companyView;
    }

}
