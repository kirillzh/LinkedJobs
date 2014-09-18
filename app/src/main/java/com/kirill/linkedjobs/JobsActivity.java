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
import java.util.ArrayList;
import java.util.List;

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


                try {
                    Log.e("WARN", "Nope");
                    JSONArray jobs = data.getJSONObject("jobs").getJSONObject("values").toJSONArray(data.getJSONObject("jobs").getJSONObject("values").names());
                    Log.e("WARN", "Got jobs JSON FILE!!!");
//                    Iterator<String> iterator = jobs.keys();
//                    while (iterator.hasNext()) {
//                        String key = iterator.next();
//                        try {
//                            Object value = jobs.get(key);
//                            Log.e("WARN", "" + value);
//                        } catch (JSONException e) {
//                            // Something went wrong!
//                        }
//                    }

//                    for(int i = 0; i < jobs.length(); i++) {
//                        JSONObject job = jobs.getJSONObject(i);
//
////                        String jobTitleString = job.getJSONObject("jobPoster").getString("descriptionSnippet");
////                        jobTitleString = jobTitleString.split("")[0];
////                        jobTitleTextView.setText(jobTitleString);
//
//                        String companyNameString = job.getJSONObject("company").getString("name");
//                        listOfCompanies.add(companyNameString);
//                        //companyNameTextView.setText(companyNameString);
////                        String locationString = job.getJSONObject("jobPoster").getString("locationDescription");
////                        locationTextView.setText(locationString);
//                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                List<String> listOfCompanies = new ArrayList<String>();
                String[] companies = listOfCompanies.toArray(new String[listOfCompanies.size()]);
                ListView companiesListView = (ListView) findViewById(R.id.list_view_jobs);
                JobsListVewAdapter companiesListViewAdapter = new JobsListVewAdapter(JobsActivity.getAppContext(), companies);

//                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                JsonParser jp = new JsonParser();
//                JsonElement je = jp.parse(String.valueOf(data));
//                String prettyJsonString = gson.toJson(je);




            }
        }
    }
}
