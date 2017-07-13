package com.hallliu.passwordgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.security.MessageDigest

// TODO: replace with SharedPrefs lookup
const val MASTER_SHA256 = "0b47c69b1033498d5f33f5f7d97bb6a3126134751629f4d0185c115db44c094e"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        masterPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val digest = MessageDigest.getInstance("SHA-256")
                val output = digest.digest(s.toString().toByteArray())
                val HEX_CHARS = "0123456789abcdef".toCharArray()

                fun ByteArray.toHex() : String{
                    val result = StringBuffer()

                    forEach {
                        val octet = it.toInt()
                        val firstIndex = (octet and 0xF0).ushr(4)
                        val secondIndex = octet and 0x0F
                        result.append(HEX_CHARS[firstIndex])
                        result.append(HEX_CHARS[secondIndex])
                    }

                    return result.toString()
                }
                if (output.toHex() == MASTER_SHA256) {
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

        fab.setOnClickListener { _ ->
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClass(this@MainActivity, AddSiteActivity::class.java)
            startActivity(intent)
        }
        val graph = DaggerApplicationComponent.builder().androidModule(AndroidModule(this)).build()
        graph.inject(this)
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
