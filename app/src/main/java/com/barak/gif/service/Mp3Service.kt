package com.barak.gif.service

import android.content.Context
import android.support.annotation.RawRes
import com.google.android.exoplayer2.ui.PlayerControlView


interface Mp3Service {
    fun playRaw(context: Context, @RawRes rawVideoRes: Int, playerView_: PlayerControlView?)
    fun stop()
    fun bindPlayerView(playerView: PlayerControlView)
    fun unBindPlayerView(playerView: PlayerControlView)
    fun _isPlay(): Boolean
    fun setPlay(b: Boolean) {

    }

}
