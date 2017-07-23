package com.hallliu.passwordgenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var depGraph: ApplicationComponent
    }

    @Inject lateinit var masterPwManager: MasterPasswordManager
    @Inject lateinit var dbInterface: DbInterface
    @Inject lateinit var clipboardManager: ClipboardManager
    var hasValidPassword = false

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
                    hasValidPassword = true
                } else {
                    masterPwContainer.error = getString(R.string.password_incorrect)
                    hasValidPassword = false
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

        val siteNameAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        siteNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        siteSelectionSpinner.adapter = siteNameAdapter

        siteSelectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                passwordTextView.setText(R.string.select_site_prompt)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                if (!hasValidPassword) {
                    passwordTextView.setText(R.string.master_pw_is_wrong)
                    return
                }
                val siteName = siteNameAdapter.getItem(position)
                dbInterface.getPwSpecForSite(siteName) { result ->
                    runOnUiThread {
                        passwordTextView.text = when (result) {
                            null -> getString(R.string.select_site_prompt)
                            else -> try {
                                generatePw(result, getMasterPassword())
                            } catch (e: PasswordMisspecificationException) {
                                getString(R.string.bad_pw_spec)
                            }
                        }
                    }
                }
            }
        }

        siteFilterEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    return
                }
                dbInterface.getSitesLike(s.toString()) { result ->
                    runOnUiThread {
                        siteNameAdapter.clear()
                        siteNameAdapter.addAll(result)
                        siteNameAdapter.sort { s1, s2 -> s1.compareTo(s2) }
                    }
                }
            }
        })

        copyPasswordButton.setOnClickListener { _ ->
            val clipData = ClipData.newPlainText("copied_password", passwordTextView.text)
            clipboardManager.primaryClip = clipData
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

    private fun getMasterPassword(): String {
        return masterPasswordEditText.text.toString()
    }
}
