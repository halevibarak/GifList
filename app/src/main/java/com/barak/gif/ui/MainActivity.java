package com.barak.gif.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.barak.gif.R;
import com.barak.gif.app.AppUtility;
import com.barak.gif.model.Gif;
import com.barak.gif.service.Mp3Binder;
import com.barak.gif.service.Mp3Service;
import com.barak.gif.service.Mp3ServiceImpl;
import com.google.android.exoplayer2.ui.PlayerControlView;

import static com.barak.gif.app.DownloadToExtStrService.DOWNLOAD_ERR;
import static com.barak.gif.app.DownloadToExtStrService.DOWNLOAD_TAB_ACTION;


public class MainActivity extends AppCompatActivity implements FragmentGifList.OnCompleteListener {
    private static final int PERMISSION_REQUEST_CODE = 45454;
    private Mp3Service mMP3Service;

    private Gif gif;
    public PlayerControlView playerView;
    private boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentGifList.newInstance())
                    .commitNow();

        }
        Intent it = new Intent(this, Mp3ServiceImpl.class);
        startService(it);
        bindService(it, mConnection, 0);
        playerView = findViewById(R.id.player);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void download(Gif gif) {
        if (checkPermission()) {
            registerReceiver();
            Snackbar.make(playerView, "הורדה מתחילה", Snackbar.LENGTH_LONG).show();
            AppUtility.downLoadSongToexternalStorage(this, gif.getImages().getOriginal().getUrl(), gif.getUsername() + gif.getId());
        } else {
            this.gif = gif;
            requestPermission();
        }
    }


    private void registerReceiver() {

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DOWNLOAD_TAB_ACTION);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DOWNLOAD_TAB_ACTION)) {
                if (intent.getBooleanExtra(DOWNLOAD_ERR, false)) {
                    Snackbar.make(playerView, "תקלה", Snackbar.LENGTH_LONG).show();
                    LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(broadcastReceiver);
                    return;
                }

                Snackbar.make(playerView, "הורדה הסתיימה", Snackbar.LENGTH_LONG).show();
                LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(broadcastReceiver);
            }
        }
    };

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(playerView, "הורדה מתחילה", Snackbar.LENGTH_LONG).show();
                    registerReceiver();
                    AppUtility.downLoadSongToexternalStorage(this,  gif.getImages().getOriginal().getUrl(), gif.getUsername() + gif.getId());
                    gif = null;
                } else {
                    Snackbar.make(playerView, "Permission Denied, Please allow to proceed !", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }



    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mMP3Service = ((Mp3Binder) service).getService();
            mMP3Service.bindPlayerView(playerView);
            if (!mMP3Service._isPlay()){
                mMP3Service.playRaw(MainActivity.this,R.raw.mpforgif,playerView);
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mMP3Service = null;
            playerView.hide();
            playerView.setPlayer(null);
            mBound = false;
        }
    };



    @Override
    protected void onPause() {
        super.onPause();
        mBound = false;
        if (mMP3Service !=null && mMP3Service._isPlay()){
            mMP3Service.setPlay(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMP3Service !=null){
            mMP3Service.setPlay(true);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        mMP3Service = null;
        gif = null;
        playerView.setPlayer(null);
        playerView = null;
    }



}

