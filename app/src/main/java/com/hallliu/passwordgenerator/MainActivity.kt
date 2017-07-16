package com.hallliu.passwordgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var masterPwManager: MasterPasswordManager
    private lateinit var depGraph: ApplicationComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        depGraph = DaggerApplicationComponent.builder().androidModule(AndroidModule(this)).build()
        depGraph.inject(this)

        masterPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val hashOfContent = hashMasterPwToHex(s.toString())

                if (hashOfContent == masterPwManager.getMasterPwHash()) {
                    masterPwContainer.error = null
                } else {
                    masterPwContainer.error = getString(R.string.password_incorrect)
                }
            }
        })

        masterPasswordEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN
                    && masterPwManager.getMasterPwHash() == null) {
                depGraph.getSetMasterPwDialogFragment().show(fragmentManager, "SetMasterPw")
            }
            false
        }

        fab.setOnClickListener { _ ->
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClass(this@MainActivity, AddSiteActivity::class.java)
            startActivity(intent)
        }
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
