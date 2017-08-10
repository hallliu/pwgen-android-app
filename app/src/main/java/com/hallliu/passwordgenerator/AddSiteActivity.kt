package com.hallliu.passwordgenerator

import android.widget.Toast
import java.util.regex.Pattern
import kotlinx.android.synthetic.main.content_edit_site.*

class AddSiteActivity : EditSiteActivityBase() {
    companion object {
        private const val DEFAULT_PASSWORD_LENGTH = 16
        private const val DEFAULT_PASSWORD_VERSION = 1
    }
    override fun initializeFields() {
        uppercaseSwitch.isChecked = true
        numbersSwitch.isChecked = true
        symbolsSwitch.isChecked = false
        passwordLengthEditText.setText(DEFAULT_PASSWORD_LENGTH.toString())
        passwordVersionEditText.setText(DEFAULT_PASSWORD_VERSION.toString())
        allowedSymbols = SYMBOLS
    }

    override fun injectMembers() {
        val depGraph = (application as PasswordGeneratorApp).depGraph
        depGraph.inject(this)
    }

    override fun setupSaveButton() {
        saveSiteButton.setOnClickListener {
            val reqs: List<Pattern>
            try {
                reqs = parseSiteRequirements()
            } catch (e: RequirementMisformatException) {
                Toast.makeText(this, "Bad pattern: " + e.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dbInterface.saveSiteInDb(PasswordSpecification(
                    siteName = siteNameEditText.text.toString(),
                    pwVersion = passwordVersionEditText.text.toString().toInt(),
                    pwLength = passwordLengthEditText.text.toString().toInt(),
                    permittedChars = getPermittedChars(),
                    requirements = reqs)) { result ->
                when (result) {
                    DbInterface.Companion.SaveSiteResult.SUCCESS -> {
                        Toast.makeText(this@AddSiteActivity, "Site added",
                                Toast.LENGTH_SHORT).show()
                        this@AddSiteActivity.finish()
                    }
                    DbInterface.Companion.SaveSiteResult.ALREADY_EXISTS ->
                        Toast.makeText(this@AddSiteActivity, "Site already exists",
                                Toast.LENGTH_SHORT).show()
                    DbInterface.Companion.SaveSiteResult.OTHER_ERROR ->
                        Toast.makeText(this@AddSiteActivity, "Database error",
                                Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}