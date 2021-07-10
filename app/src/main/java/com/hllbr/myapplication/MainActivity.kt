package com.hllbr.myapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.hllbr.myapplication.CustomAdapter
import com.hllbr.myapplication.Place
import com.hllbr.myapplication.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    //MyProperties
    var placeArray = ArrayList<Place>()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_place,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_place_option){
            val intent = Intent(applicationContext,MapsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try{
            //operation of filters in Database Actions area
            val database = openOrCreateDatabase("PLACE",Context.MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM places",null)
            val addressIndex = cursor.getColumnIndex("address")
            val latitudeIndex = cursor.getColumnIndex("latitude")
            val longitudeIndex = cursor.getColumnIndex("longitude")
            while (cursor.moveToNext()){
                val addresSFromDatabase = cursor.getString(addressIndex)
                val latitudeFromDatabase = cursor.getDouble(latitudeIndex)
                val longitudeFromDatabase = cursor.getDouble(longitudeIndex)

                val myPlace = Place(addresSFromDatabase,
                latitudeFromDatabase,
                longitudeFromDatabase)

                placeArray.add(myPlace)
            }
            cursor.close()
        }catch (e : Exception){
            e.printStackTrace()
        }
        val customAdapter = CustomAdapter(placeArray,this)
        listView.adapter = customAdapter

        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@MainActivity,MapsActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("selectedPlace",placeArray.get(position))
            startActivity(intent)
        }
    }

}