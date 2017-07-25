package com.hallliu.passwordgenerator

import android.app.ListActivity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SearchSitesActivity : ListActivity() {
    private data class SiteDisplay(val name: String, val chars: String)

    private val filteredSites = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_sites)

        val adapter = object : ArrayAdapter<SiteDisplay>(this, 0) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val display = getItem(position)
                val view = convertView ?: LayoutInflater.from(context)
                        .inflate(R.layout.site_search_list_item, parent, false)
                val siteNameField = view.findViewById(R.id.siteNameInList) as TextView
                val siteCharsField = view.findViewById(R.id.sitePwCharsInList) as TextView
                siteNameField.text = display.name
                siteCharsField.text = display.chars
                return view
            }
        }

        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
        }
    }
}
