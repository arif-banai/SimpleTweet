package com.codepath.apps.restclienttemplate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;
import org.w3c.dom.Text;

public class DetailActivity extends AppCompatActivity {

    ImageView ivProfileImageDetail;
    TextView tvNameDetail;
    TextView tvScreenNameDetail;
    TextView tvCreatedAtDetail;
    TextView tvBodyDetail;

    Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ivProfileImageDetail = findViewById(R.id.ivProfileImageDetail);
        tvNameDetail = findViewById(R.id.tvNameDetail);
        tvScreenNameDetail = findViewById(R.id.tvScreenNameDetail);
        tvCreatedAtDetail = findViewById(R.id.tvCreatedAtDetail);
        tvBodyDetail = findViewById(R.id.tvBodyDetail);

        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra("tweet"));

        tvBodyDetail.setText(tweet.body);
        tvNameDetail.setText(tweet.user.name);
        tvScreenNameDetail.setText(tweet.user.screenName);
        tvCreatedAtDetail.setText(tweet.getFormattedTimestamp());

        Glide.with(this).load(tweet.user.profileImageURL).into(ivProfileImageDetail);
    }
}
