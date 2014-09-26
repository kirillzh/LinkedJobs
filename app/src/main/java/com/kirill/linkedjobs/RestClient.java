package com.kirill.linkedjobs;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.GET;

/**
 * Created by kirill on 9/20/14.
 * My GitHub: https://gthub.com/ZhukovKirill
 */

//    private static final String JOB_SEARCH_URL = BASE_URL + "job-search";
//    private static final String JOBS_URL = BASE_URL + "jobs";
//    private static final String PEOPLE_URL = BASE_URL + "people/~";
//
//    private static final String OAUTH_ACCESS_TOKEN_PARAM = "oauth2_access_token";
//    private static final String QUESTION_MARK = "?";
//    private static final String EQUALS = "=";

public class RestClient {
    private static final String BASE_URL = "http://api.linkedin.com/v1";

    static class Job {
        String location, companyName;
        int companyId;

    }
    interface GetJobs {
        @GET("/job-search?oauth2_access_token=" + Credentials.ACCESS_TOKEN + "&format=json")
        void getJobs();

        List<Job> jobs();
    }
    public static void main(String... args) {
// Create a very simple REST adapter which points the GitHub API endpoint.
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(BASE_URL)
                .build();
// Create an instance of our GitHub API interface.
        GetJobs lnkedin = restAdapter.create(GetJobs.class);
// Fetch and print a list of the contributors to this library.
        List<Job> jobs = lnkedin.jobs();
        for (Job job : jobs) {
            System.out.println(job.companyName + " (" + job.location + ")");
        }
    }
}

