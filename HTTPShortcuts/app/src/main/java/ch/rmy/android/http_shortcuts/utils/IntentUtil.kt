package ch.rmy.android.http_shortcuts.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import java.util.*

object IntentUtil {

    private const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
    private const val ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT"
    private const val EXTRA_SHORTCUT_DUPLICATE = "duplicate"

    @JvmOverloads fun createIntent(context: Context, shortcutId: Long, variableValues: HashMap<String, String>? = null): Intent {
        val intent = Intent(context, ExecuteActivity::class.java)
        intent.action = ExecuteActivity.ACTION_EXECUTE_SHORTCUT
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION

        if (variableValues != null) {
            intent.putExtra(ExecuteActivity.EXTRA_VARIABLE_VALUES, variableValues)
        }

        val uri = ContentUris.withAppendedId(Uri.fromParts("content", context.packageName, null), shortcutId)
        intent.data = uri
        return intent
    }

    fun getShortcutId(intent: Intent): Long {
        var shortcutId = -1L
        val uri = intent.data
        if (uri != null) {
            try {
                val id = uri.lastPathSegment
                shortcutId = java.lang.Long.parseLong(id)
            } catch (e: NumberFormatException) {
            }
        }
        if (shortcutId == -1L) {
            return intent.getLongExtra(ExecuteActivity.EXTRA_SHORTCUT_ID, -1L) // for backwards compatibility
        }
        return shortcutId
    }

    fun getVariableValues(intent: Intent): Map<String, String> {
        val serializable = intent.getSerializableExtra(ExecuteActivity.EXTRA_VARIABLE_VALUES)
        if (serializable is Map<*, *>) {
            return serializable as Map<String, String>
        }
        return HashMap()
    }

    fun getShortcutPlacementIntent(context: Context, shortcut: Shortcut, install: Boolean): Intent {
        val shortcutIntent = IntentUtil.createIntent(context, shortcut.id)
        val addIntent = Intent()
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.name)
        addIntent.putExtra(EXTRA_SHORTCUT_DUPLICATE, true)
        if (shortcut.iconName != null) {
            val iconUri = shortcut.getIconURI(context)
            try {
                val icon = MediaStore.Images.Media.getBitmap(context.contentResolver, iconUri)
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
            } catch (e: Exception) {
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context.applicationContext, ShortcutUIUtils.DEFAULT_ICON))
            }

        } else {
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context.applicationContext, ShortcutUIUtils.DEFAULT_ICON))
        }

        addIntent.action = if (install) ACTION_INSTALL_SHORTCUT else ACTION_UNINSTALL_SHORTCUT

        return addIntent
    }

}
