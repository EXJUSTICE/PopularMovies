package com.xu.myapplication;

import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

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

//TODO mod image size, mod settings, mod savedinstance state


//Checking for internet connection http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
public class GridActivity extends AppCompatActivity {
    RecyclerView recycl;
    FrameLayout progressWrapper;

    ArrayList<Movie>allMoviesArray;
    String JSON;
    MovieAdapter mMovieAdapter;


    //API Sorting params

    private static final String POPULARITY_DESC = "popularity.desc";
    private static final String RATING_DESC = "vote_average.desc";

    String sortBy = null;
    String jsonStr =null;

    ArrayList<String> results;
    boolean internetAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressWrapper =(FrameLayout)findViewById(R.id.progressBarWrapper) ;
        recycl = (RecyclerView) findViewById(R.id.recycler);
        sortBy=POPULARITY_DESC;


        recycl.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
         //Create a grid based view, shoving 2 items per row!
        recycl.setLayoutManager(new GridLayoutManager(this, 2));
        recycl.setItemAnimator(new DefaultItemAnimator());
        //mMovieAdapter is only created if we finish making the movies

        results = new ArrayList<>();
        allMoviesArray = new ArrayList<>();
        internetAccess=isOnline();

        if(savedInstanceState != null) {
            sortBy=savedInstanceState.getString("SORTBY");
            if(savedInstanceState.containsKey("RESULTS")){
                results = savedInstanceState.getStringArrayList("RESULTS");
            }
            if(savedInstanceState.containsKey("ALLMOVIES")){
                allMoviesArray=(ArrayList<Movie>)savedInstanceState.getSerializable("ALLMOVIES");
            }


        }
        //We need to make a call for the top 20 movies first,
        //ttps://api.themoviedb.org/3/discover/movie?page=1&include_video=false&include_adult=false&sort_by=popularity.desc&language=en-US&api_key=%3C%3Capi_key%3E%3E'
        //Fetch movie ids, and query for trailers and reiews

        //discoveer call actually has poster path.
        //https://developers.themoviedb.org/3/discover

        if(internetAccess ==true){
            FetchMoviesTask task = new FetchMoviesTask();
            task.execute(sortBy);
        }else{
            Toast noconnect =Toast.makeText(this,"Please enable your internet connection and restart",Toast.LENGTH_LONG);
            noconnect.show();
        }








    }
    //Handle config change via onSaveInstanceState
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (results != null && !results.isEmpty()){
            bundle.putStringArrayList("RESULTS",results);
        }
        if(allMoviesArray != null && !allMoviesArray.isEmpty()){
            bundle.putSerializable("ALLMOVIES",allMoviesArray);
        }
        bundle.putString("SORTBY",sortBy);

    }


//-------------------------------------------------ViewHolder code------------------------

    private class MovieHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView poster;
        Movie selectedmovie;


        public MovieHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            poster = (ImageView) itemView.findViewById(R.id.imageView);
        }

        //TODO add in the detailactivity as well as a way of tracking which one clicked
        @Override
        public void onClick(View v) {

            Intent launch = new Intent(GridActivity.this, DetailActivity.class);


            launch.putExtra("movie",selectedmovie);
            startActivity(launch);



        }

        public void bindMovie(String address) {
            //TODO errors in this part
            String base = "http://image.tmdb.org/t/p/";
            String size= "w342/";
            String path = address;
            String imageurl=null;


                StringBuilder sb= new StringBuilder(base);
                sb.append(size);
                sb.append(address);
                imageurl = sb.toString();





            Picasso.with(getApplicationContext()).load(imageurl).placeholder(R.color.colorAccent).into(poster);


        }
        //Bind movie into the viewholder;
        public void bindMovieInfo(Movie movie){
            selectedmovie=movie;

        }

    }


    private class MovieAdapter extends RecyclerView.Adapter<MovieHolder> {
        Context mContext;
        private ArrayList<String>movieaddresses;
        private ArrayList<Movie>moviecollection;

        public MovieAdapter(ArrayList<String> movs,ArrayList<Movie>allmovies,Context context) {
            mContext=context;
            movieaddresses= movs;
            moviecollection=allmovies;
        }

        @Override
        public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutinflater = LayoutInflater.from(GridActivity.this);

            View view = layoutinflater.inflate(R.layout.row_grid, parent, false);
            final MovieHolder viewholder = new MovieHolder(view);

            return viewholder;
        }

        //bind data to holder

        public void onBindViewHolder(MovieHolder holder, int position) {
            String movieaddress= movieaddresses.get(position);
            Movie selectedmovie = allMoviesArray.get(position);

            holder.bindMovie(movieaddress);
            holder.bindMovieInfo(selectedmovie);
        }

        @Override
        public int getItemCount() {
            return movieaddresses.size();
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
        if (id == R.id.sortPopular) {
            sortBy=POPULARITY_DESC;

            if(internetAccess ==true){
                results.clear();;
                allMoviesArray.clear();
                FetchMoviesTask task2 = new FetchMoviesTask();
                task2.execute(sortBy);
                mMovieAdapter.notifyDataSetChanged();
                recycl.invalidate();
            }else{
                Toast noconnect =Toast.makeText(this,"Please enable your internet connection and restart",Toast.LENGTH_LONG);
                noconnect.show();
            }
            return true;
        }

        if (id == R.id.sortRating) {
            sortBy=RATING_DESC;

            if(internetAccess ==true){
                //TODO in order for this to work, arraylists must be cleared, otherwise wont respond
                //TODO 2 to initialization
                results.clear();;
                allMoviesArray.clear();
                FetchMoviesTask task2 = new FetchMoviesTask();
                task2.execute(sortBy);
                mMovieAdapter.notifyDataSetChanged();
                recycl.invalidate();
            }else{
                Toast noconnect =Toast.makeText(this,"Please enable your internet connection and restart",Toast.LENGTH_LONG);
                noconnect.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    //JSON Fetching code--------------------------------------------------------------------
    public class FetchMoviesTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        //TODO Progressbars
        //TODO Fix parser to work in our situation, consult documentation
        //https://developers.themoviedb.org/3/discover
        private void getMoviesAddressesFromJson(String jsonStr) throws JSONException {
            JSONObject movieJson = new JSONObject(jsonStr);
            JSONArray movieArray = movieJson.getJSONArray("results");


            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                String address = movie.getString("poster_path");
                results.add(address);


                /*Toast toast= Toast.makeText(GridActivity.this,address,Toast.LENGTH_LONG);
                toast.show();
                */


            }

            //Added 26-01 parsing all movie data from json
            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                String adr = movie.getString("poster_path");
                String overv = movie.getString("overview");
                String released = movie.getString("release_date");
                String voteav = movie.getString("vote_average");
                String origtit = movie.getString("original_title");
                allMoviesArray.add(new Movie(adr, overv, released, voteav, origtit));
            }


            //28.01 Moved All adapter code here. Previously worked but stopped
            //Problem may be due to results arraylist being empty before being loaded, so moved adapter code here
            //So that there must be a full array

            if (mMovieAdapter == null) {
                mMovieAdapter = new MovieAdapter(results,allMoviesArray,GridActivity.this);

                //Actually setting the adapter to view is done in main thread

            }

            recycl.setAdapter(mMovieAdapter);
            recycl.invalidate();
        }

        @Override
        protected void onPreExecute(){
            progressWrapper.setVisibility(View.VISIBLE);
        }


        @Override
        protected Void doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;



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


            //of not successful, return null

            return null;
        }
        //TODO not sure if this checks out lol with void in argument
        @Override
        protected void onPostExecute(Void v) {
            progressWrapper.setVisibility(View.GONE);
            try {
                //PARSING WORKS TESTED WITH TOASTS
                getMoviesAddressesFromJson(jsonStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

        }
    }
}

