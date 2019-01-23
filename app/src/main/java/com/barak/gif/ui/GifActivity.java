package com.barak.gif.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.barak.gif.R;
import com.bumptech.glide.Glide;

public class GifActivity extends AppCompatActivity {
    public static final String LINK = "LINK";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif);
        Bundle b = getIntent().getExtras();
        String link = "";
        if (b != null)
            link = b.getString(LINK);
        imageView = findViewById(R.id.imgview);
        Glide.with(this).asGif().load(link).into(imageView);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


}
