package com.example.daejeongyu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.naver.maps.map.NaverMapSdk

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NaverMapSdk.getInstance(this).client = NaverMapSdk.NcpKeyClient(BuildConfig.MAP_API_KEY);
    }
}