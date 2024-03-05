package com.autiscope.ui.gameEntry

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.autiscope.databinding.ActivityGameEntryBinding
import com.autiscope.ui.questions.QuestionBaseActivity

class GameEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameEntryBinding
    private val permissions =
        mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in permissions && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                moveToQuestions()
            }
        }

    private fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }


    private fun initialize() {
        binding.nextButton.setOnClickListener {
            onNextBtnClick()
        }
    }

    private fun onNextBtnClick() {
        requestCameraAudioPermissions()
        binding.nextButton.isEnabled = false

    }

    private fun requestCameraAudioPermissions() {
        when {
            allPermissionsGranted() -> {
                moveToQuestions()
                // You can use the API that requires the permission.
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

    private fun moveToQuestions() {
        QuestionBaseActivity.openIntent(this)

    }

}