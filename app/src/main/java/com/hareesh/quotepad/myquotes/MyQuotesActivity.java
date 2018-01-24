package com.hareesh.quotepad.myquotes;

/**
 * Created by Hareesh on 9/5/2016.
 */

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Transaction;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hareesh.quotepad.Quote;
import com.hareesh.quotepad.R;
import com.hareesh.quotepad.explore.ExploreActivity;
import com.hareesh.quotepad.explore.ExploreRecyclerViewFragment;
import com.hareesh.quotepad.explore.ScrollFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyQuotesActivity extends AppCompatActivity {

    final static ArrayList<Quote> quoteList = new ArrayList<>();
    private Context mContext;
    private MaterialViewPager mViewPager;
    public static ArrayList<Quote> myLikedQuotes;
    FirebaseDatabase database;
    DatabaseReference myRef, likeRef;
    Firebase likeFirebase;
    FirebaseUser user;
    public static ImageView authorImage;
    String imageJsonStr;
    String imageURL;
    boolean gettingImage = false;
    public static ArrayList<String> imageURLs;
    public static FetchImageTask imageTask;
    boolean doneGettingAllImages = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myquotes);
        authorImage = (ImageView) findViewById(R.id.authorImageCard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
        myLikedQuotes = new ArrayList<>();
        myLikedQuotes.clear();
        imageURLs = new ArrayList<>();
        imageURLs.clear();

        setTitle("");
        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);
        toolbar = mViewPager.getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        mViewPager.getViewPager().setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position % 2) {
                    case 0:
                        return MyQuotesRecyclerViewFragment.newInstance();
                    case 1:
                        return MyLikedQuotesRecyclerViewFragment.newInstance();
                    default:
                        return MyLikedQuotesRecyclerViewFragment.newInstance();
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
                        return "My Quotes";
                    case 1:
                        return "My Likes";
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

        getLikedQuotes();
    }

    private void getLikedQuotes() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(user.getUid());
        likeRef = myRef.child("Liked");
        likeFirebase = new Firebase(likeRef.getRoot().toString().concat("/").concat("Users").concat("/").concat(user.getUid()).concat("/").concat("Liked"));
        Log.v("dsfsdf", likeFirebase.toString());
        likeFirebase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(com.firebase.client.MutableData mutableData) {
                for (com.firebase.client.MutableData snap : mutableData.getChildren()) {
                    Quote qsnap = new Quote(snap.child("quote").getValue().toString(),
                            snap.child("author").getValue().toString(), 0);
                    myLikedQuotes.add(qsnap);
                }
                return null;
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, com.firebase.client.DataSnapshot dataSnapshot) {
                for (Quote q : myLikedQuotes) {
                    Log.v("dsfsdf", q.getQuote());
                }
                if (myLikedQuotes.size() == 0) {
                    // Log.v("dsfsdf", "no quotes");
                    getLikedQuotes();
                } else {
                    imageTask = new FetchImageTask();
                    imageTask.execute("sdfs");
                }
            }
        });
    }

    public class FetchImageTask extends AsyncTask<String, Void, ArrayList<String>> {

        private final String LOG_TAG = ExploreActivity.FetchQuoteTask.class.getSimpleName();

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            for (Quote q : myLikedQuotes) {

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

                //-----------------------------------------------------Get Image URL----------------------------------------------
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
                            .appendQueryParameter(TITLE_PARAM, q.getPerson())
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
                    Log.i("MyQuoteActivityError", "JSONException");
                } catch (IOException e) {
                    Log.i("MyQuoteActivityError", "IOException");
                }
            }
            doneGettingAllImages = true;
            return null;
        }
        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            MyLikedQuotesRecyclerViewFragment.fillCards(myLikedQuotes);
        }
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
                if (idObj.has(THUMBNAIL)) {
                    JSONObject thumbnailObj = idObj.getJSONObject(THUMBNAIL);
                    if (thumbnailObj.has(ORIGINAL)) {
                        imageURL = thumbnailObj.getString(ORIGINAL);
                        Log.i("Image URL", imageURL);
                        imageURLs.add(imageURL);
                        gettingImage = false;
                        return null;
                    }
                }
                else{
                    Log.i("MyQuoteActivity", "no image.");
                    imageURLs.add("noimage");
                }
            }
        }
        gettingImage = false;
        return null;
    }
}