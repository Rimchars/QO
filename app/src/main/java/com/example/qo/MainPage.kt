package com.example.qo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager

class MainPage: AppCompatActivity() {
    var list = ArrayList<Fragment>()
    lateinit var chatbutton: ImageView
    lateinit var connactbutton: ImageView
    lateinit var findbutton: ImageView
    lateinit var currentbutton: ImageView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_optionmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent= Intent(this,SerchPerson::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings2 -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)
        val toolbar= findViewById<Toolbar>(R.id.toolbar)
        val intent1= Intent(this,DataService::class.java)
        startService(intent1)
        setSupportActionBar(toolbar)
        chatbutton = findViewById(R.id.imageView)
        connactbutton = findViewById(R.id.imageView2)
        findbutton = findViewById(R.id.imageView3)
        list.add(Chat())
        list.add(Contact())
        list.add(FriendCycle())
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPager.adapter = PagerAdapter(list)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                changetap(position)
            }
        })
        chatbutton.setOnClickListener { viewPager.currentItem = 0}
        connactbutton.setOnClickListener { viewPager.currentItem = 1 }
        findbutton.setOnClickListener { viewPager.currentItem = 2 }
        chatbutton.isSelected = true
        currentbutton =chatbutton
    }

    fun changetap(position: Int) {
        currentbutton.isSelected = false
        when (position) {
            0 -> {
                chatbutton.isSelected = true
                currentbutton = chatbutton
            }
            1 -> {
                connactbutton.isSelected = true
                currentbutton = connactbutton
            }
            2 -> {
                findbutton.isSelected = true
                currentbutton = findbutton
            }
        }
    }

    fun PagerAdapter(list: List<Fragment>): FragmentPagerAdapter {
        return object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getCount(): Int = list.size
            override fun getItem(position: Int): Fragment = list[position]
        }
    }
}