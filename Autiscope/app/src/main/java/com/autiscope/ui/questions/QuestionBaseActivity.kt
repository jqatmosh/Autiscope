package com.autiscope.ui.questions

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.autiscope.R
import com.autiscope.databinding.ActivityQuestionBinding
import com.autiscope.model.Question
import com.autiscope.model.QuestionType
import com.autiscope.ui.differences.FindDifferencesFragment
import com.autiscope.ui.imageQuestion.ImageQuestionFragment
import com.autiscope.ui.matchGame.MatchingGameFragment
import com.autiscope.ui.similarites.FindSimilaritiesImagesFragment
import com.autiscope.ui.video.VideoFragment
import com.autiscope.util.BitmapUtils
import com.autiscope.util.GlobalKeys
import com.google.android.material.snackbar.Snackbar
import dev.bmcreations.scrcast.ScrCast
import dev.bmcreations.scrcast.config.ChannelConfig
import dev.bmcreations.scrcast.config.Options
import dev.bmcreations.scrcast.config.StorageConfig
import dev.bmcreations.scrcast.config.VideoConfig
import dev.bmcreations.scrcast.internal.config.dsl.NotificationConfigBuilder
import dev.bmcreations.scrcast.recorder.RecordingCallbacks
import dev.bmcreations.scrcast.recorder.RecordingState
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QuestionBaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private var questionsArray: List<Question> = emptyList()
    private var lastQuestion = 0

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var enumerationDeferred: Deferred<Unit>? = null

    data class CameraCapability(val camSelector: CameraSelector, val qualities: List<Quality>)

    private val cameraCapabilities = mutableListOf<CameraCapability>()

    private var recorder: ScrCast? = null


    companion object {
        val TAG = QuestionBaseActivity::class.java.toString()
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        fun openIntent(context: Context) {
            val intent = Intent(context, QuestionBaseActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    private fun initialize() {
        initEnumerationDeferred()
        lastQuestion = 0
        questionsArray = Question.getQuestions()
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        setupRecorder()
        startScreenRecord()
        this.lifecycleScope.launch {
            if (enumerationDeferred != null) {
                enumerationDeferred!!.await()
                enumerationDeferred = null
                startCamera()
            }
        }

    }


    private fun initEnumerationDeferred() {
        enumerationDeferred = lifecycleScope.async {
            whenCreated {
                val provider = ProcessCameraProvider.getInstance(this@QuestionBaseActivity).await()

                provider.unbindAll()
                for (camSelector in arrayOf(
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    CameraSelector.DEFAULT_FRONT_CAMERA
                )) {
                    try {
                        if (provider.hasCamera(camSelector)) {
                            val camera =
                                provider.bindToLifecycle(this@QuestionBaseActivity, camSelector)
                            QualitySelector
                                .getSupportedQualities(camera.cameraInfo)
                                .filter { quality ->
                                    listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD)
                                        .contains(quality)
                                }.also {
                                    cameraCapabilities.add(CameraCapability(camSelector, it))
                                }
                        }
                    } catch (exc: java.lang.Exception) {
                        Log.e(TAG, "Camera Face $camSelector is not supported")
                    }
                }
            }
        }
    }

    private fun setupRecorder() {
        recorder = ScrCast.use(this)

        // create configuration for video
        val videoConfig = VideoConfig(
            -1,
            -1,
            MediaRecorder.VideoEncoder.H264,
            8000000,
            360
        )

        // create configuration for storage
        val storageConfig = StorageConfig("autiscope-sample")

        // create configuration for notification channel for recording
        val channelConfig = ChannelConfig("1337", "Recording Service")

        // create configuration for our notification
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_camera)
        val notificationConfig = NotificationConfigBuilder()
        notificationConfig.showPause = true
        notificationConfig.icon = BitmapUtils.drawableToBitmap(icon!!)
        notificationConfig.showStop = true
        notificationConfig.showTimer = true
        notificationConfig.channel = channelConfig
        val options = Options(
            videoConfig,
            storageConfig,
            notificationConfig.build(),
            false,
            0,
            true
        )

        // set our options
        recorder?.updateOptions(options)

        // listen for state changes
        recorder?.setRecordingCallback(object : RecordingCallbacks {
            override fun onStateChange(state: RecordingState) {
                if (state == RecordingState.Recording || state is RecordingState.Idle) {
                    val isRecording = state == RecordingState.Recording
                    if (isRecording) {
                        this@QuestionBaseActivity.lifecycleScope.launch {
                            captureVideo()
                        }
                        bindQuestionView()
                    }
                }
            }

            override fun onRecordingFinished(file: File) {
                Toast.makeText(
                    this@QuestionBaseActivity,
                    "result file is located at " + file.absolutePath,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    private fun startScreenRecord() {
        recorder?.record()
        recorder?.onRecordingComplete { file ->
            Snackbar.make(
                binding.root,
                "Recording located at ${file.absolutePath}",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }


    private fun stopScreenRecord() {
        recorder?.stopRecording()
    }

    private suspend fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(this).await()

        binding.viewFinder.visibility = View.VISIBLE

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        try {
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                videoCapture,
                preview
            )

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }


    private fun stopRecording() {
        val currentRecording = recording
        if (currentRecording != null) {
            currentRecording.stop()
            recording = null
            cameraExecutor.shutdown()
            binding.viewFinder.visibility = View.GONE
        }

    }


    // Implements VideoCapture use case, including start and stop capturing.
    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        // viewBinding.videoCaptureButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }


        val name = "CameraX-recording-" +
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutput)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        this@QuestionBaseActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(mainThreadExecutor, captureListener)
        Log.i(TAG, "Recording started")


    }

    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(this) }

    private val captureListener = Consumer<VideoRecordEvent> { event ->
        if (event is VideoRecordEvent.Finalize) {
            Log.d(TAG, "VideoRecordEvent.Finalize")
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecordings()

    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "Videos").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun stopRecordings() {
        stopRecording()
        stopScreenRecord()
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) != null) {
            supportFragmentManager.findFragmentById(R.id.fragment_container)?.let {
                supportFragmentManager.beginTransaction().remove(it).commit()
            }
        }


    }

    private fun bindQuestionView() {
        if (lastQuestion >= questionsArray.size) {
            stopRecordings()
            FinishGameActivity.openIntent(this)
            finish()
            return
        }
        val question = questionsArray[lastQuestion]
        lastQuestion += 1
        var timerInSeconds = GlobalKeys.TIMER_IN_SECONDS
        when (question.type) {
            QuestionType.VIDEO ->
                replaceFragment(
                    VideoFragment.newInstance(question),
                    VideoFragment.TAG,
                    false
                )

            QuestionType.MATCHING -> {
                replaceFragment(
                    MatchingGameFragment.newInstance(question),
                    MatchingGameFragment.TAG,
                    false
                )

            }

            QuestionType.IMAGE_AUDIO -> {
                replaceFragment(
                    ImageQuestionFragment.newInstance(question),
                    ImageQuestionFragment.TAG,
                    false
                )
            }

            QuestionType.FIND_SIMILARITIES -> {
                replaceFragment(
                    FindSimilaritiesImagesFragment.newInstance(question),
                    FindSimilaritiesImagesFragment.TAG,
                    false
                )
                timerInSeconds = GlobalKeys.TIMER_IN_SECONDS_FIND_SIMILARITIES
            }

            QuestionType.FIND_DIFFERENCES -> {
                replaceFragment(
                    FindDifferencesFragment.newInstance(question),
                    FindDifferencesFragment.TAG,
                    false
                )
                timerInSeconds = GlobalKeys.TIMER_IN_SECONDS_FIND_DIFFERENCES
            }

            QuestionType.HEDGEHOG -> {
                replaceFragment(
                    VideoFragment.newInstance(question),
                    VideoFragment.TAG,
                    false
                )
                timerInSeconds = GlobalKeys.TIMER_IN_SECONDS_HEDGEHOG
            }

            else -> replaceFragment(
                VideoFragment.newInstance(question),
                VideoFragment.TAG,
                false
            )
        }
        startTimer(timerInSeconds)
    }

    private fun startTimer(timeLeftInSeconds: Int) {
        updateTimerText(timeLeftInSeconds * 1000L)
        object : CountDownTimer((timeLeftInSeconds + 1) * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished)
            }

            override fun onFinish() {
                onTimeEnded()
                cancel()
                binding.timerText.visibility = View.GONE
            }
        }.also {
            it.start()
        }
    }

    private fun updateTimerText(millisUntilFinished: Long) {
        val secondsUntilFinished = millisUntilFinished / 1000
        val seconds = secondsUntilFinished % 60
        val minutes = (secondsUntilFinished - seconds) / 60
        if (binding.timerText.visibility != View.VISIBLE) {
            binding.timerText.visibility = View.VISIBLE
        }
        binding.timerText.text = getString(
            R.string.timer_format,
            "$minutes".padStart(2, '0'),
            "$seconds".padStart(2, '0')
        )
    }

    fun onTimeEnded() {
        moveToNextQuestion()
    }


    private fun moveToNextQuestion() {
        bindQuestionView()
    }


    private fun replaceFragment(target: Fragment, tagName: String, addToBackStack: Boolean) {
        if (!isFinishing) {
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, target, tagName)
            if (addToBackStack)
                transaction.addToBackStack(null)

            if (fragmentManager.isStateSaved) {
                transaction.commitAllowingStateLoss()
            } else {
                transaction.commit()
            }
        }
    }

}