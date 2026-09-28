package com.tabnova.aidashboard.Extra;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;


public class GetFaviconURL extends AsyncTask<String, Void, String> {
    String item;
    Context context;

    public GetFaviconURL(Context context, String item) {
        this.context = context;
        this.item = item;
    }

    @Override
    protected String doInBackground(String... Item) {
        String url = getWebPage(item);
        if (url != null) {
            item = url;
        }
        return item;
    }

    @Override
    protected void onPostExecute(String item) {
        super.onPostExecute(item);
        NetworkCallback callback = (NetworkCallback) context;
        if (item != null) {
            callback.urlFetchSuccessful(item);
        } else {
            callback.urlFetchError(item);
        }
    }

    public String getWebPage(String urlStr) {
        String adresse = urlStr;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet();

        InputStream inputStream = null;

        String response = null;
        String faviconURL = null;

        try {

            URI uri = new URI(adresse);
            httpGet.setURI(uri);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            int statutCode = httpResponse.getStatusLine().getStatusCode();
            int length = (int) httpResponse.getEntity().getContentLength();


            inputStream = httpResponse.getEntity().getContent();
            Reader reader = new InputStreamReader(inputStream, "UTF-8");

            int inChar;
            StringBuffer stringBuffer = new StringBuffer();

            while ((inChar = reader.read()) != -1) {
                stringBuffer.append((char) inChar);
            }

            if (statutCode == HttpStatus.SC_OK) {
                response = stringBuffer.toString();
                int i = -1;
                i = response.indexOf("rel=\"shortcut icon\"");
                if (i == -1) {
                    i = response.indexOf("rel='shortcut icon'");
                }
                int j = i;
                int startindex = 0;
                int stopIndex = 0;
                if (i != -1) {
                    while (response.charAt(i) != '<' || response.charAt(i) != '>') {
                        if (response.charAt(i) == '<') {
                            startindex = i;
                            break;
                        } else if (response.charAt(i) == '>') {
                            stopIndex = i;
                            break;
                        }
                        i++;
                    }


                    while (response.charAt(j) == '<' || response.charAt(j) != '>') {
                        if (response.charAt(j) == '<') {
                            startindex = j;
                            break;
                        } else if (response.charAt(j) == '>') {
                            stopIndex = j;
                            break;
                        }
                        j--;

                    }

                    String finalUrl;
                    if (startindex > stopIndex) {
                        finalUrl = response.substring(stopIndex, startindex);
                    } else {
                        finalUrl = response.substring(startindex, stopIndex);
                    }
                    String[] url = finalUrl.split("href=");
                    String x = url[1].replace("rel=\"shortcut icon\"", "");
                    faviconURL = x.replace("\"", "");
                    if (faviconURL.endsWith("/"))
                        faviconURL = faviconURL.substring(0, faviconURL.length() - 1);
                    Log.d("finalUrl--", "---" + faviconURL);
                }
            }


        } catch (Exception e) {
            //Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return faviconURL;

    }
}
