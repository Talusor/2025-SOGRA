package com.example.daejeongyu

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class MemberAdapter : BaseAdapter {
    private var ctx: Context
    private var data: List<Member>
    private var layoutInflater: LayoutInflater

    constructor(ctx: Context, data: List<Member>)
    {
        this.ctx = ctx
        this.data = data
        this.layoutInflater = LayoutInflater.from(ctx)
    }

    override fun getCount(): Int {
        return this.data.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view = this.layoutInflater.inflate(R.layout.member_view, null)

        val bytes = Base64.decode(this.data[position].img, Base64.DEFAULT)
        view.findViewById<ImageView>(R.id.member_img).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        view.findViewById<TextView>(R.id.member_name).text = this.data[position].name

        return view
    }

    override fun getItem(position: Int): Any? {
        return this.data[position]
    }

    override fun getItemId(position: Int): Long {
        return position as Long
    }
}