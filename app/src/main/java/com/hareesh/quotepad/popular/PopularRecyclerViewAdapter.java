package com.hareesh.quotepad.popular;

/**
 * Created by Hareesh on 8/31/2016.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hareesh.quotepad.Quote;
import com.hareesh.quotepad.R;
import com.hareesh.quotepad.explore.ExploreActivity;
import com.hareesh.quotepad.explore.ItemClickSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class PopularRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Object> contents;
    private Context context;
    View view;
    int pos;
    RecyclerView mRecyclerView;
    RecyclerView.ViewHolder holder;
    GestureDetector gestureDetector;
    FirebaseDatabase database;
    DatabaseReference myRef, likeRef;
    Firebase likeFirebase, globalLikeFirebase;
    FirebaseUser user;
    Quote q1, q2, q3, qtemp;
    boolean quoteLiked;

    public PopularRecyclerViewAdapter(List<Object> contents, Context context, RecyclerView mRecyclerView) {
        this.contents = contents;
        this.context = context;
        this.mRecyclerView = mRecyclerView;
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = null;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_small, parent, false);

        holder = new RecyclerView.ViewHolder(view) {
        };

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView quoteText = (TextView) holder.itemView.findViewById(R.id.quoteText);
        if (position < PopularActivity.popQuotes.size()) {
            quoteText.setText(PopularActivity.popQuotes.get(position).getQuote());
        }
    }
}
