package com.kirill.linkedjobs;

import com.loopj.android.http.*;

/**
 * Created by kirill on 9/20/14.
 * My GitHub: https://gthub.com/ZhukovKirill
 */
public class RestClient {

    private static final String BASE_URL = "http://api.linkedin.com/v1/";
    private static final String JOB_SEARCH_URL = BASE_URL + "job-search";
    private static final String JOBS_URL = BASE_URL + "jobs";
    private static final String PEOPLE_URL = BASE_URL + "people/~";

    private static final String OAUTH_ACCESS_TOKEN_PARAM = "oauth2_access_token";
    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";




    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }


}
