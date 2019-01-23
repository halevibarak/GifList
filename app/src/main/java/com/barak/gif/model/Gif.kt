package com.barak.gif.model

/**
 * Created by Barak Halevi on 27/11/2018.
 */
data class Gif(
        var type: String,
        var url: String,
        var id: String,
        var username: String,
        var images:ImagesType?,
        val import_datetime: String?) {
    constructor(str: String) : this("",str,"","", ImagesType(Images_(str,10,10), Images_(str,10,10)),null) {

    }


    override fun equals(other: Any?): Boolean {
        if(other == null || other !is Gif)
            return false
        return url == other.url
        return false;
    }
}

class ImagesType (
    var original: Images_,
    var fixed_width_small: Images_)

class Images_ (
    var url: String,
    var width: Int,
    var height: Int)

