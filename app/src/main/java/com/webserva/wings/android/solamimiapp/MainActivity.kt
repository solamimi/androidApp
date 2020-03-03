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
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    // SpeechRecognizer property
    private val RECORD_REQUEST_CODE = 101

    private lateinit var mSpeechRecognizer : SpeechRecognizer

    private lateinit var mSpeechRecognizerIntent: Intent

    private var listItems = ArrayList<String>()

    // private var FROM = arrayOf("word")

    // private var TO = intArrayOf(R.id.idContent)

    private var wordMap = mutableMapOf(
        "date" to "", "user" to "", "type" to "", "text" to "",
        "value1" to "", "value2" to "", "value3" to "", "value4" to ""
    )

    private var regexWord = "\\D+".toRegex()

    private var regexNumber = "\\d+".toRegex()

    private var isPushedKeyCode24 = false
    private var isPushedKeyCode66 = false

    private var spinnerItems = arrayOf("山田 花子", "田中 太郎", "佐藤 よしこ", "木村 義信")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var spinner_user: Spinner = findViewById(R.id.spinner_user)

        // ArrayAdapter
        val adapter = ArrayAdapter(applicationContext,
            android.R.layout.simple_spinner_item, spinnerItems)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // spinner に adapter をセット
        // Kotlin Android Extensions
        spinner_user.adapter = adapter
        /**
         * voiceButton clicked
         */
        val toggle: ToggleButton = findViewById(R.id.voiceButton)
        toggle.setOnCheckedChangeListener { _, isChecked ->
            execSpeechRecognizer(isChecked)
        }

    }


    fun execSpeechRecognizer(flag: Boolean) {
        if(flag) {
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i("keydown", keyCode.toString())
        if ( keyCode == 24 ) {
            return true
        } else if ( keyCode == 66 ) {
            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i("keyup", keyCode.toString())
        return if ( keyCode == 24 ) {
            var toggle: ToggleButton = findViewById(R.id.voiceButton)
            var flag: Boolean = toggle.isChecked

            if ( flag == true ) {
                flag = false
            } else {
                flag = true
            }
            execSpeechRecognizer(flag = flag)
            toggle.isChecked = flag
            true
        } else if ( keyCode == 66 ) {
            var toggle: ToggleButton = findViewById(R.id.voiceButton)
            var flag: Boolean = toggle.isChecked
            if ( flag == true ) {
                flag = false
                execSpeechRecognizer(flag = flag)
                toggle.isChecked = flag
            }

            // Send to GCP
            sendData()
            true
        } else {
            super.onKeyUp(keyCode, event)
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i("keylongpress", keyCode.toString())
        return super.onKeyLongPress(keyCode, event)
    }

    override protected fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent);
        //TODO: android.intent.action.VOICE_COMMAND で起動された
    }

    fun onClickSend(view: View) {
        sendData()

    }

    fun onClickClear(view: View) {
        listItems.clear()

        val lvSpeech = findViewById<EditText>(R.id.speechText)
        lvSpeech.setText("")
    }

    fun sendData()
    {
        val speechText = findViewById<EditText>(R.id.speechText)

        // val jsonObject = JSONObject()
        // val listened = JSONObject()
        // val listened2 = JSONObject()
        // val listened3 = JSONObject()

        var speechList = ArrayList<String>()
        for(i in 0..4) {
            speechList.add("")
        }

        var j = 1
        var text = ""
        Log.i("test","L94")
        for (word in speechText.text.toString().lines()) {
            Log.i("test",word)
            if(regexWord.containsMatchIn(word)) {
                text += word + " "
                Log.i("test","L99")
            }

            if(regexNumber.containsMatchIn(word)) {
                speechList[j] = word
                j++
                Log.i("test","L105")
            }

        }
        speechList[0] = text.trimEnd()

        // 一行目をコマンドとして認識させる
        var  array = speechList[0].split(" ")
        var recognize_text = ""
        if ( array.count() > 1 ) {
            for(  i in 1..array.count()-1 ) {
                recognize_text += array[i]
                recognize_text += " "
            }
        }

        Log.i("test", speechList[0])

        var spinner_user: Spinner = findViewById(R.id.spinner_user)
        var select_user:Int = spinner_user.selectedItemPosition


        wordMap["date"] = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date())
        wordMap["user"] = (select_user+1).toString()
        wordMap["type"] = array[0]
        wordMap["text"] = recognize_text
        wordMap["value1"] = speechList[1]
        wordMap["value2"] = speechList[2]
        wordMap["value3"] = speechList[3]
        wordMap["value4"] = speechList[4]

        Log.i("test","L125")

        val sbParams = StringBuilder()
        var i = 0

        wordMap.forEach {
            try {
                if (i != 0) {
                    sbParams.append("&")
                }
                sbParams.append(it.key).append("=").append(URLEncoder.encode(it.value, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            Log.i("Iteration",i.toString())
            i++
        }

        try {
            GlobalScope.launch {
                // httpPost("https://solamimi.herokuapp.com", jsonObject)
                // スプシへポスト
                httpPost("https://script.google.com/macros/s/AKfycbyfCAUWNE7y0z0sKbltcIZ91yAykA2t0Ilx1Q1fNqvpaDKAE8o/exec", sbParams.toString())
                Log.i("httpPost", sbParams.toString())
            }
        } catch (ex: Exception) {

        }

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

    private suspend fun httpPost(myUrl: String, myQueryString: String): String {
        val result = withContext(Dispatchers.IO) {
            val url = URL(myUrl)
            // Create HttpURLConnection
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Accept-Charset", "UTF-8")

            // Add Query content to POST request body
            setPostRequestContent(conn, myQueryString)

            // Make POST request to the given URL
            conn.connect()

            // Return response message
            conn.responseMessage + ""

        }
        return result
    }

    private fun setPostRequestContent(conn: HttpURLConnection, queryString: String) {
        val os = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(queryString)
        Log.i("setPostRequestContent", queryString)
        writer.flush()
        writer.close()
        os.close()
    }

}
