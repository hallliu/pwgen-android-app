package com.hallliu.passwordgenerator

import android.widget.Toast
import kotlinx.android.synthetic.main.content_edit_site.*
import java.util.regex.Pattern

class EditSiteActivity: EditSiteActivityBase() {
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
            dbInterface.updateSiteInDb(PasswordSpecification(
                    siteName = siteNameEditText.text.toString(),
                    pwVersion = passwordVersionEditText.text.toString().toInt(),
                    pwLength = passwordLengthEditText.text.toString().toInt(),
                    permittedChars = getPermittedChars(),
                    requirements = reqs)) { result ->
                when (result) {
                    DbInterface.Companion.UpdateSiteResult.SUCCESS -> {
                        Toast.makeText(this@EditSiteActivity, "Site updated successfully",
                                Toast.LENGTH_SHORT).show()
                        this@EditSiteActivity.finish()
                    }
                    DbInterface.Companion.UpdateSiteResult.NO_SUCH_SITE->
                        Toast.makeText(this@EditSiteActivity, "Cannot modify site name",
                                Toast.LENGTH_SHORT).show()
                    DbInterface.Companion.UpdateSiteResult.OTHER_ERROR ->
                        Toast.makeText(this@EditSiteActivity, "Database error",
                                Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun initializeFields() {
        val siteName = intent.getStringExtra(EXTRA_SITE_NAME)
        if (siteName == null) {
            Toast.makeText(this@EditSiteActivity, "No site name supplied",
                    Toast.LENGTH_SHORT).show()
            this@EditSiteActivity.finish()
            return
        }
        dbInterface.getPwSpecForSite(siteName) { passwordSpec ->
            if (passwordSpec == null) {
                runOnUiThread {
                    Toast.makeText(this@EditSiteActivity, "$siteName does not exist",
                            Toast.LENGTH_SHORT).show()
                    this@EditSiteActivity.finish()
                }
                return@getPwSpecForSite
            }
            val symbols = extractSymbolsFromPermittedChars(passwordSpec.permittedChars)
            runOnUiThread {
                siteNameEditText.setText(siteName)
                siteNameEditText.isEnabled = false
                uppercaseSwitch.isChecked = passwordSpec.permittedChars.contains(UPPERS)
                numbersSwitch.isChecked = passwordSpec.permittedChars.contains(NUMBERS)
                symbolsSwitch.isChecked = !symbols.isEmpty()
                allowedSymbols = symbols

                passwordLengthEditText.setText(passwordSpec.pwLength.toString())
                passwordVersionEditText.setText(passwordSpec.pwVersion.toString())
                requirementsRegexEditText.setText(getRequirementsText(passwordSpec.requirements))
            }
        }
    }

    private fun extractSymbolsFromPermittedChars(permitted: CharSequence): String {
        return permitted.filter { !it.isLetterOrDigit() }.toString()
    }

    private fun getRequirementsText(requirements: List<Pattern>): String {
        return requirements.joinToString(separator = "\n") { it.pattern() }
    }
}