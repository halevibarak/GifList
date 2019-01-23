package com.barak.gif.ui

import com.barak.gif.model.Gif

/**
 * Created by Barak on 24/08/2017.
 */

interface ActionInterface {

    fun goDownload(gif: Gif)

    fun goFullScreen(gif: Gif)

}
