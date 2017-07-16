package com.hallliu.passwordgenerator

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import javax.inject.Inject

class SetMasterPwDialogFragment @Inject constructor() : DialogFragment() {
    @Inject lateinit var masterPwManager: MasterPasswordManager

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val dialogLayout = inflater.inflate(R.layout.set_master_pw_dialog, null)
        val setMasterPwEditText = dialogLayout.findViewById(R.id.setMasterPwEditText) as EditText
        val confirmMasterPwEditText =
                dialogLayout.findViewById(R.id.confirmSetMasterPwEditText) as EditText

        val dialog = builder.setView(dialogLayout)
                .setTitle(R.string.set_master_pw_title)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (confirmMasterPwEditText.text.toString()
                            != setMasterPwEditText.text.toString()) {
                        Toast.makeText(context, getString(R.string.password_mismatch),
                                Toast.LENGTH_SHORT).show()
                    } else {
                        masterPwManager.setMasterPwHash(
                                hashMasterPwToHex(setMasterPwEditText.text.toString()))
                        this@SetMasterPwDialogFragment.dialog.dismiss()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    this@SetMasterPwDialogFragment.dialog.cancel()
                }
                .create()

        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false }

        confirmMasterPwEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val confirmLayout =
                        dialogLayout.findViewById(R.id.confirmSetMasterPwLayout) as TextInputLayout
                if (s.toString() != setMasterPwEditText.text.toString()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    confirmLayout.error = getString(R.string.password_mismatch)
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    confirmLayout.error = null
                }
            }
        })

        return dialog
    }
}