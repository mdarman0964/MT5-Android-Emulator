package com.mdarman.mt5emulator

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val installerUrl =
        "https://download.mql5.com/cdn/web/metaquotes.software.corp/mt5/mt5setup.exe"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFF10141D.toInt()
        window.navigationBarColor = 0xFF10141D.toInt()
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        renderUi()
    }

    private fun renderUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
            setBackgroundColor(0xFF10141D.toInt())
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        scroll.addView(content)
        root.addView(scroll, ViewGroup.LayoutParams(-1, -1))

        content.addView(label("MT5 ANDROID EMULATOR", 13, 0xFF6EE7B7.toInt(), true))
        content.addView(label("Windows MT5 on your phone", 27, 0xFFFFFFFF.toInt(), true, dp(10)))
        content.addView(label(
            "A separate project for your Galaxy M53 5G. Runtime integration is still in development.",
            15, 0xFFB7C0CF.toInt(), false, dp(8)
        ))

        content.addView(sectionTitle("METATrader 5", dp(28)))
        content.addView(actionButton("Download MT5 Windows installer") { downloadInstaller() })
        content.addView(actionButton("Open official MT5 download page") {
            openUrl("https://www.metatrader.com/en/download/desktop")
        })
        content.addView(label(
            "The installer is saved to Downloads. This build cannot run .exe files yet because Wine/Box64 is not integrated.",
            13, 0xFFFFD28A.toInt(), false, dp(8)
        ))

        content.addView(sectionTitle("Background mode", dp(24)))
        content.addView(actionButton("Enable keep-awake service") {
            val intent = Intent(this, KeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
            Toast.makeText(this, "Keep-awake service requested", Toast.LENGTH_SHORT).show()
        })
        content.addView(actionButton("Stop keep-awake service") {
            stopService(Intent(this, KeepAliveService::class.java))
            Toast.makeText(this, "Keep-awake service stopped", Toast.LENGTH_SHORT).show()
        })
        content.addView(label(
            "This service reduces sleep interruptions but does not guarantee that Android or Samsung battery management will never stop the app. It does not run MT5 yet.",
            13, 0xFFB7C0CF.toInt(), false, dp(8)
        ))

        content.addView(sectionTitle("Runtime components", dp(24)))
        content.addView(label(
            "Planned: Wine + Box64, ARM64 root filesystem, graphics/input bridge, a virtual C: drive, and optional Wine Gecko/Mono downloads. These are not installed in this first build.",
            14, 0xFFB7C0CF.toInt(), false, dp(8)
        ))
        content.addView(actionButton("Project README / setup notes") {
            openUrl("https://github.com/mdarman0964/MT5-Android-Emulator")
        })
        setContentView(root)
    }

    private fun downloadInstaller() {
        try {
            val request = DownloadManager.Request(Uri.parse(installerUrl))
                .setTitle("MetaTrader 5 Windows installer")
                .setDescription("Downloading the official MT5 web installer")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "mt5setup.exe")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(this, "Download started. Check Downloads/notifications.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download could not start: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(this, "No app available to open this link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sectionTitle(text: String, top: Int) =
        label(text, 18, 0xFFFFFFFF.toInt(), true, top)

    private fun label(text: String, size: Int, color: Int, bold: Boolean, top: Int = 0): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, top, 0, dp(10))
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }

    private fun actionButton(text: String, action: () -> Unit) =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 15f
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(8)
            }
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
