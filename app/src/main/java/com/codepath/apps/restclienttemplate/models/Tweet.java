package com.codepath.apps.restclienttemplate.models;

import com.codepath.apps.restclienttemplate.TimeFormatter;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Tweet {

    public String body;
    public long tweetId;
    public String createdAt;
    public User user;

    //Empty constructor required by Parceler library
    public Tweet() {
    }

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        tweet.body = jsonObject.getString("text");
        tweet.tweetId = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");

        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));

        return tweet;
    }

    public String getFormattedTimestamp() {
        return TimeFormatter.getTimeDifference(createdAt);
    }
}
