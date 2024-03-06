package com.autiscope.ui.questions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.autiscope.databinding.ActivityGameFinishBinding

class FinishGameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameFinishBinding

    companion object {
        val TAG = FinishGameActivity::class.java.toString()

        fun openIntent(context: Context) {
            val intent = Intent(context, FinishGameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameFinishBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}