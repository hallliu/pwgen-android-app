package com.hallliu.passwordgenerator

import android.content.Context
import android.os.Bundle
import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.support.annotation.UiThread
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import javax.inject.Inject

import kotlinx.android.synthetic.main.fragment_password_generation.*
import java.util.regex.Pattern

class PasswordGenerationFragment @Inject constructor() : Fragment() {

    companion object {
        const val FRAGMENT_TAG = "PasswordGenerationFragment"
    }

    @Inject lateinit var masterPwManager: MasterPasswordManager
    @Inject lateinit var dbInterface: DbInterface
    @Inject lateinit var clipboardManager: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_password_generation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        masterPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val hashOfContent = hashMasterPwToHex(s.toString())

                if (hashOfContent == masterPwManager.getMasterPwHash()) {
                    getView()
                    masterPwContainer.error = null
                    masterPwManager.masterPassword = s.toString()
                } else {
                    masterPwContainer.error = getString(R.string.password_incorrect)
                    masterPwManager.masterPassword = ""
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

        masterPasswordEditText.setText(masterPwManager.masterPassword)

        val siteNameAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item)
        siteNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        siteSelectionSpinner.adapter = siteNameAdapter

        siteSelectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                passwordTextView.setText(R.string.select_site_prompt)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                refreshPasswordDisplay()
            }
        }

        siteFilterEditText.addTextChangedListener(object : TextWatcher {
            var siteNames: MutableList<String> = mutableListOf()

            override fun afterTextChanged(s: Editable?) {
                refreshPasswordDisplay()
            }

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
                siteSelectionSpinner.setSelection(0)
            }
        })

        copyPasswordButton.setOnClickListener { _ ->
            val clipData = ClipData.newPlainText("copied_password", passwordTextView.text)
            clipboardManager.primaryClip = clipData
        }
    }

    @UiThread
    private fun refreshPasswordDisplay() {
        if (masterPwManager.masterPassword.isNullOrEmpty()) {
            passwordTextView.setText(R.string.master_pw_is_wrong)
            return
        }

        val siteName = siteSelectionSpinner.selectedItem as String?
        if (siteName == null) {
            passwordTextView.text = getString(R.string.select_site_prompt)
            return
        }

        dbInterface.getPwSpecForSite(siteName) { result ->
            val text = when (result) {
                null -> getString(R.string.select_site_prompt)
                else -> try {
                    generatePw(result, masterPwManager.masterPassword)
                } catch (e: PasswordMisspecificationException) {
                    getString(R.string.bad_pw_spec)
                }
            }
            activity.runOnUiThread {
                passwordTextView.text = text
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }
}
