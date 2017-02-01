package com.xu.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Data class, holding all of the returned data from JSON call
 */

public class Movie implements Serializable {
    String posterpath;
    String overview;
    String releasedate;
    String voteaverage;
    String originaltitle;

    public Movie(String postpath, String overv, String released, String voteav,String origtit){
        this.posterpath=postpath;
        this.overview=overv;
        this.releasedate=released;
        this.voteaverage=voteav;
        this.originaltitle=origtit;
    }




}
