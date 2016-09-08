package com.hareesh.quotepad.popular;

/**
 * Created by Hareesh on 9/5/2016.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.view.MaterialListView;
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

public class PopularActivity extends AppCompatActivity {

    final static ArrayList<Quote> quoteList = new ArrayList<>();
    private Context mContext;
    private MaterialListView mListView;
    ArrayList<Card> cards;
    int c;
    Card undoCard;
    Quote undoQ;
    int cardPos;
    SharedPreferences prefs;
    Firebase mRootRef;
    Quote q;
    int score;
    private MaterialViewPager mViewPager;
    public static ArrayList<Quote> popQuotes;
    FirebaseDatabase database;
    DatabaseReference myRef, likeRef;
    Firebase likeFirebase, globalLikeFirebase;
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
                return PopularRecyclerViewFragment.newInstance();
            }

            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "";
            }
        });
        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                return HeaderDesign.fromColorResAndUrl(
                        R.color.theme_primary,
                        "");
            }
        });
        mViewPager.getViewPager().setOffscreenPageLimit(mViewPager.getViewPager().getAdapter().getCount());
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());




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
                for(Quote e : popQuotes){
                    Log.v("pop", e.getQuote());
                }
                PopularRecyclerViewFragment.fillCards(popQuotes);
            }
        });

    }
}