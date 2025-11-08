package com.example.daejeongyu

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var bottomSheet: View
    private val markers = mutableMapOf<String, LocMarker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomSheet = findViewById(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            isGestureInsetBottomIgnored = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        Log.d("[APP]", "state=${bottomSheetBehavior.state}, peek=${bottomSheetBehavior.peekHeight}")

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


        findViewById<FloatingActionButton>(R.id.btn_refresh).setOnClickListener {
            val loc = locationSource.lastLocation
            if (loc != null) {
                Log.d("[APP]", String.format("%.5f / %.5f", loc.latitude, loc.longitude))

                HttpClient.getInstance().get(
                    String.format(
                        "landmarks/nearby?x=%f&y=%f",
                        loc.latitude,
                        loc.longitude
                    ), object : Callback {
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
                                    addMarker(name, lat, long)
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

    fun addMarker(name: String, lat: Double, long: Double) {
        if (markers.containsKey(name)) {
            markers[name]?.setPosition(lat, long)
        } else {
            markers.put(
                name,
                LocMarker(
                    lat,
                    long,
                    naverMap,
                    name,
                    object : StringCallBack {
                        override fun onCallback(string: String) {
                            onMarkerClicked(name)
                        }
                    }
                )
            )
        }
    }

    fun onMarkerClicked(name: String) {
        var url = String.format(
            "landmarks/details/%s",
            URLEncoder.encode(name, "UTF-8").replace("+", "%20")
        )

        Log.d("[APP]", "onMarkerClicked")

        HttpClient.getInstance().get(url, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("[APP]", e.message ?: "Err")
            }

            override fun onResponse(call: Call, response: Response) {
                val resp = response.body.string()

                val json = JSONObject(resp)
                val address = json.getString("address")
                val desc = json.getString("description")
                val imgUrl = json.getString("imageUrl")
                val recommendationCount = json.getInt("recommendationCount")
                val visitCount = json.getInt("visitCount")
                runOnUiThread { setBottomSheetData(name, address, desc, imgUrl) }
            }
        })

        url = String.format(
            "party/%s/members",
            URLEncoder.encode(name, "UTF-8").replace("+", "%20")
        )

        HttpClient.getInstance().get(url, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("[APP]", e.message ?: "Err")
            }

            override fun onResponse(call: Call, response: Response) {
                val resp = response.body.string()
                Log.d("[APP]", "Get Party")
                Log.d("[APP]", resp)
                if (!resp.isEmpty()) {
                    val json = JSONObject(resp)
                    val maxMembers = json.getInt("maxMembers")
                    val partyName = json.getString("partyName")
                    val ownerImage = json.getString("ownerImage")
                    val ownerName = json.getString("ownerName")
                    val members = json.getJSONArray("memberNames")

                    val memberList = mutableListOf<Member>()
                    memberList.add(Member(ownerName, ownerImage))

                    for (i: Int in 0..<members.length()) {
                        val obj = members.getJSONObject(i)
                        val name = obj.getString("name")
                        val img = obj.getString("image")
                        memberList.add(Member(name, img))
                    }

                    runOnUiThread { setBottomSheetParty(maxMembers, memberList) }
                } else {
                    runOnUiThread { setBottomSheetParty(0, null) }
                }
            }
        })
    }

    fun setBottomSheetParty(maxMember: Int, members: List<Member>?) {
        if (members == null) {
            bottomSheet.findViewById<TextView>(R.id.detail_party_count).text = "파티 없음"
            bottomSheet.findViewById<TextView>(R.id.detail_button).text = "파티 만들기"
            val listview = bottomSheet.findViewById<ListView>(R.id.rv_people)
            listview.adapter = null
        } else {
            bottomSheet.findViewById<TextView>(R.id.detail_party_count).text =
                String.format("%d/%d", members.size, maxMember)
            bottomSheet.findViewById<TextView>(R.id.detail_button).text = "파티 참가"
            val listview = bottomSheet.findViewById<ListView>(R.id.rv_people)
            val adapter = MemberAdapter(this, members)
            listview.adapter = adapter
        }
    }

    fun setBottomSheetData(name: String, addr: String, desc: String, imgUrl: String) {
        bottomSheet.findViewById<TextView>(R.id.detail_title).text = name
        bottomSheet.findViewById<TextView>(R.id.detail_address).text = addr
        bottomSheet.findViewById<TextView>(R.id.detail_desc).text = desc

        var bitmap: Bitmap? = null

        val imgDownload = Thread() {
            run {
                val url = URL(imgUrl)
                val conn = url.openConnection()
                conn.doOutput = true
                conn.connect()

                val istream = conn.inputStream
                bitmap = BitmapFactory.decodeStream(istream)
            }
        }
        imgDownload.start()
        imgDownload.join()
        bottomSheet.findViewById<ImageView>(R.id.detail_img).setImageBitmap(bitmap)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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