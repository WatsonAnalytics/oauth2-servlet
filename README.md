# Java Servlet example for IBM Watson Analytics

## Setup
1. Download and install Java 8 from http://java.com/en/download/manual.jsp
2. In a console, type `java -version` to verify that Java installed correctly and is available to other applications.
3. Download Apache Tomcat 8.0 in .zip format from http://tomcat.apache.org.
4. Install Apache Tomcat in one of these ways:
  - Decompress the zip file into a folder.
  - Follow the setup documentation available from http://tomcat.apache.org/tomcat-8.0-doc/setup.html.
5. Navigate to the `./<tomcat_folder>/webapps` folder and create a folder called `demo`.
6. Copy the content of the `./WebContent/` folder content to the `./<tomcat_folder>/webapps/demo/` folder (including the index.html and integration.html files, and the WEB-INF and META_INF folders).
7. Move to the `./<tomcat_folder>/webapps/demo/WEB-INF/` folder and create a folder called `lib`.
8. Download the Apache HTTP client (zip format) from https://hc.apache.org/downloads.cgi. We built this sample using Apache HTTP Client version 4.2.5.
9. Decompress the Apache HTTP client content in the `./<tomcat_folder>/webapps/demo/WEB-INF/lib/` folder.
10. Download Apache Sling Common JSON (zip format) from http://sling.apache.org/downloads.cgi. We built this sample using Apache Sling Common JSON version 2.0.16.
11. Decompress the Apache Sling Common JSON content in the `./<tomcat_folder>/webapps/demo/WEB-INF/lib/` folder.
12. Register your client id and client secret. Use "http://localhost:8080/demo/oauth2/code" as your redirect URI. You only need to call this operation once unless the client ID changes. Use the following CURL command. Replace the details with your information.
```
curl -v -X PUT -H "X-IBM-Client-Secret:YOUR_CLIENT_SECRET" -H "X-IBM-Client-Id:YOUR_CLIENT_ID" -H "Content-Type: application/json" -d '{"clientName": "The Sample Outdoors Company", "redirectURIs": 'https://example.com:5443", "ownerName": "John Smith", "ownerEmail": "John.Smith@example.com", "ownerCompany": "example.com", "ownerPhone": "555-123-4567"}' https://api.ibm.com/watsonanalytics/run/oauth2/v1/config
```
## Create a properties file
1. Create a file called `appkey.properties` in the `./<tomcat_folder>/webapps/demo/WEB-INF/` folder that matches the following template. Enter your client id and secret. Save it in the application folder. That's where the index.html file is located.
```
  		client_id=YOUR_CLIENT_ID
  		client_secret=YOUR_CLIENT_SECRET
```
## Start the application
1. In a console, navigate to the `./<tomcat_folder>/bin` folder, then type `./startup.sh` (on OSX) or `startup.bat` (on Windows).
   - Tip: For OSX, you will probably need to run `chmod u+x *.sh` in the `bin` folder first.
2. To run the application, open a web browser and navigate to this address: http://localhost:8080/demo.

## Notes
* For clarity, this sample application uses `localhost` as part of the URL for the application. However, when you create your own application, do not use localhost.
* This sample application is simplified to emphasize the code that you need to work with Watson Analytics. For example, it contains minimal error checking.

