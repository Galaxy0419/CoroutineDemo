package com.demo.coroutinedemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import com.demo.coroutinedemo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val titleStringBuilder = StringBuilder()
    private lateinit var viewBinding: ActivityMainBinding

    companion object {
        private const val TITLE_STRING = "Kotlin Coroutine Demo"
        private const val COROUTINE_THREAD_TAG = "CoroutineThreadLogTag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    override fun onResume() {
        super.onResume()

        viewBinding.titleTextView.text = ""
        titleStringBuilder.setLength(0)

        lifecycle.coroutineScope.launch {
            for (i in TITLE_STRING.indices) {
                viewBinding.titleTextView.text = titleStringBuilder.append(TITLE_STRING[i]).toString()
                delay(125L)
            }
        }
    }

    fun onStartButtonPressed(view: View) {
        Log.d(COROUTINE_THREAD_TAG, "Outside Scope Thread ID: ${Thread.currentThread().id}")

        lifecycle.coroutineScope.launch {
            Log.d(COROUTINE_THREAD_TAG, "Top Level Scope Thread ID: ${Thread.currentThread().id}")

            launch(Dispatchers.Default) {
                launch {
                    Log.d(COROUTINE_THREAD_TAG, "Task 1 Thread ID: ${Thread.currentThread().id}")
                    for (i in 1..2) {
                        Thread.sleep(1000L)
                        withContext(Dispatchers.Main) {
                            viewBinding.task1ProgressBar.progress = i * 50
                        }
                    }
                }

                launch {
                    Log.d(COROUTINE_THREAD_TAG, "Task 2 Thread ID: ${Thread.currentThread().id}")
                    for (i in 1..4) {
                        Thread.sleep(1000L)
                        withContext(Dispatchers.Main) {
                            viewBinding.task2ProgressBar.progress = i * 25
                        }
                    }
                }
            }.invokeOnCompletion {
                launch(Dispatchers.Main) {
                    Log.d(COROUTINE_THREAD_TAG, "Completion Thread ID: ${Thread.currentThread().id}")
                    viewBinding.startButton.isEnabled = true
                    viewBinding.resetButton.isEnabled = true
                    viewBinding.statusTextView.text = "All tasks have been finished."
                }
            }
        }

        viewBinding.statusTextView.text = "All tasks have been started."
        viewBinding.startButton.isEnabled = false
        viewBinding.resetButton.isEnabled = false
    }

    fun onResetButtonPressed(view: View) {
        viewBinding.task1ProgressBar.progress = 0
        viewBinding.task2ProgressBar.progress = 0
        viewBinding.statusTextView.text = "Press \"Start\" to start the tasks."
    }
}
