package com.hallliu.passwordgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_edit_site.*
import kotlinx.android.synthetic.main.content_edit_site.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.inject.Inject

class EditSiteActivity : AppCompatActivity() {
    companion object {
        const val ACTION_ADD_NEW_SITE = "com.hallliu.passwordgenerator.ACTION_ADD_NEW_SITE"
        const val ACTION_EDIT_SITE = "com.hallliu.passwordgenerator.ACTION_EDIT_SITE"
        const val EXTRA_SITE_NAME = "com.hallliu.passwordgenerator.EXTRA_SITE_NAME"
    }

    private class RequirementMisformatException(offendingPattern: String) :
            Exception(offendingPattern)

    var allowedSymbols = SYMBOLS
    @Inject lateinit var dbInterface: DbInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_site)
        setSupportActionBar(toolbar)
        val depGraph = (application as PasswordGeneratorApp).depGraph
        depGraph.inject(this)

        symbolsSwitch.setOnLongClickListener {
            val frag = depGraph.getEditIncludedSymbolsDialogFragment()
            val args = Bundle()
            args.putString(EditIncludedSymbolsDialogFragment.EXTRA_INITIAL_SYMBOLS, allowedSymbols)
            frag.arguments = args
            frag.show(fragmentManager, "EditSymbols")
            true
        }

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
                    pwVersion = 1,
                    pwLength = 16, //TODO
                    permittedChars = getPermittedChars(),
                    requirements = reqs)) { result ->
                when (result) {
                    DbInterface.Companion.DbUpdateResult.SUCCESS -> {
                        Toast.makeText(this@EditSiteActivity, "Site added",
                                Toast.LENGTH_SHORT).show()
                        this@EditSiteActivity.finish()
                    }
                    DbInterface.Companion.DbUpdateResult.ALREADY_EXISTS ->
                        Toast.makeText(this@EditSiteActivity, "Site already exists",
                                Toast.LENGTH_SHORT).show()
                    DbInterface.Companion.DbUpdateResult.OTHER_ERROR ->
                        Toast.makeText(this@EditSiteActivity, "Database error",
                                Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getPermittedChars(): String {
        var result = ""
        if (uppercaseSwitch.isChecked) {
            result += UPPERS
        }
        if (numbersSwitch.isChecked) {
            result += NUMBERS
        }
        if (symbolsSwitch.isChecked) {
            result += allowedSymbols
        }
        return result
    }

    private fun parseSiteRequirements(): List<Pattern> {
        val requirementsInput = requirementsRegexEditText.text
        return requirementsInput.split("\n")
                .filter { it.isNotBlank() }
                .map {
                    try {
                        Pattern.compile(it.trim())
                    } catch (e : PatternSyntaxException) {
                        throw RequirementMisformatException(e.pattern)
                    }
                }
    }
}
