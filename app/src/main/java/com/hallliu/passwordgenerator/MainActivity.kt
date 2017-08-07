package com.hallliu.passwordgenerator

import android.app.ActionBar
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

    private val searchTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            // submit is disabled.
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            if (newText == null || newText.isNullOrEmpty()) {
                val searchSiteFragment =
                        fragmentManager.findFragmentByTag(SearchSitesFragment.FRAGMENT_TAG)
                if (searchSiteFragment == null) {
                    // Just ignore this case
                    return true
                }
                fragmentManager.popBackStack()
            } else {
                var searchSiteFragment =
                        fragmentManager.findFragmentByTag(SearchSitesFragment.FRAGMENT_TAG)
                if (searchSiteFragment == null) {
                    searchSiteFragment =
                            (application as PasswordGeneratorApp).depGraph.getSearchSitesFragment()
                }

                if (searchSiteFragment !is SearchSitesFragment) {
                    throw RuntimeException("Wrong fragment type?")
                }

                if (!searchSiteFragment.isVisible) {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.add(R.id.mainActivityContent, searchSiteFragment,
                            SearchSitesFragment.FRAGMENT_TAG)
                    transaction.detach(fragmentManager.findFragmentByTag(
                            PasswordGenerationFragment.FRAGMENT_TAG))
                    transaction.addToBackStack(null)
                    transaction.commit()
                }

                searchSiteFragment.onQueryUpdated(newText)
            }
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO: check intent to see whether it's a search intent
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val injector = (application as PasswordGeneratorApp).depGraph
        injector.inject(this)

        val transaction = fragmentManager.beginTransaction()
        val pwgenFragment = injector.getPasswordGenerationFragment()
        transaction.add(R.id.mainActivityContent, pwgenFragment,
                PasswordGenerationFragment.FRAGMENT_TAG)
        transaction.commit()

        fab.setOnClickListener { _ ->
            val intent = Intent(EditSiteActivity.ACTION_ADD_NEW_SITE)
            intent.setClass(this@MainActivity, EditSiteActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchView = menu.findItem(R.id.action_search_sites).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                ComponentName(this, this::class.java)))
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(searchTextListener)
        searchView.layoutParams = ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
        super.onCreateOptionsMenu(menu)
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
