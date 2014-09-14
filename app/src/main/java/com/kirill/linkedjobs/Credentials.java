package com.kirill.linkedjobs;

/**
 * Created by kirill on 9/13/14.
 */
public class Credentials {

    static String scheme = "https";
    static String domainName = "linkedin";
    static String theDomain = "com";
    static String baseURL = scheme + "://" + domainName + "." + theDomain + "/";
    static String loginURL = baseURL + "uas/login";

}
