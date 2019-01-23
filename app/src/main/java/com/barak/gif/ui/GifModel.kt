package com.barak.gif.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.barak.gif.app.App
import com.barak.gif.model.Gif
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


/**
 * Created by Barak on 1/10/2018.
 */

class GifModel(application: Application, pharam: String) : AndroidViewModel(application) {

    val URL__ = "http://api.giphy.com/v1/gifs/search?api_key=Zz7XnA0RZzJJetQAQv1e2c7ErivA9F5u&limit=7&q="
//    val URL__ = "http://api.giphy.com/v1/gifs/search?api_key=vE545tCszu9dERIlNQMcYlsluoJzWCxu&limit=7&q="
    private var articleList: JsonLiveData? = null

    private val refresh = MutableLiveData<Int>()


    init {
        if (articleList == null)
            articleList = JsonLiveData(this.getApplication(), pharam)
    }

    fun getArticleList(): MutableLiveData<List<Gif>>? {
        return articleList
    }

    fun refreshData(pharam: String) {
        val jsonObjReq = object : JsonObjectRequest(Method.GET, URL__+pharam, JSONObject("{}"),
                Response.Listener<JSONObject> { response ->
                    val gson = Gson()
                    var array: JSONArray? = null
                    try {
                        array = response.getJSONArray("data")
                        if (array.length() > 0) {
                            val gifList = ArrayList<Gif>()
                            for (i in 0 until array.length()) {
                                val jObject = array.get(i) as JSONObject
                                val movie = gson.fromJson(jObject.toString(), Gif::class.java)
                                gifList.add(movie)
                            }
                            articleList!!.postValue(gifList)
                            refresh.setValue(1)
                        }

                    } catch (e: JSONException) {
                        refresh.setValue(1)
                    }
                },
                Response.ErrorListener { error ->
                    val gson = Gson()
                }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                return headers
            }
        }
        App.getInstance().addToRequestQueue(jsonObjReq)

    }
    inner class JsonLiveData(context: Context, pharam: String) : MutableLiveData<List<Gif>>() {

        init {
            LoadData(context, pharam)
        }

        private fun LoadData(context: Context, pharam: String) {
            val jsonObjReq = object : JsonObjectRequest(Method.GET, URL__+pharam, JSONObject("{}"),
                    Response.Listener<JSONObject> { response ->
                        val gson = Gson()
                        var array: JSONArray? = null
                        try {
                            array = response.getJSONArray("data")
                            if (array.length() > 0) {
                                val gifList = ArrayList<Gif>()
                                for (i in 0 until array.length()) {
                                    val jObject = array.get(i) as JSONObject
                                    val gif = gson.fromJson(jObject.toString(), Gif::class.java)
                                    gifList.add(gif)
                                }
                                articleList!!.postValue(gifList)
                                refresh.setValue(1)
                            }

                        } catch (e: JSONException) {
                            value = null
                            refresh.setValue(1)
                        }
                    },
                    Response.ErrorListener { error ->
                        articleList!!.postValue(null)
                        refresh.setValue(1)
                    }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    return headers
                }
            }
            App.getInstance().addToRequestQueue(jsonObjReq)
        }

    }


}


