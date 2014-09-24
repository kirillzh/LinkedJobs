package com.kirill.linkedjobs;

/**
 * Created by kirill on 9/13/14.
 * My GitHub: https://github.com/ZhukovKirill
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class JobsActivity extends Activity {

    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";
    private static final String AMPERSAND = "&";

    //private static final String DEFAULT_DATE_POSTED = "all";
    private static String JOBS_SEARCH_URL = "https://api.linkedin.com/v1/job-search";
    private static final String PROFILE_URL = "https://api.linkedin.com/v1/people/~";
    private static final String BASE_URL = "http://api.linkedin.com/v1/";
    private static final String JOB_SEARCH_URL = BASE_URL + "job-search";
    private static final String JOBS_URL = BASE_URL + "jobs";
    private static final String PEOPLE_URL = BASE_URL + "people/~";
    private static final String OAUTH_ACCESS_TOKEN_PARAM = "oauth2_access_token";
    private static final String FORMAT_PARAM = "format";
    private static final String JSON_FORMAT_PARAM = FORMAT_PARAM + EQUALS + "json";
    public static JSONArray jobs;

    private TextView jobTitleTextView, companyNameTextView, locationTextView;
    private static Context context;

    private static AsyncHttpClient client = new AsyncHttpClient();

    private static String accessToken = Credentials.ACCESS_TOKEN;
    public static Context getAppContext() {
        return JobsActivity.context;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JobsActivity.context = getApplicationContext();
        setContentView(R.layout.activity_jobs);
        jobTitleTextView = (TextView) findViewById(R.id.job_title);
        companyNameTextView = (TextView) findViewById(R.id.company_name);
        locationTextView = (TextView) findViewById(R.id.location);

        //Request most recent (random/default) jobs when user just launched app and logged in
        SharedPreferences preferences = this.getSharedPreferences("user_info", 0);
        //String accessToken = preferences.getString("accessToken", null);

//        try {
//            getJobs(accessToken, JOBS_SEARCH_URL);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        JOBS_SEARCH_URL += QUESTION_MARK + OAUTH_ACCESS_TOKEN_PARAM + EQUALS + accessToken + AMPERSAND + JSON_FORMAT_PARAM;

        Log.e("TOKEN", JOBS_SEARCH_URL);

        try {
            getJobsTest(JOBS_SEARCH_URL);
            //Log.e("WARN", "" + jobs.getJSONObject(0));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.e("!!!!!!", String.valueOf(jobs));
        String[] companyNames = null, jobLocations = null;
        try {
            companyNames = getCompanyNames(jobs);
            jobLocations = getJobLocations(jobs);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("WARN", Arrays.toString(companyNames));
        Log.e("WARN", Arrays.toString(jobLocations));


    }

    public static void getJobs(String url) throws JSONException {
        RestClient.get(url, null, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e("FAIL", "OnFailure!");

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                if(JobsActivity.jobs == null) {
                    try {
                        Thread.sleep(5000);                 //1000 milliseconds is one second.
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }                }
                Log.e("E", "onSuccess");

                try {
                    Log.e("E", "try");
                    jobs = response.getJSONObject("jobs").getJSONArray("values");

                    //store the values.

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }




    public static void getJobsTest(String url) throws JSONException {
        RestClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Log.e("E", "pass " + i);

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Log.e("E", "fail " + i);
            }
        });

    }




    public static String[] getCompanyNames(JSONArray jobs) throws JSONException {
        String[] companyNames = new String[jobs.length()];
        for(int jobIndex = 0; jobIndex < jobs.length(); jobIndex++) {
            companyNames[jobIndex] = jobs.getJSONObject(jobIndex).getJSONObject("company").getString("name");
        }
        
        return companyNames;
    }


    public static String[] getJobLocations(JSONArray jobs) throws JSONException {
        String[] jobLocations = new String[jobs.length()];
        for(int jobIndex = 0; jobIndex < jobs.length(); jobIndex++) {
            jobLocations[jobIndex] = jobs.getJSONObject(jobIndex).getString("locationDescription");
        }
        return jobLocations;
    }


}

