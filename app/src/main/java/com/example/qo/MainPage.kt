package com.example.qo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.JsonReader
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.navigation.NavigationView
import okhttp3.internal.http2.Header
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors


class MainPage: AppCompatActivity() {
    var list = ArrayList<Fragment>()
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_optionmenu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_button -> {
                val intent = Intent(this, SerchPerson::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)
        val intent1 = Intent(this, DataService::class.java)
        startService(intent1)
        sendmessage(this@MainPage).sendmessage("getcontacts:" + MainActivity.id)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        //每次打开都获取一言
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Respond when the drawer's position changes
            }
            override fun onDrawerOpened(drawerView: View) {
                //headerlayout滚动到底部
                val headerView = navigationView.getHeaderView(0)
                val layoutParams = headerView.layoutParams
                layoutParams.height = drawerLayout.height / 2
                headerView.layoutParams = layoutParams
                //定义headerlayout
                Thread {
                    val url = URL("https://v1.hitokoto.cn/")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val inputStream = connection.inputStream
                    val reader = JsonReader(InputStreamReader(inputStream))
                    reader.beginObject()
                    var hitokoto = ""
                    while (reader.hasNext()) {
                        if (reader.nextName() == "hitokoto") {
                            hitokoto = reader.nextString()
                            break
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.close()
                    runOnUiThread {
                        val headerTextView = findViewById<View>(R.id.yiyan) as TextView
                        headerTextView.text = hitokoto
                    }
                }.start()
            }
            override fun onDrawerClosed(drawerView: View) {
                // Respond when the drawer is closed
            }
            override fun onDrawerStateChanged(newState: Int) {
                // Respond when the drawer motion state changes
            }
        })
        val headerView = navigationView.getHeaderView(0)
        headerView.setOnTouchListener { v, event ->
            drawerLayout.openDrawer(GravityCompat.START)
            true
        }
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation view item clicks here.
            when (menuItem.itemId) {
                R.id.a1-> {
                    // Handle the click on the first item
                }
                R.id.a2 -> {
                    // Handle the click on the second item
                }
                // Add more cases for more items
                R.id.a3 -> {
                    sendmessage(this).sendmessage("logout:"+MainActivity.id)
                    MainActivity.islogin="false"
                    val sharedPreferences1 = getSharedPreferences("login", MODE_PRIVATE)
                    val editor1 = sharedPreferences1.edit()
                    editor1.putString("islogin", "false")
                    editor1.apply()
                    DatabaseHelper.messages_table= ""
                    DatabaseHelper.contacts_table = ""
                    DatabaseHelper.GptHelper_table = ""
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    //关闭service
                    stopService(intent1)
                    startActivity(intent)
                    finish()
                }

            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        list.add(Chat())
        list.add(Contact())
        val viewPager = findViewById<NonSwipeableViewPager>(R.id.viewPager)
        viewPager.adapter = PagerAdapter(list)
        viewPager.setPageTransformer(true, StackPageTransformer())
        // Removed setPageTransformer
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (Chat.drawerOpen) {

                    viewPager.isSwipeable = false
                }
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    1 -> sendmessage(this@MainPage).sendmessage("getcontacts:" + MainActivity.id)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // No operation needed
            }
        })
    }

    fun PagerAdapter(list: List<Fragment>): FragmentPagerAdapter {
        return object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getCount(): Int = list.size
            override fun getItem(position: Int): Fragment = list[position]
        }
    }
}