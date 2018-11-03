package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class TutorialActivity extends AppCompatActivity {

    private static final String VIDEO_TUTORIAL_ID = "video_tutorial_id";
    private static final String YOUTUBE_VIDEO_LINK = "http://www.youtube.com/watch?v=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        findViewById(R.id.btn_tutorial_skip).setOnClickListener(view -> finish());
        findViewById(R.id.btn_tutorial_watch_on_youtube).setOnClickListener(view -> {
            String videoLink = YOUTUBE_VIDEO_LINK + getYoutubeTutorialVideoID();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoLink));
            startActivity(intent);
            finish();
        });
    }

    private String getYoutubeTutorialVideoID(){
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.fetch();
        remoteConfig.activateFetched();

        return remoteConfig.getString(VIDEO_TUTORIAL_ID);
    }
}
