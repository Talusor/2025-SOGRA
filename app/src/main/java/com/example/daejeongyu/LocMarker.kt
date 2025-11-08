package com.example.daejeongyu

import android.util.Log
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker

class LocMarker(lat: Double, long: Double, map: NaverMap, name: String, var callBack: StringCallBack) {
    var map: NaverMap? = map
    var marker: Marker = Marker()

    init {
        marker.position = LatLng(lat, long)
        marker.map = this.map
        marker.captionText = name
        marker.setOnClickListener {
            Log.d("[APP]", String.format("%s is Clicked!", marker.captionText))
            callBack.onCallback(name)
            true
        }
    }

    fun setPosition(lat: Double, long: Double) {
        this.marker.position = LatLng(lat, long)
    }

    fun setCallback(callBack: StringCallBack)
    {
        this.callBack = callBack
    }
}