package com.hareesh.quotepad.explore;

/**
 * Created by Hareesh on 8/31/2016.
 */
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.hareesh.quotepad.R;
import com.squareup.picasso.Picasso;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class ScrollFragment extends Fragment {

    private static ObservableScrollView mScrollView;
    public static Context mContext;
    public static ImageView authorImage;
    public static CardView cardView1;
    public static CardView cardView2;
    public static CardView cardView3;
    public static CardView cardView4;
    public static TextView aboutText1;

    public static ScrollFragment newInstance() {
        return new ScrollFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scroll, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getContext();
        mScrollView = (ObservableScrollView) view.findViewById(R.id.scrollView);
        authorImage = (ImageView) getView().findViewById(R.id.authorImage);
        aboutText1 = (TextView) getView().findViewById(R.id.aboutText1);
        cardView1 = (CardView) getView().findViewById(R.id.card_view1);
        cardView2 = (CardView) getView().findViewById(R.id.card_view2);
        cardView3 = (CardView) getView().findViewById(R.id.card_view3);
        cardView4 = (CardView) getView().findViewById(R.id.card_view4);
        cardView1.setVisibility(View.GONE);
        cardView2.setVisibility(View.GONE);
        cardView3.setVisibility(View.GONE);
        cardView4.setVisibility(View.GONE);

        MaterialViewPagerHelper.registerScrollView(getActivity(), mScrollView, null);
    }

    public static void setAuthorImage(String URL){
        if(URL.equals("none")){
            cardView1.setVisibility(View.VISIBLE);
            cardView2.setVisibility(View.GONE);
            cardView3.setVisibility(View.GONE);
            cardView4.setVisibility(View.GONE);
            Picasso.with(mContext).load(R.drawable.question_mark).into(authorImage);
        }
        else{
            cardView1.setVisibility(View.VISIBLE);
            cardView2.setVisibility(View.VISIBLE);
            cardView3.setVisibility(View.VISIBLE);
            cardView4.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(URL).into(authorImage);
            aboutText1.setText("" + ExploreActivity.resultStrs.size() + " quotes available");

        }
    }
}