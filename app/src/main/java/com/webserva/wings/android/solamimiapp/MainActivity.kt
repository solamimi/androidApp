package com.webserva.wings.android.solamimiapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    // SpeechRecognizer property
    private val RECORD_REQUEST_CODE = 101

    private lateinit var mSpeechRecognizer : SpeechRecognizer

    private lateinit var mSpeechRecognizerIntent: Intent

    // private var listItems = mutableListOf<MutableMap<String, String>>()

    var listItems = ArrayList<String>()

    // private var FROM = arrayOf("word")

    // private var TO = intArrayOf(R.id.idContent)

    private var wordList = listOf<String>()

    // private var regexWord = Regex(pattern = "")

    // private var regexNumber = Regex(pattern = "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * voiceButton clicked
         */
        val toggle: ToggleButton = findViewById(R.id.voiceButton)
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                initializeSpeechRecognizer()
                Log.i("voice", "L49")
            } else {
                try {
                    GlobalScope.launch {
                        runOnUiThread {
                            mSpeechRecognizer.cancel()
                        }
                        delay(1000)
                        runOnUiThread {
                            mSpeechRecognizer.destroy()
                        }
                        Log.i("voice", "L60")
                    }
                } catch (ex: Exception) {

                }
            }
        }

    }

    fun onClickSend(view: View) {
        val speechText = findViewById<EditText>(R.id.speechText)
        val jsonObject = JSONObject()
        val listened = JSONObject()
        // val listened2 = JSONObject()
        // val listened3 = JSONObject()

        wordList = speechText.text.toString().lines()

        wordList.forEach {
            Log.i("send",it + "\n")
        }

        // 登録するパラメータを作成
        // Build Json Format
        listened.accumulate(
            "listen_at",
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date())
        )

        listened.accumulate("action_type", "訪問介護")
        listened.accumulate("listen_words", "訪問介護 脈拍 血圧")
        listened.accumulate("user", 1)
        listened.accumulate("value1", 999)
        listened.accumulate("value1", 60)
        listened.accumulate("value2", 70)
        listened.accumulate("value3", 80)
        listened.accumulate("value4", 90)
        // listened.accumulate("created_at", SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date()))
        // listened.accumulate("created_by", 3)
        // listened.accumulate("updated_at", SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date()))
        // listened.accumulate("updated_by", 3)


        /*
        listened.accumulate(
            "created_at",
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date())
        )
        listened.accumulate("listen_at", 1)
        listened.accumulate("created_by", "訪問介護")
        listened.accumulate("updated_at", "訪問介護 脈拍 血圧")
        listened.accumulate("updated_by", 60)
        listened.accumulate("user", 120)
        listened.accumulate("listen_words", "ああああ")
        listened.accumulate("value", "いいいい")
        listened.accumulate("I", SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date()))
        listened.accumulate("J", 3)
        listened.accumulate("K", SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date()))
        listened.accumulate("L", 3)

         */

        jsonObject.accumulate("listened", listened)

        Log.i("json", jsonObject.toString())


        try {
            GlobalScope.launch {
                // httpPost("https://solamimi.herokuapp.com", jsonObject)
                // スプシへポスト
                httpPost("https://script.google.com/macros/s/AKfycbyfCAUWNE7y0z0sKbltcIZ91yAykA2t0Ilx1Q1fNqvpaDKAE8o/exec", jsonObject)
                Log.i("httpPost", jsonObject.toString())
            }
        } catch (ex: Exception) {

        }

        listItems.clear()

        val lvSpeech = findViewById<EditText>(R.id.speechText)
        lvSpeech.setText("")


    }

    fun onClickClear(view: View) {
        listItems.clear()

        val lvSpeech = findViewById<EditText>(R.id.speechText)
        lvSpeech.setText("")
    }

    private fun initializeSpeechRecognizer() {


        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName)
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().language)
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        // Log.i("voice", "L122")

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext) // applicationContext → this ??

        mSpeechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                Log.i("voice", "onReadyForSpeech")
            }

            override fun onRmsChanged(p0: Float) {
                Log.i("voice", "onRmsChanged")
            }

            override fun onBufferReceived(p0: ByteArray?) {
                Log.i("voice", "onBufferReceived")
            }

            override fun onBeginningOfSpeech() {
                Log.i("voice", "onBeginningOfSpeech")
            }

            override fun onEndOfSpeech() {
                Log.i("voice", "onEndOfSpeech")
            }

            override fun onError(p0: Int) {

                try {
                    GlobalScope.launch {
                        runOnUiThread {
                            mSpeechRecognizer.cancel()
                        }
                        delay(1000)
                        runOnUiThread {
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent)
                        }
                    }
                } catch (ex: Exception) {

                }
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                Log.i("voice", "onEvent")
            }

            override fun onPartialResults(p0: Bundle?) {

            }


            override fun onResults(results: Bundle?) {
                Log.i("voice", "L185")
                val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                // var resultMap = mutableMapOf("word" to result.toString().removeSurrounding("[","]"))


                listItems.add(result.toString().removeSurrounding("[","]"))

                if (listItems.count() > 20) {
                    listItems.removeAt(0)
                }

                Log.i("voice", "L204")

                var text = ""
                listItems.forEach {
                    text += it + "\n"
                }
                try {
                    GlobalScope.launch {
                        // The action is posted to the event queue of the UI thread
                        runOnUiThread {
                            val speechText = findViewById<EditText>(R.id.speechText)
                            speechText.setText(text)
                        }
                        runOnUiThread {
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent)
                        }
                    }
                } catch (ex: Exception) {
                    /* Exception happen */
                }


                /*
                try {
                    Log.i("voice", "L203")
                    GlobalScope.launch {
                        Log.i("voice", "L205")
                        // The action is posted to the event queue of the UI thread
                        runOnUiThread {
                            Log.i("voice", "L201")
                            // val lvSpeech = findViewById<ListView>(R.id.speechText)
                            // val adapter = SimpleAdapter(applicationContext, listItems, R.layout.item, FROM, TO)
                            // lvSpeech.adapter = adapter
                            val lvSpeech = findViewById<ListView>(R.id.speechText)
                            customAdapter = EditAdapter(applicationContext, listItems)
                            lvSpeech!!.adapter = customAdapter
                        }
                        runOnUiThread {
                            Log.i("voice", "L210")
                            mSpeechRecognizer.startListening(mSpeechRecognizerIntent)
                        }
                    }
                } catch (ex: Exception) {
                    /* Exception happen */
                }
                 */

            }


        })

        if(ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_REQUEST_CODE)
        }
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent)
        Log.i("voice", "Start listening")

    }


    /**
     * Http communication
     */

    private suspend fun httpPost(myUrl: String, jsonObject: JSONObject): String {
        val result = withContext(Dispatchers.IO) {
            val url = URL(myUrl)
            // Create HttpURLConnection
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            // Add JSON content to POST request body
            setPostRequestContent(conn, jsonObject)

            // Make POST request to the given URL
            conn.connect()

            // Return response message
            conn.responseMessage + ""

        }
        return result
    }

    private fun setPostRequestContent(conn: HttpURLConnection, jsonObject: JSONObject) {
        val os = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(jsonObject.toString())
        Log.i("setPostRequestContent", jsonObject.toString())
        writer.flush()
        writer.close()
        os.close()
    }

}
