package com.hallliu.passwordgenerator

import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView

import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject lateinit var searchManager: SearchManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        (application as PasswordGeneratorApp).depGraph.inject(this)

        fab.setOnClickListener { _ ->
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClass(this@MainActivity, AddSiteActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchView = menu.findItem(R.id.action_search_sites).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                ComponentName(this, SearchSitesActivity::class.java)))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_search_sites -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
