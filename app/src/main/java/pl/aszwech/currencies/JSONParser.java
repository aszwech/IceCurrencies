package pl.aszwech.currencies;

import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class JSONParser {
    static InputStream sInputStream = null;
    static JSONObject sReturnJsonObject = null;
    static String sRawJsonString = "";

    public JSONParser() {
    }

    public JSONObject getJSONFromUrl(String urlArg) {

        try {
            URL url = new URL(urlArg);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            sInputStream = urlConnection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        } catch (ClientProtocolException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    sInputStream, "iso-8859-1"), 8);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            sInputStream.close();
            sRawJsonString = stringBuilder.toString();
        } catch (Exception e) {
            Log.e("Error reading: " + e.toString(), this.getClass().getSimpleName());
        }
        try {
            sReturnJsonObject = new JSONObject(sRawJsonString);
        } catch (JSONException e) {
            Log.e("Parser", "Error when parsing data " + e.toString());
        }
        return sReturnJsonObject;
    }
}