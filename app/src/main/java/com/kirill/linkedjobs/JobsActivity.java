package com.kirill.linkedjobs;

/**
 * Created by kirill on 9/13/14.
 * My GitHub: https://github.com/ZhukovKirill
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class JobsActivity extends Activity {

    //private static final String DEFAULT_DATE_POSTED = "all";

    private static final String JOB_SEARCH_URL = "https://api.linkedin.com/v1/job-search";
    private static final String PROFILE_URL = "https://api.linkedin.com/v1/people/~";
    private static final String OAUTH_ACCESS_TOKEN_PARAM ="oauth2_access_token";
    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";
    private TextView jobTitleTextView, companyNameTextView, locationTextView;
    private ProgressDialog pd;
    private static Context context;

    public static Context getAppContext() {
        return JobsActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JobsActivity.context = getApplicationContext();
        setContentView(R.layout.activity_jobs);
        jobTitleTextView = (TextView) findViewById(R.id.job_title);
        companyNameTextView = (TextView) findViewById(R.id.company_name);
        locationTextView = (TextView) findViewById(R.id.location);

        //Request basic profile of the user
        SharedPreferences preferences = this.getSharedPreferences("user_info", 0);
        String accessToken = preferences.getString("accessToken", null);
        if(accessToken!=null){
            String jobsUrl = getJobsUrl(accessToken);
            new GetJobsRequestAsyncTask().execute(jobsUrl);
        }

        Button btnExample = (Button) findViewById(R.id.search_button);
        btnExample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something here
            }
        });

    }
//    private static String getProfileUrl(String accessToken){
//        return PROFILE_URL
//                +QUESTION_MARK
//                +OAUTH_ACCESS_TOKEN_PARAM
//                +EQUALS
//                +accessToken;
//
//    }

    private static String getJobsUrl(String accessToken) {
        return JOB_SEARCH_URL
                +QUESTION_MARK
                +OAUTH_ACCESS_TOKEN_PARAM
                +EQUALS
                +accessToken;
    }

    protected class GetJobsRequestAsyncTask extends AsyncTask<String, Void, JSONObject>{
        @Override
        protected void onPreExecute(){
            pd = ProgressDialog.show(JobsActivity.this, "", JobsActivity.this.getString(R.string.loading),true);
        }
        @Override
        protected JSONObject doInBackground(String... urls) {
            if(urls.length>0){
                String url = urls[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader("x-li-format", "json");
                try{
                    HttpResponse response = httpClient.execute(httpget);
                    if(response!=null){
                        //If status is OK 200
                        if(response.getStatusLine().getStatusCode()==200){
                            String result = EntityUtils.toString(response.getEntity());
                        //Convert the string result to a JSON Object
                            return new JSONObject(result);
                        }
                    }
                }catch(IOException e){
                    Log.e("Authorize","Error Http response "+e.getLocalizedMessage());
                } catch (JSONException e) {
                    Log.e("Authorize","Error Http response "+e.getLocalizedMessage());
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject data){
            if(pd!=null && pd.isShowing()){
                pd.dismiss();
            }
            if(data!=null){

                String[] companies;

                try {
                    JSONArray values = data.getJSONObject("jobs").getJSONArray("values");
                    companies = new String[values.length()];
                    Log.e("WARN", "" + companies.length + " " + values.length());

                    for(int i = 0; i < values.length(); i++) {
                        JSONObject job = values.optJSONObject(i);
                        String companyName = job.getJSONObject("company").getString("name");
                        companies[i] = companyName;
                    }

                    ListView companiesListView = (ListView) findViewById(R.id.list_view_jobs);
                    JobsListVewAdapter companiesListViewAdapter = new JobsListVewAdapter(JobsActivity.this, R.layout.job_listview_item, companies);
                    companiesListView.setAdapter(companiesListViewAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
