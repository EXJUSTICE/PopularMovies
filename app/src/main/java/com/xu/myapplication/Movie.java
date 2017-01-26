package com.xu.myapplication;

/**
 * Data class, holding all of the returned data from JSON call
 */

public class Movie {
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
