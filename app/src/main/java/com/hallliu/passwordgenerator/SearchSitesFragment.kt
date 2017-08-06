package com.hallliu.passwordgenerator

import android.app.ListFragment
import android.os.Bundle
import android.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import java.util.regex.Pattern
import javax.inject.Inject

class SearchSitesFragment @Inject constructor() : ListFragment() {
    companion object {
        const val FRAGMENT_TAG = "SearchSiteFragment"
    }

    private data class SiteDisplay(val name: String, val chars: String)

    private val knownSites = mutableListOf<SiteDisplay>()
    @Inject lateinit var dbInterface: DbInterface
    private lateinit var adapter: ArrayAdapter<SiteDisplay>
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

    override fun onListItemClick(l: ListView?, v: View, position: Int, id: Long) {
        val checkbox = v.findViewById(R.id.siteListItemCheckbox) as CheckBox
        checkbox.toggle()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_site_list_fragment, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val searchMenuItem = menu.findItem(R.id.action_search_sites)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.isIconified = false
        searchView.setQuery(currentQuery, false)
    }

    fun onQueryUpdated(query: String) {
        currentQuery = query
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

