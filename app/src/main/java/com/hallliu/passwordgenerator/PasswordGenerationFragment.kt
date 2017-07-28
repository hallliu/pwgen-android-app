package com.hallliu.passwordgenerator

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import javax.inject.Inject

import kotlinx.android.synthetic.main.fragment_password_generation.*
import java.util.regex.Pattern

class PasswordGenerationFragment : Fragment() {

    @Inject lateinit var masterPwManager: MasterPasswordManager
    @Inject lateinit var dbInterface: DbInterface
    @Inject lateinit var clipboardManager: ClipboardManager
    var hasValidPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity.application as PasswordGeneratorApp).depGraph.inject(this)
        if (arguments != null) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_password_generation, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                (activity.application as PasswordGeneratorApp).depGraph
                        .getSetMasterPwDialogFragment().show(fragmentManager, "SetMasterPw")
            }
            false
        }

        val siteNameAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item)
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
                    activity.runOnUiThread {
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
            var siteNames: MutableList<String> = mutableListOf()

            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    siteNames.clear()
                    return
                }
                val filterString = s.toString()
                if (siteNames.isEmpty()) {
                    dbInterface.getSitesLike(filterString) { result ->
                        activity.runOnUiThread {
                            result.mapTo(siteNames, {it})
                            siteNameAdapter.clear()
                            siteNameAdapter.addAll(result)
                            siteNameAdapter.sort { s1, s2 -> s1.compareTo(s2) }
                        }
                    }
                } else {
                    siteNameAdapter.clear()
                    siteNameAdapter.addAll(
                            siteNames.filter { Pattern.matches(".*$filterString.*", it) })
                    siteNameAdapter.sort { s1, s2 -> s1.compareTo(s2) }
                }
            }
        })

        copyPasswordButton.setOnClickListener { _ ->
            val clipData = ClipData.newPlainText("copied_password", passwordTextView.text)
            clipboardManager.primaryClip = clipData
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun getMasterPassword(): String {
        return masterPasswordEditText.text.toString()
    }
}