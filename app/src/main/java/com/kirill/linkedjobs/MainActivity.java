package com.kirill.linkedjobs;

/**
 * Created by kirill on 9/13/14.
 * My GitHub: https://github.com/ZhukovKirill
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
public class MainActivity extends Activity {
/*CONSTANT FOR THE AUTHORIZATION PROCESS*/
    /****FILL THIS WITH YOUR INFORMATION*********/
//This is the public api key of our application
    private static final String API_KEY = Credentials.PUBLIC_API_KEY;
    //This is the private api key of our application
    private static final String SECRET_KEY = Credentials.PRIVATE_API_KEY;
    //This is any string we want to use. This will be used for avoid CSRF attacks. You can generate one here: http://strongpasswordgenerator.com/
    private static final String STATE = Credentials.STATE;
    //This is the url that LinkedIn Auth process will redirect to. We can put whatever we want that starts with http:// or https:// .
//We use a made up url that we will intercept when redirecting. Avoid Uppercases.
    private static final String REDIRECT_URI = Credentials.REDIRECT_URI;
    /*********************************************/
//These are constants used for build the urls
    private static final String AUTHORIZATION_URL = "https://www.linkedin.com/uas/oauth2/authorization";
    private static final String ACCESS_TOKEN_URL = "https://www.linkedin.com/uas/oauth2/accessToken";
    private static final String SECRET_KEY_PARAM = "client_secret";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String GRANT_TYPE_PARAM = "grant_type";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String RESPONSE_TYPE_VALUE ="code";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String STATE_PARAM = "state";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    /*---------------------------------------*/
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUALS = "=";
    private WebView webView;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startJobSearchActivity = new Intent(MainActivity.this, JobsActivity.class);
        MainActivity.this.startActivity(startJobSearchActivity);

        setContentView(R.layout.activity_main);
//get the webView from the layout
        webView = (WebView) findViewById(R.id.main_activity_web_view);
//Request focus for the webview
        webView.requestFocus(View.FOCUS_DOWN);
//Show a progress dialog to the user
        pd = ProgressDialog.show(this, "", this.getString(R.string.loading),true);
//Set a custom web view client

        if(!isNetworkAvailable()) {
            Toast.makeText(this, "No Connection Available", Toast.LENGTH_LONG).show();
        }
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
//This method will be executed each time a page finished loading.
//The only we do is dismiss the progressDialog, in case we are showing any.
                if(pd!=null && pd.isShowing()){
                    pd.dismiss();
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {
//This method will be called when the Auth proccess redirect to our RedirectUri.
//We will check the url looking for our RedirectUri.
                if(authorizationUrl.startsWith(REDIRECT_URI)){
                    Log.i("Authorize", "");
                    Uri uri = Uri.parse(authorizationUrl);
//We take from the url the authorizationToken and the state token. We have to check that the state token returned by the Service is the same we sent.
//If not, that means the request may be a result of CSRF and must be rejected.
                    String stateToken = uri.getQueryParameter(STATE_PARAM);
                    if(stateToken==null || !stateToken.equals(STATE)){
                        Log.e("Authorize", "State token doesn't match");
                        return true;
                    }
//If the user doesn't allow authorization to our application, the authorizationToken Will be null.
                    String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
                    if(authorizationToken==null){
                        Log.i("Authorize", "The user doesn't allow authorization.");
                        return true;
                    }
                    Log.i("Authorize", "Auth token received: "+authorizationToken);
//Generate URL for requesting Access Token
                    String accessTokenUrl = getAccessTokenUrl(authorizationToken);
                    Log.e("Auth", accessTokenUrl);
//We make the request in a AsyncTask
                    new PostRequestAsyncTask().execute(accessTokenUrl);
                }else{
//Default behaviour
                    Log.i("Authorize","Redirecting to: "+authorizationUrl);
                    webView.loadUrl(authorizationUrl);
                }
                return true;
            }
        });
//Get the authorization Url
        String authUrl = getAuthorizationUrl();
        Log.i("Authorize","Loading Auth Url: "+authUrl);
//Load the authorization URL into the webView
        webView.loadUrl(authUrl);
    }
    /**
     * Method that generates the url for get the access token from the Service
     * @return Url
     */
    private static String getAccessTokenUrl(String authorizationToken){
        return ACCESS_TOKEN_URL
                +QUESTION_MARK
                +GRANT_TYPE_PARAM+EQUALS+GRANT_TYPE
                +AMPERSAND
                +RESPONSE_TYPE_VALUE+EQUALS+authorizationToken
                +AMPERSAND
                +CLIENT_ID_PARAM+EQUALS+API_KEY
                +AMPERSAND
                +REDIRECT_URI_PARAM+EQUALS+REDIRECT_URI
                +AMPERSAND
                +SECRET_KEY_PARAM+EQUALS+SECRET_KEY;
    }
    /**
     * Method that generates the url for get the authorization token from the Service
     * @return Url
     */
    private static String getAuthorizationUrl(){
        return AUTHORIZATION_URL
                +QUESTION_MARK+RESPONSE_TYPE_PARAM+EQUALS+RESPONSE_TYPE_VALUE
                +AMPERSAND+CLIENT_ID_PARAM+EQUALS+API_KEY
                +AMPERSAND+STATE_PARAM+EQUALS+STATE
                +AMPERSAND+REDIRECT_URI_PARAM+EQUALS+REDIRECT_URI;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    private class PostRequestAsyncTask extends AsyncTask<String, Void, Boolean>{
        @Override
        protected void onPreExecute(){
            pd = ProgressDialog.show(MainActivity.this, "", MainActivity.this.getString(R.string.loading),true);
        }
        @Override
        protected Boolean doInBackground(String... urls) {
            if(urls.length>0){
                String url = urls[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                try{
                    HttpResponse response = httpClient.execute(httpPost);
                    if(response!=null){
//If status is OK 200
                        if(response.getStatusLine().getStatusCode()==200){
                            String result = EntityUtils.toString(response.getEntity());
//Convert the string result to a JSON Object
                            JSONObject resultJson = new JSONObject(result);
//Extract data from JSON Response
                            int expiresIn = resultJson.has("expires_in") ? resultJson.getInt("expires_in") : 0;
                            String accessToken = resultJson.has("access_token") ? resultJson.getString("access_token") : null;
                            if(expiresIn>0 && accessToken!=null){
                                Log.i("Authorize", "This is the access Token: "+accessToken+". It will expires in "+expiresIn+" secs");
//Calculate date of expiration
                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.SECOND, expiresIn);
                                long expireDate = calendar.getTimeInMillis();
////Store both expires in and access token in shared preferences
                                SharedPreferences preferences = MainActivity.this.getSharedPreferences("user_info", 0);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putLong("expires", expireDate);
                                editor.putString("accessToken", accessToken);
                                Log.e("Access Token HERE! ", accessToken);
                                editor.apply();
                                return true;
                            }
                        }
                    }
                }catch(IOException e){
                    Log.e("Authorize","Error Http response "+e.getLocalizedMessage());
                }
                catch (ParseException e) {
                    Log.e("Authorize","Error Parsing Http response "+e.getLocalizedMessage());
                } catch (JSONException e) {
                    Log.e("Authorize","Error Parsing Http response "+e.getLocalizedMessage());
                }
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean status){
            if(pd!=null && pd.isShowing()){
                pd.dismiss();
            }
            if(status){
                //If everything went Ok, change to another activity.
                Intent startJobSearchActivity = new Intent(MainActivity.this, JobsActivity.class);
                MainActivity.this.startActivity(startJobSearchActivity);
            }
        }
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}