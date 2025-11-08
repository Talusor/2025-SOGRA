package com.example.daejeongyu

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private val client = OkHttpClient()
    private val markers = mutableMapOf<String, LocMarker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }


        findViewById<Button>(R.id.test_button2).setOnClickListener {
            val loc = locationSource.lastLocation
            if (loc != null) {
                Log.d("[APP]", String.format("%.5f / %.5f", loc.latitude, loc.longitude))

                val req = Request.Builder()
                    .url(
                        String.format(
                            "http://10.0.2.2:8080/landmarks/nearby?x=%f&y=%f",
                            loc.latitude,
                            loc.longitude
                        )
                    )
                    .build()

                client.newCall(req).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("[APP]", "Failed req")
                        Log.e("[APP]", e.message ?: "Err")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val resp = response.body.string()
                        Log.d("[APP]", resp)
                        val json = JSONArray(resp)

                        for (i: Int in 0..<json.length()) {
                            val landmark = json.getJSONObject(i)
                            runOnUiThread {
                                val name = landmark.getString("name")
                                val lat = landmark.getDouble("x")
                                val long = landmark.getDouble("y")
                                if (markers.containsKey(name)) {
                                    markers[name]?.setPosition(lat, long)
                                } else {
                                    markers.put(
                                        name,
                                        LocMarker(
                                            lat,
                                            long,
                                            naverMap,
                                            name
                                        )
                                    )
                                }
                            }
                        }

                    }
                })
            }

        }

        NaverMapSdk.getInstance(this).client = NaverMapSdk.NcpKeyClient(BuildConfig.MAP_API_KEY)
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_frag)
        if (mapFragment != null) {
            (mapFragment as MapFragment).getMapAsync(this)
        }

        locationSource = FusedLocationSource(this, 100)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (
            locationSource.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        ) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
    }
}