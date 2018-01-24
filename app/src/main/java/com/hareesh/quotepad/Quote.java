package com.hareesh.quotepad;

/**
 * Created by Hareesh on 2016-09-07.
 */
public class Quote {
    public String quote;
    public String person;
    public int score;

    public Quote(String mQuote, String mPerson, int mScore){
        super();

        quote = mQuote;
        person = mPerson;
        score = mScore;
    }

    public String getPerson() {
        return person;
    }

    public String getQuote() {
        return quote;
    }

    public int getScore() { return score; }
}
