package com.example.kirgo.psmgajda;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends ListActivity {

    public String Psm_log = "Psm_log";

    private String last_checked = "last_checked";
    private String api_coinmarketcap = "https://api.coinmarketcap.com/v1/ticker/?limit=10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Psm_log, "onCreate");

        setContentView(R.layout.activity_main);

        //onListItemClick();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(Psm_log, "onResume");

        new ReadURLTask().execute(api_coinmarketcap);
    }

    private class ReadURLTask extends AsyncTask<String, Void, String> {

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> currenciesList;
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(Psm_log, "onPreExecute");
            pd = ProgressDialog.show(MainActivity.this, " api_coinmarketcap ", "Pobieram dane...");
        }

        @Override
        protected String doInBackground(String... urls) {
            Log.e(Psm_log, "doInBackground + urls: " + urls.toString());

            String response = "";

            try {
                URL url = new URL(urls[0]);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String linia = "";
                while((linia = br.readLine()) !=  null ) {
                    response += linia;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(response);

            if(!response.equals(""))
                zapisz_do_pliku(response);
            else if (wczytaj_z_pliku()!= null) {
                response = wczytaj_z_pliku();
                //Toast.makeText(MainActivity.this,"Brak internetu, wyswietlam ostatnie zapisane dane", Toast.LENGTH_SHORT).show();
            } else
                pd.dismiss();

            currenciesList = ParseJSON_coinmarketcap(response);

            return response;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e(Psm_log, "onPostExecute + s: " + s);

            System.out.println(s);

            pd.dismiss();

            /**
             * Updating received data from JSON into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, currenciesList,
                    R.layout.list_item,
                    new String[]{Keys_api_coinmarketcap.id, Keys_api_coinmarketcap.name, Keys_api_coinmarketcap.price_usd, Keys_api_coinmarketcap.percent_change_24h},
                    new int[]{R.id.id, R.id.name, R.id.price_usd, R.id.percent_change_24h});

            setListAdapter(adapter);
        }
    }

    private ArrayList<HashMap<String, String>> ParseJSON_coinmarketcap(String raw_json) {
        Log.e(Psm_log, "ParseJSON_coinmarketcap + raw_json: " + raw_json);
        if (raw_json != null) {
            try {
                // Hashmap for ListView
                ArrayList<HashMap<String, String>> crypto_currencies_list = new ArrayList<HashMap<String, String>>();

                // Getting JSON Array node
                JSONArray currencies = new JSONArray(raw_json);

                // looping through All Students
                for (int i = 0; i < currencies.length(); i++) {
                    JSONObject currency = currencies.getJSONObject(i);

                    String id = currency.getString(Keys_api_coinmarketcap.id);
                    String name = currency.getString(Keys_api_coinmarketcap.name);
                    String price_usd = currency.getString(Keys_api_coinmarketcap.price_usd);
                    String percent_change_24h = currency.getString(Keys_api_coinmarketcap.percent_change_24h);

                    // tmp hashmap for single currency
                    HashMap<String, String> currencyMap = new HashMap<String, String>();

                    // adding every child node to HashMap key => value
                    currencyMap.put(Keys_api_coinmarketcap.id, id);
                    currencyMap.put(Keys_api_coinmarketcap.name, name);
                    currencyMap.put(Keys_api_coinmarketcap.price_usd, "price_usd: " + price_usd);
                    currencyMap.put(Keys_api_coinmarketcap.percent_change_24h, "percent_change_24h: " + percent_change_24h + "%");

                    // adding currencyMap to currencies list
                    crypto_currencies_list.add(currencyMap);
                }
                return crypto_currencies_list;

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e(Psm_log, "No data received from HTTP request");
            System.out.println("\"ServiceHandler\", \"No data received from HTTP request\"");
            return null;
        }
    }

    public void zapisz_do_pliku (String raw_jason) {
        Log.e(Psm_log, "zapisz_do_pliku + raw_json: " + raw_jason);
        try {
            FileOutputStream fileOutputStream = openFileOutput(last_checked, Context.MODE_PRIVATE);
            fileOutputStream.write(raw_jason.getBytes());
            fileOutputStream.close();
            System.out.println("zapisano do pliku");
            Log.e(Psm_log,"zapisano do pliku");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String wczytaj_z_pliku () {
        Log.e(Psm_log, "wczytaj_z_pliku");
        StringBuffer stringBuffer = new StringBuffer();
        try {
            FileInputStream fileInputStream = openFileInput(last_checked);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String message;
            while ( ( message = bufferedReader.readLine()) != null ) {
                stringBuffer.append(message + "\n");
            }

//            bufferedReader.close();
//            inputStreamReader.close();
//            fileInputStream.close();

            System.out.println("Wczytano z pliku");
            Log.e(Psm_log,"Wczytano z pliku");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

}
