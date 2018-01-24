package com.hareesh.quotepad.explore;

/**
 * Created by Hareesh on 8/31/2016.
 */

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hareesh.quotepad.Quote;
import com.hareesh.quotepad.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class ExploreRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
    public static boolean quoteLiked;
    Toast toast;

    public ExploreRecyclerViewAdapter(List<Object> contents, Context context, RecyclerView mRecyclerView) {
        this.contents = contents;
        this.context = context;
        this.mRecyclerView = mRecyclerView;
        Firebase.setAndroidContext(context);
        quoteLiked = false;
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(user.getUid());
        likeRef = myRef.child("Liked");
        likeFirebase = new Firebase(likeRef.getRoot().toString().concat("/").concat("Users").concat("/").concat(user.getUid()).concat("/").concat("Liked"));
        globalLikeFirebase = new Firebase(likeRef.getRoot().toString().concat("/").concat("Liked"));
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = null;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_small_explore, parent, false);

        holder = new RecyclerView.ViewHolder(view) {
        };

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView quoteText = (TextView) holder.itemView.findViewById(R.id.quoteText);
        if (position < ExploreActivity.resultStrs.size()) {
            quoteText.setText(ExploreActivity.resultStrs.get(position));
        }

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {

            }
        });


        ItemClickSupport.addTo(mRecyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {

                pos = position;
                qtemp = new Quote(ExploreActivity.resultStrs.get(position), ExploreActivity.getAuthor(), 0);

                likeFirebase.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(com.firebase.client.MutableData mutableData) {
                        for (com.firebase.client.MutableData snap : mutableData.getChildren()) {
                            Quote qsnap = new Quote(snap.child("quote").getValue().toString(),
                                    snap.child("author").getValue().toString(), 0);
                            if ((qsnap.getQuote().equals(qtemp.getQuote()) && qsnap.getPerson().equals(qtemp.getPerson()))) {
                                quoteLiked = false;
                                likeFirebase.child(snap.getKey()).setValue(null);
                                return null;
                            }
                        }
                        quoteLiked = true;
                        Map<String, Object> map = new HashMap<>();
                        map.put("author", qtemp.getPerson());
                        map.put("quote", qtemp.getQuote());
                        likeFirebase.push().setValue(map);
                        return null;
                    }

                    @Override
                    public void onComplete(FirebaseError firebaseError, boolean b, com.firebase.client.DataSnapshot dataSnapshot) {
                        if(quoteLiked){
                            makeToast();
                        }
                    }
                });


                globalLikeFirebase.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(com.firebase.client.MutableData mutableData) {

                        for (com.firebase.client.MutableData snap : mutableData.getChildren()) {
                            Quote qsnap = new Quote(snap.child("quote").getValue().toString(),
                                    snap.child("author").getValue().toString(), snap.child("score").getValue(Integer.class));
                            if ((qsnap.getQuote().equals(qtemp.getQuote()) && qsnap.getPerson().equals(qtemp.getPerson()))) {
                                Firebase scoreSnap = globalLikeFirebase.child(snap.getKey()).child("score");
                                if(quoteLiked) {
                                    scoreSnap.setValue(qsnap.getScore() + 1);
                                } else{
                                    scoreSnap.setValue(qsnap.getScore() - 1);
                                    if(qsnap.getScore()-1 == 0){
                                        globalLikeFirebase.child(snap.getKey()).setValue(null);

                                    }
                                }
                                return null;
                            }
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("author", qtemp.getPerson());
                        map.put("quote", qtemp.getQuote());
                        map.put("score", 1);
                        globalLikeFirebase.push().setValue(map);
                        return null;
                    }

                    @Override
                    public void onComplete(FirebaseError firebaseError, boolean b, com.firebase.client.DataSnapshot dataSnapshot) {

                    }
                });
                return true;
            }
        });
    }

    public void makeToast(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.toast_like, (ViewGroup) mRecyclerView.findViewById(R.id.relativeLayout1));
        toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(view);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 750);
    }
}
