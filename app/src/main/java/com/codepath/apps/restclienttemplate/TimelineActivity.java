package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.graphics.Movie;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 20;

    private TwitterClient client;
    private RecyclerView rvTweets;
    private TweetsAdapter adapter;
    private List<Tweet> tweets;

    private SwipeRefreshLayout swipeContainer;

    private EndlessRecyclerViewScrollListener scrollListener;

    private long lowestTweetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        lowestTweetId = Long.MAX_VALUE;

        client = TwitterApp.getRestClient(this);

        swipeContainer = findViewById(R.id.swipeContainer);

        //Configure refreshing colors
        swipeContainer.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );

        // Find the recycler view
        rvTweets = findViewById(R.id.rvTweets);
        // Initialize the list of tweets and adapter from the data source
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        // Setup recycler view: layout manager and adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };

        rvTweets.setAdapter(adapter);





        populateHomeTimeline();

        rvTweets.addOnScrollListener(scrollListener);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("TwitterClient", "Content is being refreshed");
                //Reset lowestTweetId before refreshing page
                //to insure infinite scrolling works properly
                lowestTweetId = Long.MAX_VALUE;
                populateHomeTimeline();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu; add items to the action bar if present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.compose) {
            //Tapped on compose icon
            Intent i = new Intent(this, ComposeActivity.class);
            startActivityForResult(i, REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //REQUEST_CODE is defined above
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Pull info out of the data intent (Tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Update recycler view with this tweet
            tweets.add(0, tweet);
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void loadNextDataFromApi(int offset) {
        // 1. Send an API request to retrieve appropriate paginated data
        client.getUpdatedHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TwitterClient", response.toString());

                List<Tweet> tweetsToAdd = new ArrayList<>();

                //Iterate through the list of tweets from JSONArray
                for(int i = 0; i < response.length(); i++) {
                    try {
                        // 2. Deserialize and construct new model objects from the API response
                        JSONObject jsonTweetObject = response.getJSONObject(i);
                        Tweet tweet = Tweet.fromJson(jsonTweetObject);

                        //Update lowestTweetId if needed
                        if(tweet.tweetId < lowestTweetId) {
                            lowestTweetId = tweet.tweetId;
                        }

                        //Add the tweet into the data source
                        tweetsToAdd.add(tweet);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // 3. Append the new data objects to the existing set of items inside the array of items
                adapter.addTweets(tweetsToAdd);
                // 4. Notify the adapter of the new items made with `notifyDataSetChanged()`
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("TwitterClient", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("TwitterClient", errorResponse.toString());
            }
        }, lowestTweetId);

    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TwitterClient", response.toString());

                List<Tweet> tweetsToAdd = new ArrayList<>();

                //Iterate through the list of tweets from JSONArray
                for(int i = 0; i < response.length(); i++) {
                    try {
                        //Convert each JSON object from list into a Tweet object
                        JSONObject jsonTweetObject = response.getJSONObject(i);
                        Tweet tweet = Tweet.fromJson(jsonTweetObject);

                        if(tweet.tweetId < lowestTweetId) {
                            lowestTweetId = tweet.tweetId;
                        }

                        //Add the tweet into the data source
                        tweetsToAdd.add(tweet);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //Clear existing data
                adapter.clear();

                //Show the newly received data
                adapter.addTweets(tweetsToAdd);
                adapter.notifyDataSetChanged();
                scrollListener.resetState();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("TwitterClient", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("TwitterClient", errorResponse.toString());
            }
        });
    }
}
