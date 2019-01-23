package com.barak.gif.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.support.annotation.RawRes
import android.support.v4.content.LocalBroadcastManager
import com.barak.gif.app.App
import com.barak.gif.app.DownloadToExtStrService.DOWNLOAD_ERR
import com.barak.gif.app.DownloadToExtStrService.DOWNLOAD_TAB_ACTION
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.RepeatModeUtil
import java.io.IOException



class Mp3ServiceImpl : Service(), Mp3Service, Player.EventListener, ExtractorMediaSource.EventListener {

    private var mPlayer: SimpleExoPlayer? = null
    private var mPlayerView: PlayerControlView? = null
    private var mIsPlaying: Boolean = false

    override fun onBind(arg0: Intent): IBinder? {
        return Mp3Binder(this)
    }

    override fun onUnbind(intent: Intent): Boolean {
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent != null) {
            if (intent.extras != null) {
                return Service.START_NOT_STICKY
            }
            if (ACAO_STOP == intent.getStringExtra(EXTRA_ACAO)) {
                stop()
            }

        }
        return Service.START_NOT_STICKY
    }


    override fun playRaw(context: Context, @RawRes rawVideoRes: Int, playerView_: PlayerControlView?) {
        mPlayer = ExoPlayerFactory.newSimpleInstance(App.getInstance())
        mIsPlaying = true
        val dataSpec = DataSpec(RawResourceDataSource.buildRawResourceUri(rawVideoRes))
        val rawResourceDataSource = RawResourceDataSource(context)
        rawResourceDataSource.open(dataSpec)
        mPlayer?.addListener(this)
        val factory: DataSource.Factory = DataSource.Factory { rawResourceDataSource }
        mPlayer!!.prepare(LoopingMediaSource(ExtractorMediaSource.Factory(factory).createMediaSource(rawResourceDataSource.uri)))
        mPlayer?.playWhenReady = true
        if (mPlayerView == null && playerView_ != null) {

            mPlayerView = playerView_
            mPlayerView?.player = mPlayer
            mPlayerView?.show()


        }
    }
    override fun _isPlay(): Boolean {
        return mIsPlaying
    }
    override fun stop() {
        mPlayer?.release()
        mPlayerView = null
        stopSelf()
        mIsPlaying = false

    }


    override fun bindPlayerView(playerView: PlayerControlView) {
        if (mPlayer != null) {
            playerView.player = mPlayer
        }
    }

    override fun unBindPlayerView(playerView: PlayerControlView) {
        if (mPlayer != null) {
            playerView.player = null
        }
    }

    override fun setPlay(bPlay: Boolean) {
        if (mPlayer != null) {
            mPlayer!!.playWhenReady = bPlay
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mPlayer?.release()
        mPlayer = null
        mPlayerView = null


    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        mIsPlaying = playWhenReady
        if (playbackState == 4) {
            stop()
        } else if (playbackState == 3 && playWhenReady && mPlayerView != null) {
            mPlayerView?.setShowMultiWindowTimeBar(true)
            mPlayerView?.showShuffleButton = false
            mPlayerView?.repeatToggleModes = RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE
            Handler().postDelayed({
                mPlayerView?.alpha = 1.0f
                mPlayerView = null

            }, 400)

        }

    }

    override fun onRepeatModeChanged(repeatMode: Int) {

    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

    }

    override fun onPlayerError(error: ExoPlaybackException?) {

    }

    override fun onPositionDiscontinuity(reason: Int) {

    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

    }

    override fun onSeekProcessed() {

    }

    override fun onLoadError(error: IOException) {
        stop()
        val intentLocal = Intent(DOWNLOAD_TAB_ACTION)
        intentLocal.putExtra(DOWNLOAD_ERR, true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentLocal)
    }



    companion object {
        @JvmField
        val EXTRA_ACAO = "acao"
        @JvmField
        val ACAO_STOP = "stop"
    }
}
