package com.example.newsreader;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    // List of News objects representing the forecast
    private List<News> newsList = new ArrayList<>();

    // ArrayAdapter for binding News objects to a ListView
    private NewsArrayAdapter newsArrayAdapter;
    private ListView newsListView;

    // store news source url/keys/id
    private String api_key;
    private String base_url;

    MediaPlayer musicPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d(TAG, "onCreate()");

        // create ArrayAdapter to bind newsList to the newsListView
        newsListView = (ListView) findViewById(R.id.newsListView);
        newsArrayAdapter = new NewsArrayAdapter(this, newsList);
        newsListView.setAdapter(newsArrayAdapter);

        // default to CNN
        api_key = getString(R.string.cnn_api_key);
        base_url = getString(R.string.cnn_web_service_url);

        // start background music
        startMusicPlayer();

      // configure FAB to hide keyboard and initiate web service request
      FloatingActionButton fab =
         (FloatingActionButton) findViewById(R.id.fab);
      fab.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            // get text from newsEditText and create web service URL
            EditText newsEditText =
               (EditText) findViewById(R.id.newsEditText);
            URL url = createURL(newsEditText.getText().toString());
            Log.d(TAG, "onClick()");

            // hide keyboard and initiate a GetNewsTask to download
            // news data from newsapi.org in a separate thread
            if (url != null) {
               dismissKeyboard(newsEditText);
               GetNewsTask getNewsTask = new GetNewsTask();
               getNewsTask.execute(url);
            }
            else {
               Snackbar.make(findViewById(R.id.coordinatorLayout),
                  R.string.invalid_url, Snackbar.LENGTH_LONG).show();
            }
         }
      });
   }

    // programmatically dismiss keyboard when user touches FAB
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // create newsapi.org web service URL using city
    private URL createURL(String topic) {

        try {
            // create URL for specified city and imperial units (Fahrenheit)
            String urlString = base_url + URLEncoder.encode(topic, "UTF-8") +
                    "&apiKey=" + api_key;
            Log.d(TAG, "createURL(): " + urlString);
            return new URL(urlString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null; // URL was malformed
    }

    // makes the REST web service call to get news data and
    // saves the data to a local HTML file
    private class GetNewsTask
            extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                    catch (IOException e) {
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    return new JSONObject(builder.toString());
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            }
            catch (Exception e) {
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); // close the HttpURLConnection
            }

            return null;
        }

        // process JSON response and update ListView
        @Override
        protected void onPostExecute(JSONObject news) {
            convertJSONtoArrayList(news); // repopulate newsList
            newsArrayAdapter.notifyDataSetChanged(); // rebind to ListView
            newsListView.smoothScrollToPosition(0); // scroll to top
        }
    }

    // create News objects from JSONObject containing the forecast
    private void convertJSONtoArrayList(JSONObject forecast) {
        newsList.clear(); // clear old news data
        Log.d(TAG, "convertJSONtoArrayList()");
        try {
            // get forecast's "articles" JSONArray
            JSONArray list = forecast.getJSONArray("articles");

            // convert each element of list to a News object
            for (int i = 0; i < list.length(); ++i) {
                JSONObject article = list.getJSONObject(i); // get one day's data

                // add new News object to newsList
                newsList.add(new News(
                        article.getString("title"), // article title
                        article.getString("url"))); // article url
                Log.d(TAG, "convertJSONtoArrayList()" + article.getString("title"));
                Log.d(TAG, "convertJSONtoArrayList()" + article.getString("url"));

            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.cnn_news) {
            api_key = getString(R.string.cnn_api_key);
            base_url = getString(R.string.cnn_web_service_url);
            return true;
        }
        if (id == R.id.nyt_news) {
            api_key = getString(R.string.nyt_api_key);
            base_url = getString(R.string.nyt_web_service_url);
            return true;
        }
        if (id == R.id.music_on_off) {
            musicPause();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startMusicPlayer() {
        if(musicPlayer == null) {
            musicPlayer = MediaPlayer.create(this, R.raw.dharma );
            musicPlayer.setLooping(true);
            musicPlayer.setVolume(100,100 );
            musicPlayer.start();
        }
    }

    private void musicPause() {
        if(musicPlayer != null) {
            if(musicPlayer.isPlaying()) {
                musicPlayer.pause();
            }
            else {
                musicPlayer.start();
            }
        }
    }
}
