package com.example.coroutine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private val JOB_TIMEOUT = 1500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            CoroutineScope(IO).launch {
                apiRequest1()
            }
        }

        button1.setOnClickListener {
            CoroutineScope(IO).launch {
                apiRequest2()
            }
        }

        button2.setOnClickListener {
            CoroutineScope(IO).launch {
                apiRequest3()
            }
        }

        button3.setOnClickListener {
            CoroutineScope(IO).launch {
                apiRequest4()
            }
        }
    }

    private suspend fun apiRequest1() {

        val result1 = getResult1FromApi() // wait until job is done
        setTextOnMainThread("Got $result1")

        val result2 = getResult2FromApi() // wait until job is done
        setTextOnMainThread("Got $result2")
    }

    private suspend fun apiRequest2() {
        withContext(IO) {
            val executionTime = measureTimeMillis {
                val job1 = launch{
                    val result1 = getResult1FromApi()
                    setTextOnMainThread("Got $result1")
                }
                job1.join()

                val result2 = async {
                    getResult2FromApi()
                }.await()
                setTextOnMainThread("Got $result2")
            }
            setTextOnMainThread("Total Time ${executionTime} ms")
        }
    }

    private suspend fun apiRequest3() {
        withContext(IO) {

            val job1 = launch {
                val time1 = measureTimeMillis {
                    val result1 = getResult1FromApi()
                    setTextOnMainThread("Got $result1")
                }
                println("debug: compeleted job1 in $time1 ms.")
            }

            val job2 = launch {
                val time2 = measureTimeMillis {
                    val result2 = getResult2FromApi()
                    setTextOnMainThread("Got $result2")
                }
                setTextOnMainThread("Total Time $time2 ms.")
            }

        }
    }

    private suspend fun apiRequest4() {
        withContext(IO) {

            val job = withTimeoutOrNull(JOB_TIMEOUT) {

                val result1 = getResult1FromApi() // wait until job is done
                setTextOnMainThread("Got $result1")

                val result2 = getResult2FromApi() // wait until job is done
                setTextOnMainThread("Got $result2")

            } // waiting for job to complete...

            if(job == null){
                setTextOnMainThread("Cancelling job... took longer than $JOB_TIMEOUT ms")
            }

        }
    }

    private suspend fun getResult1FromApi(): String {
        delay(1000) // Does not block thread. Just suspends the coroutine inside the thread
        return "Result #1"
    }

    private suspend fun getResult2FromApi(): String {
        delay(1000)
        return "Result #2"
    }

    private fun setNewText(input: String){
        val newText = textView.text.toString() + "\n$input"
        textView.text = newText
    }

    private suspend fun setTextOnMainThread(input: String) {
        withContext (Main) {
            setNewText(input)
        }
    }
}
