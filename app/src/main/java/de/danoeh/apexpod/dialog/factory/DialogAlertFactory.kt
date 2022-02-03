package de.danoeh.apexpod.dialog.factory

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class DialogAlertFactory {
    companion object {
        fun create(
            context: Context,
            title: String,
            msg: String,
            positiveLabel: String? = null,
            negativeLabel: String? = null,
            neutralLabel: String? = null
        ): AlertDialog {
            val builder = AlertDialog.Builder(context)
            builder
                .setTitle(title)
                .setMessage(msg)
            if (positiveLabel != null) {
                builder.setPositiveButton(
                    positiveLabel,
                    DialogInterface.OnClickListener { dialog, which -> })
            }
            if (negativeLabel != null) {
                builder.setNegativeButton(
                    negativeLabel,
                    DialogInterface.OnClickListener { dialog, which -> })
            }
            if (neutralLabel != null) {
                builder.setNeutralButton(
                    neutralLabel,
                    DialogInterface.OnClickListener { dialog, which -> })
            }
            return builder.create()
        }
    }
}