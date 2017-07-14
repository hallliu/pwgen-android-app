package com.hallliu.passwordgenerator

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private var masterPasswordHash: String? = null

    private var masterPwHashChangedListener = SharedPreferences.OnSharedPreferenceChangeListener {
        sharedPreferences, key ->
        if (key == MASTER_PW_HASH_KEY) {
            masterPasswordHash = sharedPreferences.getString(MASTER_PW_HASH_KEY, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val prefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        masterPasswordHash = prefs.getString(MASTER_PW_HASH_KEY, null)
        prefs.registerOnSharedPreferenceChangeListener(masterPwHashChangedListener)

        masterPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val hashOfContent = hashMasterPwToHex(s.toString())

                if (hashOfContent == masterPasswordHash) {
                    masterPwCorrectnessImage.setImageResource(R.drawable.ic_check_black_24dp)
                    masterPwCorrectnessImage.setColorFilter(
                            getColor(android.R.color.holo_green_light))
                } else {
                    masterPwCorrectnessImage.setImageResource(R.drawable.ic_close_black_24dp)
                    masterPwCorrectnessImage.setColorFilter(
                            getColor(android.R.color.holo_red_light))
                }
            }
        })

        masterPasswordEditText.setOnTouchListener { _, event ->
            Log.i("MainActivity", "touched")
            if (event.action == MotionEvent.ACTION_DOWN && masterPasswordHash == null) {
                // TODO: show the set master pw dialog
                Toast.makeText(this@MainActivity, "No Password Set", Toast.LENGTH_SHORT).show()
            }
            false
        }

        fab.setOnClickListener { _ ->
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClass(this@MainActivity, AddSiteActivity::class.java)
            startActivity(intent)
        }
        val graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(this)).build()
        graph.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        val prefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.unregisterOnSharedPreferenceChangeListener(masterPwHashChangedListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
