package com.hareesh.quotepad.explore;

/**
 * Created by Hareesh on 8/31/2016.
 */
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;
import com.hareesh.quotepad.MainActivity;
import com.hareesh.quotepad.R;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExploreActivity extends AppCompatActivity {

    private MaterialViewPager mViewPager;
    MaterialSearchView searchView;
    public static ArrayList<String> resultStrs;
    ArrayList<String> remove;
    ArrayList<String> remove2;
    String quoteJsonStr;
    String imageJsonStr;
    String input2;
    String imageURL;
    public static String author;
    String searchText;
    boolean quotesExist = false;
    TextView logo;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        context = this;

        resultStrs = new ArrayList<>();

        //--------------------------------------------------MaterialViewPager----------------------------------------//
        setTitle("");
        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);
        mViewPager.getViewPager().setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position % 2) {
                    case 0:
                        return ScrollFragment.newInstance();
                    case 1:
                        return ExploreRecyclerViewFragment.newInstance();
                    default:
                        return ExploreRecyclerViewFragment.newInstance();
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position % 2) {
                    case 0:
                        return "About";
                    case 1:
                        return "Quotes";
                }
                return "";
            }
        });
        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                switch (page) {
                    case 0:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.theme_primary,
                                "");
                    case 1:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.theme_primary,
                                "");
                }
                //execute others actions if needed (ex : modify your header logo)

                return null;
            }
        });
        mViewPager.getViewPager().setOffscreenPageLimit(mViewPager.getViewPager().getAdapter().getCount());
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());

        logo = (TextView)findViewById(R.id.header);
        if (logo != null) {
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.notifyHeaderChanged();
                    Toast.makeText(getApplicationContext(), "Yes, the title is clickable", Toast.LENGTH_SHORT).show();
                }
            });
        }


        //--------------------------------------------------MaterialSearchView----------------------------------------//
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(false);
        searchView.setSubmitOnClick(true);
        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = WordUtils.capitalize(query);
                searchText = query;
                FetchQuoteTask quoteTask = new FetchQuoteTask();
                quoteTask.execute(query);
                searchView.clearFocus();
                searchView.closeSearch();
                searchView.dismissSuggestions();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
    }

    public void setTitleToAuthor(boolean exist){
        if(exist){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logo.setText(author);
                }
            });
        }
        else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logo.setText("Not Found");
                }
            });
        }

    }

    public class FetchQuoteTask extends AsyncTask<String, Void, ArrayList<String>> {

        private final String LOG_TAG = FetchQuoteTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private ArrayList<String> getQuoteDataFromJson(String searchText)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String QUERY = "query";
            final String PAGES = "pages";
            final String TITLE = "title";

            JSONObject searchJSON = new JSONObject(searchText);
            JSONObject queryObj = searchJSON.getJSONObject(QUERY);
            if (queryObj.has("pageids")) {
                JSONArray indexArray = queryObj.getJSONArray("pageids");
                String index = indexArray.getString(0);
                JSONObject pagesObj = queryObj.getJSONObject(PAGES);
                if (pagesObj.has(index)) {
                    JSONObject idObj = pagesObj.getJSONObject(index);

                    resultStrs.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ExploreRecyclerViewFragment.fillCards(resultStrs);
                        }
                    });

                    remove = new ArrayList<>();
                    remove2 = new ArrayList<>();
                    String quotes;
                    String input;

                    if(idObj.has(TITLE) && idObj.has("extract")){
                        quotesExist = true;
                        author = idObj.getString(TITLE);
                        quotes = idObj.getString("extract");

                        setTitleToAuthor(true);

                        input = quotes;

                        // pattern1 and pattern2 are String objects
                        String regexString = Pattern.quote("<li>") + "(.*?)" + Pattern.quote("</li>");
                        Pattern pattern = Pattern.compile(regexString);
                        // text contains the full text that you want to extract data
                        Matcher matcher = pattern.matcher(input);
                        while (matcher.find()) {
                            String textInBetween = matcher.group(1); // Since (.*?) is capturing group 1
                            // You can insert match into a List/Collection here
                            remove.add(textInBetween);
                        }

                        input2 = input;

                        for (String s : remove) {
                            input2 = input2.replace(s + "</li>", "");
                        }

                        String regexString2 = Pattern.quote("<li>") + "(.*?)" + Pattern.quote("\n");
                        Pattern pattern2 = Pattern.compile(regexString2);
                        // text contains the full text that you want to extract data
                        Matcher matcher2 = pattern2.matcher(input2);
                        while (matcher2.find()) {
                            String textInBetween2 = matcher2.group(1); // Since (.*?) is capturing group 1
                            // You can insert match into a List/Collection here
                            remove2.add(textInBetween2);
                        }

                        for (String s : remove2){
                            String y = Html.fromHtml(s).toString();
                            resultStrs.add(y);
                        }

                        for (String s : remove2) {
                            if (s.isEmpty())
                                resultStrs.remove(s);
                        }

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                        if(resultStrs.size() == 0) {
                            quotesExist = false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ExploreRecyclerViewFragment.fillCards(resultStrs);
                            }
                        });

                        return resultStrs;
                    }
                }
            }
            setTitleToAuthor(false);
            quotesExist = false;
            return null;
        }

        private String getImageURLFromJson(String imageText)
                throws JSONException, IOException {

            // These are the names of the JSON objects that need to be extracted.
            final String QUERY = "query";
            final String PAGES = "pages";
            final String THUMBNAIL = "thumbnail";
            final String ORIGINAL = "original";

            JSONObject searchJSON = new JSONObject(imageText);
            JSONObject queryObj = searchJSON.getJSONObject(QUERY);
            if (queryObj.has("pageids")) {
                JSONArray indexArray = queryObj.getJSONArray("pageids");
                String index = indexArray.getString(0);
                JSONObject pagesObj = queryObj.getJSONObject(PAGES);
                if (pagesObj.has(index)) {
                    JSONObject idObj = pagesObj.getJSONObject(index);
                    if(idObj.has(THUMBNAIL)){
                        JSONObject thumbnailObj = idObj.getJSONObject(THUMBNAIL);
                        if(thumbnailObj.has(ORIGINAL)){
                            if(quotesExist){
                                imageURL = thumbnailObj.getString(ORIGINAL);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.v("quotes", "exists");
                                        ScrollFragment.setAuthorImage(imageURL);
                                    }
                                });
                                return null;
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            // If there's no parameter, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String format = "json";
            String prop = "extracts";
            String propImage = "pageimages";
            String piprop = "original";
            String sectionformat = "plain";

            //-----------------------------------------------------Get Quotes URL----------------------------------------------
            try {
                // Construct the URL for the Wikiquote query
                final String QUOTE_BASE_URL =
                        "https://en.wikiquote.org/w/api.php?action=query";
                final String PROP_PARAM = "prop";
                final String FORMAT_PARAM = "format";
                final String SECTIONFORMAT_PARAM = "exsectionformat";
                final String TITLE_PARAM = "titles";
                final String INDEX_PARAM = "indexpageids";


                Uri builtUri = Uri.parse(QUOTE_BASE_URL).buildUpon()
                        .appendQueryParameter(PROP_PARAM, prop)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(SECTIONFORMAT_PARAM, sectionformat)
                        .appendQueryParameter(TITLE_PARAM, params[0])
                        .appendQueryParameter(INDEX_PARAM, null)
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                if (params.length == 0) {
                    return null;
                }

                quoteJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                getQuoteDataFromJson(quoteJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }



            //-----------------------------------------------------Get Image URL----------------------------------------------
            if(quotesExist) {
                try {
                    // Construct the URL for the Wikiquote query
                    final String QUOTE_BASE_URL =
                            "https://en.wikiquote.org/w/api.php?action=query";
                    final String PROP_PARAM = "prop";
                    final String FORMAT_PARAM = "format";
                    final String SECTIONFORMAT_PARAM = "exsectionformat";
                    final String TITLE_PARAM = "titles";
                    final String INDEX_PARAM = "indexpageids";
                    final String PIPROP_PARAM = "piprop";


                    Uri builtUri = Uri.parse(QUOTE_BASE_URL).buildUpon()
                            .appendQueryParameter(PROP_PARAM, propImage)
                            .appendQueryParameter(FORMAT_PARAM, format)
                            .appendQueryParameter(SECTIONFORMAT_PARAM, sectionformat)
                            .appendQueryParameter(TITLE_PARAM, params[0])
                            .appendQueryParameter(INDEX_PARAM, null)
                            .appendQueryParameter(PIPROP_PARAM, piprop)
                            .build();

                    URL url = new URL(builtUri.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }

                    if (params.length == 0) {
                        return null;
                    }

                    imageJsonStr = buffer.toString();

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the data, there's no point in attemping
                    // to parse it.
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }
                try {
                    getImageURLFromJson(imageJsonStr);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("quotes", "do not exist");
                        ScrollFragment.setAuthorImage("none");
                    }
                });
            }
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_explore, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            startActivity(new Intent(ExploreActivity.this, MainActivity.class));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(ExploreActivity.this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String getAuthor(){
        return author;
    }
}