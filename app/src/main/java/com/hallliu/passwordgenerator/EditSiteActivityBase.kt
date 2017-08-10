package com.hallliu.passwordgenerator

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_edit_site.*
import kotlinx.android.synthetic.main.content_edit_site.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.inject.Inject

abstract class EditSiteActivityBase : AppCompatActivity() {
    companion object {
        const val ACTION_ADD_NEW_SITE = "com.hallliu.passwordgenerator.ACTION_ADD_NEW_SITE"
        const val ACTION_EDIT_SITE = "com.hallliu.passwordgenerator.ACTION_EDIT_SITE"
        const val EXTRA_SITE_NAME = "com.hallliu.passwordgenerator.EXTRA_SITE_NAME"
    }

    protected class RequirementMisformatException(offendingPattern: String) :
            Exception(offendingPattern)

    var allowedSymbols = ""
    @Inject lateinit var dbInterface: DbInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_site)
        setSupportActionBar(toolbar)
        val depGraph = (application as PasswordGeneratorApp).depGraph
        injectMembers()

        symbolsSwitch.setOnLongClickListener {
            val frag = depGraph.getEditIncludedSymbolsDialogFragment()
            val args = Bundle()
            args.putString(EditIncludedSymbolsDialogFragment.EXTRA_INITIAL_SYMBOLS, allowedSymbols)
            frag.arguments = args
            frag.show(fragmentManager, "EditSymbols")
            true
        }

        setupSaveButton()
        initializeFields()
    }

    protected fun getPermittedChars(): String {
        // NEVER MODIFY THE ORDER IN WHICH THESE APPEAR
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

    protected fun parseSiteRequirements(): List<Pattern> {
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

    abstract fun injectMembers()
    abstract fun setupSaveButton()
    abstract fun initializeFields()
}
