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
import android.view.Menu;
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
import java.util.Arrays;

public class JobsActivity extends Activity {

    //private static final String DEFAULT_DATE_POSTED = "all";
    private static final String JOBS_SEARCH_URL = "https://api.linkedin.com/v1/job-search";
    private static final String JOB_SEARCH_URL = "https://api.linkedin.com/v1/jobs/";
    private static final String PROFILE_URL = "https://api.linkedin.com/v1/people/~";
    private static final String OAUTH_ACCESS_TOKEN_PARAM = "oauth2_access_token";
    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";
    private TextView jobTitleTextView, companyNameTextView, locationTextView;
    private ProgressDialog pd;
    private static Context context;
    protected String[] companies, locations, jobTitles;
    GetCompaniesRequestAsyncTask asyncTask = new GetCompaniesRequestAsyncTask();

    public static Context getAppContext() {
        return JobsActivity.context;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    void postResult(String output){
        //this you will received result fired from async class of onPostExecute(result) method.
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
        String accessToken = preferences.getString("accessToken", null);
        if (accessToken != null) {



            String jobsUrl = getJobsUrl(accessToken);
            new GetCompaniesRequestAsyncTask().execute(jobsUrl);
            new GetLocationsRequestAsyncTask().execute(jobsUrl);


            //Log.e("WARN", Arrays.toString(asyncTask.companiesArray));
            Log.e("WARN", Arrays.toString(locations));

            //
//            JSONObject data = null;
//            try {
//                data = new GetJobsRequestAsyncTask().execute(jobsUrl).get();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//            if (data != null) {
//
//                try {
//                    JSONArray values = data.getJSONObject("jobs").getJSONArray("values");
//                    String[] companies = new String[values.length()];
//                    String[] locations = new String[values.length()];
//                    String[] jobTitles = new String[values.length()];
//
//                    Log.e("WARN", "" + companies.length + " " + values.length());
//
//
//                    for (int i = 0; i < values.length(); i++) {
//                        JSONObject job = values.optJSONObject(i);
//                        companies[i] = job.getJSONObject("company").getString("name");
//                        locations[i] = job.getString("locationDescription");
//
//
//                        String jobUrl = getJobUrl(accessToken, job.getString("id"));
//                        JSONObject JSONObjectJob = new GetJobsRequestAsyncTask().execute(jobUrl).get();
//                        jobTitles[i] = JSONObjectJob.getJSONObject("position").getString("title");
//
//                    }
//
//                    Log.e("WARN", Arrays.toString(companies));
//                    Log.e("WARN", Arrays.toString(locations));
//                    Log.e("WARN", Arrays.toString(jobTitles));
//
//
//                    ListView companiesListView = (ListView) findViewById(R.id.list_view_jobs);
//                    JobsListVewAdapter companiesListViewAdapter = new JobsListVewAdapter(JobsActivity.this, R.layout.job_listview_item, companies);
//                    companiesListView.setAdapter(companiesListViewAdapter);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//
//            }
        }
    }

    private static String getJobsUrl(String accessToken) {
        return JOBS_SEARCH_URL
                + QUESTION_MARK
                + OAUTH_ACCESS_TOKEN_PARAM
                + EQUALS
                + accessToken;
    }

    private static String getJobUrl(String accessToken, String jobId) {
        return JOB_SEARCH_URL
                + jobId
                + QUESTION_MARK
                + OAUTH_ACCESS_TOKEN_PARAM
                + EQUALS
                + accessToken;
    }


    protected class GetLocationsRequestAsyncTask extends AsyncTask<String, Void, JSONObject> {
        private final Context asyncTaskContext;

        public GetLocationsRequestAsyncTask() {
            this.asyncTaskContext = context;
        }



        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(JobsActivity.this, "", JobsActivity.this.getString(R.string.loading), true);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            if (urls.length > 0) {
                String url = urls[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader("x-li-format", "json");
                try {
                    HttpResponse response = httpClient.execute(httpget);
                    if (response != null) {
                        //If status is OK 200
                        if (response.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(response.getEntity());
                            //Convert the string result to a JSON Object
                            return new JSONObject(result);
                        }
                    }
                } catch (IOException e) {
                    Log.e("Authorize", "Error Http response " + e.getLocalizedMessage());
                } catch (JSONException e) {
                    Log.e("Authorize", "Error Http response " + e.getLocalizedMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            String[] locations = null;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            try {
                JSONArray values = data.getJSONObject("jobs").getJSONArray("values");
                locations = new String[values.length()];
                for (int i = 0; i < values.length(); i++) {
                    JSONObject job = values.optJSONObject(i);
                    locations[i] = job.getString("locationDescription");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JobsActivity jobsActivity = (JobsActivity) asyncTaskContext;
            jobsActivity.locations = locations;
            super.onPostExecute(data);

        }
    }

    protected class GetCompaniesRequestAsyncTask extends AsyncTask<String, Void, JSONObject> {
        public AsyncResponse companiesArray=null;

        private String[] POST(String[] output){
            return output;
        }

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(JobsActivity.this, "", JobsActivity.this.getString(R.string.loading), true);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            if (urls.length > 0) {
                String url = urls[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader("x-li-format", "json");
                try {
                    HttpResponse response = httpClient.execute(httpget);
                    if (response != null) {
                        //If status is OK 200
                        if (response.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(response.getEntity());
                            //Convert the string result to a JSON Object

                            return new JSONObject(result);
                        }
                    }
                } catch (IOException e) {
                    Log.e("Authorize", "Error Http response " + e.getLocalizedMessage());
                } catch (JSONException e) {
                    Log.e("Authorize", "Error Http response " + e.getLocalizedMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            String[] companies = null;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            try {
                JSONArray values = data.getJSONObject("jobs").getJSONArray("values");
                companies = new String[values.length()];

                for (int i = 0; i < values.length(); i++) {
                    JSONObject job = values.optJSONObject(i);
                    companies[i] = job.getJSONObject("company").getString("name");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            companiesArray.postResult(companies);
//            JobsActivity jobsActivity = (JobsActivity) asyncTaskContext;
//            jobsActivity.companies = companies;
//            super.onPostExecute(data);
        }

    }
}

interface AsyncResponse {
    void postResult(String[] output);
}