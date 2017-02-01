package com.xu.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

//28012017 DetailActivity works as well, just needs aligning and polishing

public class DetailActivity extends AppCompatActivity {
    TextView originaltit;
    ImageView poster;
    TextView overv;
    TextView release;
    TextView voteAv;
    Movie movie;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        originaltit = (TextView) findViewById(R.id.originalTitle);
        poster= (ImageView)findViewById(R.id.poster_image);
        overv =(TextView)findViewById(R.id.overview);
        voteAv = (TextView)findViewById(R.id.voteaverage);
        release= (TextView)findViewById(R.id.releasedate);

        Intent intent = getIntent();
        movie = (Movie)intent.getSerializableExtra("movie");

        originaltit.setText(movie.originaltitle);
        overv.setText(movie.overview);
        release.setText(movie.releasedate);
        voteAv.setText(movie.voteaverage);

        loadImage();


    }
    public void loadImage(){
        String base = "http://image.tmdb.org/t/p/";
        String size= "w780/";
        String path = movie.posterpath;
        String imageurl=null;


        StringBuilder sb= new StringBuilder(base);
        sb.append(size);
        sb.append(path);
        imageurl = sb.toString();





        Picasso.with(getApplicationContext()).load(imageurl).placeholder(R.color.colorAccent).into(poster);
    }

}
