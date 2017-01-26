package com.xu.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GridActivity extends AppCompatActivity {
    RecyclerView recycl;
    ArrayList<String> moviesAddressArray;
    ArrayList<Movie>allMoviesArray;
    String JSON;
    MovieAdapter mMovieAdapter;
    //Key for onSaveInstanceState check
    private static final String SORT_SETTING_KEY = "sort_setting";

    //API Sorting params

    private static final String POPULARITY_DESC = "popularity.desc";
    private static final String RATING_DESC = "vote_average.desc";
    private static final String FAVORITE = "favorite";
    String sortBy = POPULARITY_DESC;

    List<String> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recycl = (RecyclerView) findViewById(R.id.recycler);


        recycl.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // Create a grid based view, shoving 2 items per row!
        recycl.setLayoutManager(new GridLayoutManager(this, 2));
        recycl.setItemAnimator(new DefaultItemAnimator());
        //mMovieAdapter is only created if we finish making the movies
        recycl.setAdapter(mMovieAdapter);
       results = new ArrayList<>();


        //We need to make a call for the top 20 movies first,
        //ttps://api.themoviedb.org/3/discover/movie?page=1&include_video=false&include_adult=false&sort_by=popularity.desc&language=en-US&api_key=%3C%3Capi_key%3E%3E'
        //Fetch movie ids, and query for trailers and reiews

        //discoveer call actually has poster path.
        //https://developers.themoviedb.org/3/discover


        FetchMoviesTask task = new FetchMoviesTask();
        task.execute(sortBy);


    }

    private class MovieHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView poster;


        public MovieHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            poster = (ImageView) itemView.findViewById(R.id.imageView);
        }

        //TODO add in the detailactivity as well as a way of tracking which one clicked
        @Override
        public void onClick(View v) {
            /*
            Intent launch = new Intent(this, DetailActivity.class);


            launch.putExtra("AlphaKey", );
            launch.putExtra("AlphaName", );
            startActivity(launch);
            */


        }

        public void bindMovie(String address) {
            //Picasso's one step bind
            String base = "http://image.tmdb.org/t/p/";
            String size= "w185/";
            String path = address;
            String imageurl=null;


                Uri builtUri = Uri.parse(base).buildUpon()
                        .appendQueryParameter("size", size)
                        .appendQueryParameter("imagepath", path)
                        .build();

                 imageurl = builtUri.toString();



            Picasso.with(getApplicationContext()).load(imageurl).into(poster);

        }

    }


    private class MovieAdapter extends RecyclerView.Adapter<MovieHolder> {
        private ArrayList<String>movieaddresses;

        public MovieAdapter(ArrayList<String> movs) {
            movieaddresses= movs;
        }

        @Override
        public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutinflater = LayoutInflater.from(GridActivity.this);

            View view = layoutinflater.inflate(R.layout.row_grid, parent, false);
            return new MovieHolder(view);
        }

        //bind data to holder

        public void onBindViewHolder(MovieHolder holder, int position) {
            String movieaddress= movieaddresses.get(position);
            holder.bindMovie(movieaddress);
        }

        @Override
        public int getItemCount() {
            return movieaddresses.size();
        }
    }

    //TODO Fix parser to work in our situation, consult documentation
    //https://developers.themoviedb.org/3/discover
    private void getMoviesAddressesFromJson(String jsonStr) throws JSONException {
        JSONObject movieJson = new JSONObject(jsonStr);
        JSONArray movieArray = movieJson.getJSONArray("results");



        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movie = movieArray.getJSONObject(i);
            String address = movie.getString("poster_path");
            results.add(address);
        }

        //Added 26-01 parsing all movie data from json
        for (int i = 0;i<movieArray.length();i++){
            JSONObject movie = movieArray.getJSONObject(i);
            String adr = movie.getString("poster_path");
            String overv= movie.getString("overview");
            String released = movie.getString("release_date");
            String voteav =movie.getString("vote_average");
            String origtit = movie.getString("original_title");
            allMoviesArray.add(new Movie(adr,overv,released,voteav,origtit));
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //JSON Fetching code--------------------------------------------------------------------
    public class FetchMoviesTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        //TODO Progressbars


        @Override
        protected Void doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, params[0])

                        //TODO get a key for moviesDB
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.tmdb_api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                //if successful, call get movies from JSONSTR
                getMoviesAddressesFromJson(jsonStr);
                return null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            //of not successful, return null

            return null;
        }
        //TODO not sure if this checks out lol with void in argument
        @Override
        protected void onPostExecute(Void v) {
            if (moviesAddressArray != null) {
                if (mMovieAdapter == null) {
                    mMovieAdapter = new MovieAdapter(moviesAddressArray);
                    //Actually setting the adapter to view is done in main thread

                }

            }
        }
    }
}

