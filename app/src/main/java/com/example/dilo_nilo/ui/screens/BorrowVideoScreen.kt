package com.example.dilo_nilo.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.dilo_nilo.ui.navigation.Routes
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.viewmodel.LoanViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowVideoScreen(
    loanId: String,
    loanViewModel: LoanViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val loanState by loanViewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isRecording by remember { mutableStateOf(false) }
    var recordingComplete by remember { mutableStateOf(false) }
    var spokenText by remember { mutableStateOf("") }
    var timer by remember { mutableStateOf(15) }
    var videoRecording: Recording? by remember { mutableStateOf(null) }
    var videoCapture: VideoCapture<Recorder>? by remember { mutableStateOf(null) }
    var speechRecognizer: SpeechRecognizer? by remember { mutableStateOf(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    LaunchedEffect(loanId) { loanViewModel.loadLoan(loanId) }

    val loan = loanState.currentLoan

    // Script the borrower should say
    val script = loan?.let {
        "I am borrowing $${it.amount} for ${it.termMonths} months. I will repay in full. Otherwise, you can take action against me."
    } ?: "I acknowledge this loan agreement."

    // Timer countdown while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            timer = 15
            while (timer > 0 && isRecording) {
                delay(1000L)
                timer--
            }
            if (isRecording) {
                isRecording = false
                videoRecording?.stop()
                recordingComplete = true
                speechRecognizer?.stopListening()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Verification", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Script card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Say this aloud:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(script, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, lineHeight = 22.sp)
                }
            }

            // Camera preview
            if (hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = androidx.camera.core.Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val recorder = Recorder.Builder()
                                    .setQualitySelector(QualitySelector.from(Quality.HD))
                                    .build()
                                videoCapture = VideoCapture.withOutput(recorder)
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_FRONT_CAMERA,
                                        preview,
                                        videoCapture
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Red, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("REC ${timer}s", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (recordingComplete) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recording complete!", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Recognized speech preview
                if (spokenText.isNotBlank()) {
                    Text(
                        "Recognized: $spokenText",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!recordingComplete) {
                        Button(
                            onClick = {
                                if (!isRecording) {
                                    // Start recording
                                    val file = File(context.cacheDir, "loan_video_$loanId.mp4")
                                    val outputOptions = FileOutputOptions.Builder(file).build()
                                    videoRecording = videoCapture?.output?.prepareRecording(context, outputOptions)
                                        ?.withAudioEnabled()
                                        ?.start(ContextCompat.getMainExecutor(context)) { event ->
                                            if (event is VideoRecordEvent.Finalize) {
                                                if (!event.hasError()) {
                                                    val bytes = file.readBytes()
                                                    loanViewModel.verifyVideo(loanId, bytes)
                                                }
                                            }
                                        }
                                    // Start speech recognition
                                    if (SpeechRecognizer.isRecognitionAvailable(context)) {
                                        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                                            setRecognitionListener(object : RecognitionListener {
                                                override fun onResults(results: Bundle?) {
                                                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                                    spokenText = matches?.firstOrNull() ?: ""
                                                }
                                                override fun onPartialResults(partial: Bundle?) {
                                                    val matches = partial?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                                    spokenText = matches?.firstOrNull() ?: ""
                                                }
                                                override fun onReadyForSpeech(p: Bundle?) {}
                                                override fun onBeginningOfSpeech() {}
                                                override fun onRmsChanged(rmsdB: Float) {}
                                                override fun onBufferReceived(buffer: ByteArray?) {}
                                                override fun onEndOfSpeech() {}
                                                override fun onError(error: Int) {}
                                                override fun onEvent(eventType: Int, params: Bundle?) {}
                                            })
                                            startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                            })
                                        }
                                    }
                                    isRecording = true
                                } else {
                                    isRecording = false
                                    videoRecording?.stop()
                                    recordingComplete = true
                                    speechRecognizer?.stopListening()
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) Color.Red else Primary
                            )
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRecording) "Stop (${timer}s)" else "Start Recording")
                        }
                    }

                    if (recordingComplete) {
                        Button(
                            onClick = { navController.navigate(Routes.chat(loanId)) },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !loanState.isLoading
                        ) {
                            Text("Submit & Continue", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Text(
                    "Camera and microphone permission required.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}
