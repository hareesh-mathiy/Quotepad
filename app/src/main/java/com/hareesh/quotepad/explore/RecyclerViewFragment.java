package com.hareesh.quotepad.explore;

/**
 * Created by Hareesh on 8/31/2016.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator;
import com.hareesh.quotepad.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class RecyclerViewFragment extends Fragment {

    static final boolean GRID_LAYOUT = false;
    private static RecyclerView mRecyclerView;
    private static RecyclerView.Adapter mAdapter;
    private static List<Object> mContentItems = new ArrayList<>();
    public static RecyclerViewFragment newInstance() {
        return new RecyclerViewFragment();
    }
    public static TextView quoteText;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager;
        quoteText = (TextView) getView().findViewById(R.id.quoteText);

        if (GRID_LAYOUT) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
        } else {
            layoutManager = new LinearLayoutManager(getActivity());
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        //Use this now
        mRecyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());

        mAdapter = new TestRecyclerViewAdapter(mContentItems);

    }

    public static void fillCards(ArrayList<String> resultStrs){
        Log.v("resultStrs", resultStrs.size()+"");
        mRecyclerView.scrollToPosition(0);
        mContentItems.clear();
        mRecyclerView.setAdapter(mAdapter);
        {
            for (int i = 0; i < resultStrs.size(); ++i) {
                mContentItems.add(new Object());
            }
            mAdapter.notifyDataSetChanged();
        }
    }
}