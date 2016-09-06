package com.hareesh.quotepad.explore;

/**
 * Created by Hareesh on 8/31/2016.
 */
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hareesh.quotepad.R;

import java.util.List;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class TestRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Object> contents;

    public TestRecyclerViewAdapter(List<Object> contents) {
        this.contents = contents;
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_small, parent, false);

        return new RecyclerView.ViewHolder(view) {
        };
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView quoteText = (TextView) holder.itemView.findViewById(R.id.quoteText);
        if(position < ExploreActivity.resultStrs.size()){
            quoteText.setText(ExploreActivity.resultStrs.get(position));
        }
    }
}
