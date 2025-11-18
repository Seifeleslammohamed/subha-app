package com.example.subha

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var countText: TextView
    private lateinit var btnTap: Button
    private lateinit var btnReset: Button
    private lateinit var btnSetTarget: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var txtTarget: TextView

    private var count = 0
    private var target = 33

    private val PREFS = "subha_prefs"
    private val KEY_COUNT = "count"
    private val KEY_TARGET = "target"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force RTL for Arabic labels (if device supports)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        setContentView(R.layout.activity_main)

        countText = findViewById(R.id.countText)
        btnTap = findViewById(R.id.btnTap)
        btnReset = findViewById(R.id.btnReset)
        btnSetTarget = findViewById(R.id.btnSetTarget)
        progressBar = findViewById(R.id.progressBar)
        txtTarget = findViewById(R.id.txtTarget)

        loadPrefs()
        updateUi()

        btnTap.setOnClickListener {
            increment(1)
        }

        // long press to add 10 (fast)
        btnTap.setOnLongClickListener {
            increment(10)
            true
        }

        btnReset.setOnClickListener {
            confirmReset()
        }

        btnSetTarget.setOnClickListener {
            showSetTargetDialog()
        }
    }

    private fun increment(by: Int) {
        count += by
        if (count < 0) count = 0
        vibrateShort()
        savePrefs()
        updateUi()
        if (count >= target) {
            showTargetReached()
        }
    }

    private fun updateUi() {
        countText.text = count.toString()
        progressBar.max = if (target > 0) target else 1
        progressBar.progress = if (count <= progressBar.max) count else progressBar.max
        txtTarget.text = "?????: $target"
    }

    private fun confirmReset() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("????? ?????")
        builder.setMessage("?? ???? ????? ????? ?????? ??? ????")
        builder.setPositiveButton("???") { _, _ ->
            count = 0
            savePrefs()
            updateUi()
        }
        builder.setNegativeButton("??", null)
        builder.show()
    }

    private fun showSetTargetDialog() {
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "????: 33"

        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        input.layoutParams = params
        container.addView(input)

        val dialog = AlertDialog.Builder(this)
            .setTitle("????? ?????")
            .setView(container)
            .setPositiveButton("???") { _, _ ->
                val text = input.text.toString()
                val newTarget = text.toIntOrNull() ?: target
                if (newTarget > 0) {
                    target = newTarget
                    savePrefs()
                    updateUi()
                } else {
                    Toast.makeText(this, "???? ????? ?????? ???? ?? 0", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("?????", null)
            .create()
        dialog.show()
    }

    private fun showTargetReached() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("?????!")
        builder.setMessage("???? ????? ($target) — ?? ???? ????? ????????")
        builder.setPositiveButton("??? ???????") { _, _ ->
            count = 0
            savePrefs()
            updateUi()
        }
        builder.setNegativeButton("?????", null)
        builder.show()
    }

    private fun savePrefs() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_COUNT, count).putInt(KEY_TARGET, target).apply()
    }

    private fun loadPrefs() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        count = prefs.getInt(KEY_COUNT, 0)
        target = prefs.getInt(KEY_TARGET, 33)
    }

    private fun vibrateShort() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(30)
            }
        } catch (e: Exception) {
            // ignore if vibrator not present
        }
    }
}
