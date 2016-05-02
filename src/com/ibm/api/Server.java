/*
###############################################################################
#
# The MIT License (MIT)
#
# Copyright (c) 2016 IBM Corp.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#
###############################################################################
*/

package com.ibm.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.sling.commons.json.JSONObject;

public class Server extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String WATSON_ANALYTICS_API_URL = "api.ibm.com";
	private static final String WATSON_ANALYTICS_API_BASE_PATH = "/watsonanalytics/run";
	private static final String APPLICATION_URL = "localhost";
	private static final String APPLICATION_PORT = "8080";
	private static final String REDIRECT_URL = "http://" + APPLICATION_URL + ":" + APPLICATION_PORT + "/demo/oauth2/code";
	private static String ACCESS_TOKENS = null;
	private static final Properties yourAppKey = new Properties();

    public Server() {
        super();
    }

    public void init(ServletConfig config) throws ServletException {
    	try {
        	InputStream keyStream = config.getServletContext().getResourceAsStream("/WEB-INF/appkey.properties");
        	yourAppKey.load(keyStream);
    	} catch (Exception e) {
    		System.err.println(e.getLocalizedMessage());
    	}
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String rest_endpoint = request.getRequestURI();
		if (rest_endpoint.compareTo("/demo/oauth2/auth") == 0) {
			demoOAuth2OAuth(response);
		} else
		if (rest_endpoint.compareTo("/demo/oauth2/code") == 0) {
			demoOAuth2Code(request, response);
		} else
		if (rest_endpoint.compareTo("/demo/me") == 0) {
			demoMe(response);
		} else
		if (rest_endpoint.compareTo("/demo/upload") == 0) {
			demoUpload(response);
		}
	}

	// Build the request to get an OAuth2 authorization code.
	// The server builds the request here, but the browser must make the request.
	private void demoOAuth2OAuth(HttpServletResponse response) {
		try {
			String locationURI = "https://" + WATSON_ANALYTICS_API_URL + WATSON_ANALYTICS_API_BASE_PATH
					+ "/clientauth/v1/auth?response_type=code&client_id=" + yourAppKey.getProperty("client_id")
					+ "&scope=userContext&state=xyz&redirect_uri=" + URLEncoder.encode(REDIRECT_URL, "UTF-8");
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			response.setHeader("Location", locationURI);
		} catch (Exception e) {
        	System.err.println(e.getLocalizedMessage());
        }

	}

	// Watson Analytics returns the authorization code by adding it to the redirect URL.
	// The browser received HTTP 302 + location. The location is the redirect URL.
	// Trade the authorization code for an OAuth2 access token.
	private void demoOAuth2Code(HttpServletRequest request, HttpServletResponse response) {
		String code = request.getParameter("code");
		String apiURL = "https://" + WATSON_ANALYTICS_API_URL + WATSON_ANALYTICS_API_BASE_PATH + "/oauth2/v1/token";
		HttpPost apiRequest = new HttpPost(apiURL);
		apiRequest.addHeader("X-IBM-Client-Id", yourAppKey.getProperty("client_id"));
		apiRequest.addHeader("X-IBM-Client-Secret", yourAppKey.getProperty("client_secret"));
		try {
			String form = "grant_type=authorization_code&code=" + URLEncoder.encode(code, "UTF-8");
			apiRequest.setEntity(new StringEntity(form, ContentType.create("application/x-www-form-urlencoded")));
			HttpClient httpClient = new DefaultHttpClient();
        	HttpResponse apiResponse = httpClient.execute( apiRequest );
        	ByteArrayOutputStream stream = new ByteArrayOutputStream();
            apiResponse.getEntity().writeTo( stream );
            JSONObject obj = new JSONObject( stream.toString() );
            stream.close();
            ACCESS_TOKENS = (String)obj.get( "access_token" );

        } catch (Exception e) {
        	System.err.println(e.getLocalizedMessage());
        }

		String locationURI = "http://" + APPLICATION_URL + ":" + APPLICATION_PORT + "/demo/integration.html";
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", locationURI);
	}

	// Use the ME API.
	private void demoMe(HttpServletResponse response) {
		String apiURL = "https://" + WATSON_ANALYTICS_API_URL + WATSON_ANALYTICS_API_BASE_PATH + "/accounts/v1/me";
		HttpGet apiRequest = new HttpGet(apiURL);
		apiRequest.addHeader("X-IBM-Client-Id", yourAppKey.getProperty("client_id"));
		apiRequest.addHeader("X-IBM-Client-Secret", yourAppKey.getProperty("client_secret"));
		apiRequest.addHeader("Authorization", "Bearer " + ACCESS_TOKENS);
		try {
			HttpClient httpClient = new DefaultHttpClient();
        	HttpResponse apiResponse = httpClient.execute( apiRequest );
        	ByteArrayOutputStream stream = new ByteArrayOutputStream();
            apiResponse.getEntity().writeTo( stream );
            response.getWriter().write(stream.toString());
            stream.close();
        } catch (Exception e) {
        	System.err.println(e.getLocalizedMessage());
        }
	}

	// Create and push a simple data set to Watson Analytics.
	// Create a new empty data set that has a specified name.
	private void demoUpload(HttpServletResponse response) {
		String id = null;
		try {
			String apiURL = "https://" + WATSON_ANALYTICS_API_URL + WATSON_ANALYTICS_API_BASE_PATH + "/data/v1/datasets";
			HttpPost apiRequest = new HttpPost(apiURL);
			apiRequest.addHeader("X-IBM-Client-Id", yourAppKey.getProperty("client_id"));
			apiRequest.addHeader("X-IBM-Client-Secret", yourAppKey.getProperty("client_secret"));
			apiRequest.addHeader("Authorization", "Bearer " + ACCESS_TOKENS);
			apiRequest.addHeader("Content-Type", "application/json");
			JSONObject json = new JSONObject();
	        json.put( "name", "YourApplication_" + new Date().getTime() );
	        apiRequest.setEntity( new StringEntity( json.toString() ) );
	        HttpClient httpClient = new DefaultHttpClient();
        	HttpResponse apiResponse = httpClient.execute( apiRequest );
        	ByteArrayOutputStream stream = new ByteArrayOutputStream();
            apiResponse.getEntity().writeTo( stream );
            JSONObject obj = new JSONObject( stream.toString() );
            stream.close();
            id = (String)obj.get( "id" );
		} catch (Exception e) {
	        	System.err.println(e.getLocalizedMessage());
	    }
		try {
            String apiURL = "https://" + WATSON_ANALYTICS_API_URL + WATSON_ANALYTICS_API_BASE_PATH + "/data/v1/datasets/" + id + "/content";
			HttpPut apiRequest = new HttpPut(apiURL);
			apiRequest.addHeader("X-IBM-Client-Id", yourAppKey.getProperty("client_id"));
			apiRequest.addHeader("X-IBM-Client-Secret", yourAppKey.getProperty("client_secret"));
			apiRequest.addHeader("Authorization", "Bearer " + ACCESS_TOKENS);
			apiRequest.addHeader("Content-Type", "text/csv");
			String body = "c1, c2\n";
		    body += "r1, r1\n";
		    body += "r2, r2\n";
	        apiRequest.setEntity( new StringEntity( body ) );
	        HttpClient httpClient = new DefaultHttpClient();
        	httpClient.execute( apiRequest );
        } catch (Exception e) {
        	System.err.println(e.getLocalizedMessage());
        }

		String locationURI = "https://watson.analytics.ibmcloud.com";
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", locationURI);
	}
}

