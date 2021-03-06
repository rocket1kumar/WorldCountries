package com.vyke.worldcountries;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.vyke.worldcountries.R;

import com.vyke.worldcountries.about.About;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final double APP_VER = 1.0;
    private final String exeUrlAll = "https://restcountries.eu/rest/v2/all";
    private ListView listView;
    private ProgressBar progressBar;
    private List<Countries> CountriesList = new ArrayList<>();
    private CustomAdapter adapter;

    public boolean isConnected() {
        ConnectivityManager getNetworkStatus = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = getNetworkStatus.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                Toast.makeText(MainActivity.this, "Mobile Data Will be used", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else

            return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isConnected();

        listView = (ListView) findViewById(R.id.main_list);
        progressBar = (ProgressBar) findViewById(R.id.list_progress);

        // new GetInfo().execute("https://restcountries.eu/rest/v2/name/india?fullText=true");

        try {
            if (isConnected()) {
                new GetInfo().execute(exeUrlAll);
                //   AdRequest adRequest = new AdRequest.Builder().addTestDevice("860C5A531681CA9A3CA94D8936AC515F").build();
                AdView mAdView = (AdView) findViewById(R.id.adView);
                mAdView.setVisibility(View.INVISIBLE);
              //  AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
               AdRequest adRequest = new AdRequest.Builder().build();
               mAdView.loadAd(adRequest);
               mAdView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(MainActivity.this, " Oops! Your are off the Internet", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sname;
                try {
                    sname = adapter.getItem(position).getName();

                    if (sname != null) {
                        Intent intent = new Intent(MainActivity.this, WikiWeb.class);
                        intent.putExtra("Cname", sname);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Cannot Get the Name", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_items, menu);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.searchbtn));
        searchView.setSearchableInfo(searchableInfo);
        searchView.setMaxWidth( Integer.MAX_VALUE );
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {

                    //  adapter.setFilter(newText);
                    CustomAdapter newAdapter = adapter.setFilter(newText);
                    listView.setAdapter(newAdapter);
             /*       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            try {
                                String sname = newAdapter.getItem(position).getName();
                                if (sname != null) {
                                    Intent intent = new Intent(MainActivity.this, WikiWeb.class);
                                    intent.putExtra("Cname", sname);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "Cannot Get the Name", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
*/
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh:
                if (isConnected()) {
                    new GetInfo().execute(exeUrlAll);


                } else {
                    Toast.makeText(MainActivity.this, "Could not Refresh", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return true;
           case R.id.about:
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                return true;
        }
        return false;
        // return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();


    }

    @Override
    protected void onResume() {
        super.onResume();
        listView.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class GetInfo extends AsyncTask<String, String, List<Countries>> {


        @Override
        public List<Countries> doInBackground(String... params) {

            StringBuilder builder = new StringBuilder();
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {

                    InputStream inputStream = urlConnection.getInputStream();
                    //  InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String Line;

                    while ((Line = bufferedReader.readLine()) != null) {
                        builder.append(Line);
                    }
                    Log.e("JSON", "  DATA is Received");
                } else {
                    return null;
                }

                CountriesList = jsonParsing(builder.toString());
                return CountriesList;
            } catch (MalformedURLException m) {
                m.getCause().printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        private List<Countries> jsonParsing(String jsonData) {
            try {
                JSONArray jsonArray = new JSONArray(jsonData);
                for (int j = 0; j < jsonArray.length(); j++) {
                    Countries cts = new Countries();
                    cts.setName(jsonArray.getJSONObject(j).getString("name"));
                    cts.setCapital(jsonArray.getJSONObject(j).getString("capital"));
                    cts.setAlpha2Code(jsonArray.getJSONObject(j).getString("alpha2Code"));
                    cts.setAlpha3Code(jsonArray.getJSONObject(j).getString("alpha3Code"));
                    cts.setRegion(jsonArray.getJSONObject(j).getString("region"));
                    cts.setSubregion(jsonArray.getJSONObject(j).getString("subregion"));
                    cts.setArea(jsonArray.getJSONObject(j).getString("area"));
                    cts.setDemonym(jsonArray.getJSONObject(j).getString("demonym"));
                    cts.setNativeName(jsonArray.getJSONObject(j).getString("nativeName"));
                    cts.setPopulation(jsonArray.getJSONObject(j).getString("population"));
                    cts.setFlag(jsonArray.getJSONObject(j).getString("flag"));
                    //  cts.setFlag(jsonArray.getJSONObject(j).getString("name"));
                    // Get time Zone
                    List<String> timeZone = new ArrayList<>();
                    for (int i = 0; i < jsonArray.getJSONObject(j).getJSONArray("timezones").length(); i++) {
                        String S = jsonArray.getJSONObject(j).getJSONArray("timezones").getString(i);
                        timeZone.add(S);
                    }
                    cts.setTimezones(timeZone);
                    // for border
                    List<String> Borders = new ArrayList<>();
                    for (int i = 0; i < jsonArray.getJSONObject(j).getJSONArray("borders").length(); i++) {
                        String S = jsonArray.getJSONObject(j).getJSONArray("borders").getString(i);
                        Borders.add(S);
                    }
                    cts.setBorders(Borders);
                    // Get Currency
                    List<Countries.Currency> CurrencyList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.getJSONObject(j).getJSONArray("currencies").length(); i++) {
                        Countries.Currency Curli = new Countries.Currency();
                        JSONObject CurJSONObject = jsonArray.getJSONObject(j).getJSONArray("currencies").getJSONObject(i);
                        Curli.setCurrencyCode(CurJSONObject.getString("code"));

                        Curli.setSymbol(CurJSONObject.getString("symbol"));

                        Curli.setCurrencyName(CurJSONObject.getString("name"));
                        CurrencyList.add(Curli);
                    }
                    cts.setCurrencies(CurrencyList);

                    // Get Languages
                    List<Countries.Languages> LanguageList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.getJSONObject(j).getJSONArray("languages").length(); i++) {
                        Countries.Languages Lingo = new Countries.Languages();
                        JSONObject CurJSONObject = jsonArray.getJSONObject(j).getJSONArray("languages").getJSONObject(i);
                        Lingo.setIso639_1(CurJSONObject.getString("iso639_1"));

                        Lingo.setName(CurJSONObject.getString("name"));


                        LanguageList.add(Lingo);
                    }
                    cts.setLanguages(LanguageList);

                    // Get Calling Code
                    List<String> CallingCode = new ArrayList<>();
                    for (int i = 0; i < jsonArray.getJSONObject(j).getJSONArray("callingCodes").length(); i++) {
                        String S = jsonArray.getJSONObject(j).getJSONArray("callingCodes").getString(i);
                        CallingCode.add(S);

                    }
                    cts.setCallingCodes(CallingCode);
                    CountriesList.add(cts);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return CountriesList;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPostExecute(List<Countries> CL) {
            super.onPostExecute(CL);
            adapter = new CustomAdapter(getApplicationContext(), R.layout.row, CL);
            try {
                listView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

