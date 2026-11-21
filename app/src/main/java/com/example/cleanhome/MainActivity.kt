package com.example.cleanhome

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class AppItem(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    val launchIntent: Intent
)

class MainActivity : AppCompatActivity() {

    private lateinit var grid: RecyclerView
    private lateinit var adapter: AppAdapter

    // Apps listed first, in this order. Play Store is always the first tile.
    private val pinned = listOf(
        "com.android.vending",
        "com.google.android.youtube.tv",
        "com.netflix.ninja",
        "com.amazon.amazonvideo.livingroom",
        "com.google.android.apps.tv.launcherx"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        grid = findViewById(R.id.grid)
        grid.layoutManager = GridLayoutManager(this, 5)
        adapter = AppAdapter(
            onClick = { startActivity(it.launchIntent) },
            onLongClick = { openAppInfo(it) }
        )
        grid.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.submit(loadApps())
        grid.post { grid.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus() }
    }

    private fun loadApps(): List<AppItem> {
        val pm = packageManager
        val seen = HashSet<String>()
        val result = ArrayList<AppItem>()

        fun collect(category: String) {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
            val infos: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
            for (info in infos) {
                val pkg = info.activityInfo.packageName
                if (pkg == packageName || !seen.add(pkg)) continue
                val launch = pm.getLeanbackLaunchIntentForPackage(pkg)
                    ?: pm.getLaunchIntentForPackage(pkg)
                    ?: continue
                result.add(
                    AppItem(
                        label = info.loadLabel(pm).toString(),
                        packageName = pkg,
                        icon = info.activityInfo.loadBanner(pm)
                            ?: info.loadIcon(pm),
                        launchIntent = launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                )
            }
        }

        collect(Intent.CATEGORY_LEANBACK_LAUNCHER)
        collect(Intent.CATEGORY_LAUNCHER)

        return result.sortedWith(
            compareBy<AppItem> {
                val idx = pinned.indexOf(it.packageName)
                if (idx == -1) Int.MAX_VALUE else idx
            }.thenBy { it.label.lowercase() }
        )
    }

    // Long-press OK (or press MENU) on a tile to reach Uninstall / App info.
    private fun openAppInfo(item: AppItem) {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:${item.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            val focused = grid.focusedChild
            val pos = if (focused != null) grid.getChildAdapterPosition(focused) else -1
            adapter.itemAt(pos)?.let { openAppInfo(it); return true }
        }
        return super.onKeyDown(keyCode, event)
    }

    // Home screen should not close on Back.
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        grid.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
    }
}
