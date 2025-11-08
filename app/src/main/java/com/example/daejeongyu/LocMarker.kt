package com.example.daejeongyu

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import androidx.core.net.toUri

class LocMarker(var lat: Double, var long: Double, map: NaverMap, name: String) {
    var map: NaverMap? = map

    init {
        val marker = Marker()
        marker.position = LatLng(this.lat, this.long)
        marker.map = this.map
        marker.captionText = name
        marker.setOnClickListener {
            Log.d("[APP]", String.format("%s is Clicked!", marker.captionText))
            true
        }
    }
}