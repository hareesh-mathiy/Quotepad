package com.hareesh.quotepad.popular;

/**
 * Created by Hareesh on 9/5/2016.
 */
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Transaction;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hareesh.quotepad.Quote;
import com.hareesh.quotepad.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PopularActivity extends AppCompatActivity {

    final static ArrayList<Quote> quoteList = new ArrayList<>();
    private Context mContext;
    private MaterialViewPager mViewPager;
    public static ArrayList<Quote> popQuotes;
    FirebaseDatabase database;
    DatabaseReference myRef, likeRef;
    Firebase globalLikeFirebase;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
        popQuotes = new ArrayList<>();
        popQuotes.clear();
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
                        return PopularTodayRecyclerViewFragment.newInstance();
                    case 1:
                        return PopularAllTimeRecyclerViewFragment.newInstance();
                    default:
                        return PopularAllTimeRecyclerViewFragment.newInstance();
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
                        return "Today";
                    case 1:
                        return "All Time";
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

        getPopQuotes();
    }

    private void getPopQuotes(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(user.getUid());
        likeRef = myRef.child("Liked");
        globalLikeFirebase = new Firebase(likeRef.getRoot().toString().concat("/").concat("Liked"));
        globalLikeFirebase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(com.firebase.client.MutableData mutableData) {

                for (com.firebase.client.MutableData snap : mutableData.getChildren()) {
                    Quote qsnap = new Quote(snap.child("quote").getValue().toString(),
                            snap.child("author").getValue().toString(), snap.child("score").getValue(Integer.class));
                    popQuotes.add(qsnap);
                }
                return null;
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, com.firebase.client.DataSnapshot dataSnapshot) {
                if(popQuotes.size() == 0){
                    getPopQuotes();
                }
                else{
                    Collections.sort(popQuotes, new Comparator<Quote>(){
                        public int compare(Quote q1, Quote q2) {
                            return Integer.valueOf(q2.getScore()).compareTo(q1.getScore());
                        }
                    });
                    PopularAllTimeRecyclerViewFragment.fillCards(popQuotes);
                }
            }
        });
    }
}