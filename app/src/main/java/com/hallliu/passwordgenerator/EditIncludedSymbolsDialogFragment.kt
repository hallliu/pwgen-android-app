package com.hallliu.passwordgenerator

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import javax.inject.Inject

class EditIncludedSymbolsDialogFragment @Inject constructor() : DialogFragment() {
    companion object {
        const val EXTRA_INITIAL_SYMBOLS = "com.hallliu.passwordgenerator.EXTRA_INITIAL_SYMBOLS"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        val symbolsEditText = EditText(activity)
        symbolsEditText.inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_NORMAL or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        symbolsEditText.setText(arguments.getString(EXTRA_INITIAL_SYMBOLS, SYMBOLS))

        val dialog = builder.setView(symbolsEditText)
                .setTitle(R.string.edit_pw_symbols_title)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    (activity as AddSiteActivity).allowedSymbols = symbolsEditText.text.toString()
                }
                .create()

        return dialog
    }
}