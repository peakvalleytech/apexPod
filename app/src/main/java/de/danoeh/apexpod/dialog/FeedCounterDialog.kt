package de.danoeh.apexpod.dialog

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import de.danoeh.apexpod.R
import de.danoeh.apexpod.core.preferences.UserPreferences

class FeedCounterDialog {
    fun showDialog(activity : Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.pref_nav_drawer_feed_counter_title)
            .setItems(R.array.nav_drawer_feed_counter_options,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                }
            })

    }
}