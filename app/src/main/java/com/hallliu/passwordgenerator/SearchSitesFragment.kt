package com.hallliu.passwordgenerator

import android.app.ListFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.util.regex.Pattern
import javax.inject.Inject

class SearchSitesFragment @Inject constructor() : ListFragment() {
    companion object {
        const val FRAGMENT_TAG = "SearchSiteFragment"
    }

    private data class SiteDisplay(val name: String, val chars: String)

    @Inject lateinit var dbInterface: DbInterface

    private val knownSites = mutableListOf<SiteDisplay>()

    private lateinit var adapter: ArrayAdapter<SiteDisplay>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = object : ArrayAdapter<SiteDisplay>(activity, 0) {
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
        adapter.addAll(knownSites)
        adapter.sort { s1, s2 -> s1.name.compareTo(s2.name) }
        listAdapter = adapter
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun onQueryUpdated(query: String) {
        if (query.isNullOrEmpty()) {
            knownSites.clear()
            adapter.clear()
            return
        }
        if (knownSites.isEmpty()) {
            populateKnownSites(query)
        } else {
            adapter.clear()
            adapter.addAll(knownSites.filter { Pattern.matches(".*$query.*", it.name) })
            adapter.sort { s1, s2 -> s1.name.compareTo(s2.name) }
        }
    }

    private fun populateKnownSites(query: String) {
        dbInterface.getSitesLike(query, arrayOf(COLUMN_SITE_NAME, COLUMN_PERMITTED_CHARS), {
            SiteDisplay(it.getStringByName(COLUMN_SITE_NAME),
                    it.getStringByName(COLUMN_PERMITTED_CHARS))
        }) { result ->
            result.mapTo(knownSites) { it }
            activity?.runOnUiThread {
                adapter.clear()
                adapter.addAll(knownSites)
                adapter.sort { s1, s2 -> s1.name.compareTo(s2.name) }
            }
        }
    }
}

