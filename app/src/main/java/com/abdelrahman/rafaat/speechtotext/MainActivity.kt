package com.abdelrahman.rafaat.speechtotext

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.util.*


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val TAG = "MainActivity"
    private val SPEECH_TO_TEXT_REQUEST_CODE = 10
    private lateinit var textToSpeechButton: Button
    private lateinit var clearButton: Button
    private lateinit var editText: EditText
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speechToTextImageView: ImageView
    private lateinit var copyTextImageView: ImageView
    private lateinit var shareTextImageView: ImageView

    private lateinit var languagesSpinner: Spinner
    private lateinit var currentLocale: Locale

    private val languages = arrayOf(
        Locale.UK,
        Locale.FRANCE,
        Locale.GERMAN,
        Locale.CHINESE,
        Locale.ITALIAN,
        Locale.JAPANESE,
        Locale.KOREAN,
        Locale.US,
        Locale.forLanguageTag("ar")
    )

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        textToSpeechButton = findViewById(R.id.text_to_speech)
        editText = findViewById(R.id.editText)
        speechToTextImageView = findViewById(R.id.speech_to_text)
        copyTextImageView = findViewById(R.id.copy_text)
        shareTextImageView = findViewById(R.id.share_text)
        languagesSpinner = findViewById(R.id.languages_spinner)
        clearButton = findViewById(R.id.clear_button)



        clearButton.setOnClickListener {
            editText.setText("")
        }

        languagesSpinner.onItemSelectedListener = this
        val adapter = ArrayAdapter(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            languages
        )
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        languagesSpinner.adapter = adapter

        textToSpeechButton.setOnClickListener {
            Log.i(TAG, "onCreate: sayItButton---> $currentLocale")
            textToSpeech = TextToSpeech(this) {
                if (it == TextToSpeech.SUCCESS) {
                    Log.i(TAG, "onCreate: TextToSpeech.SUCCESS")
                    textToSpeech.language = currentLocale
                    textToSpeech.setSpeechRate(1.0f)
                    textToSpeech.speak(editText.text.toString(), TextToSpeech.QUEUE_ADD, null)
                }
            }
        }

        speechToTextImageView.setOnClickListener {
            Log.i(TAG, "onCreate: speechToText---> $currentLocale")

            if (isNetworkAvailable(this)) {
                val intentText = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intentText.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intentText.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    currentLocale
                )

                startActivityForResult(intentText, SPEECH_TO_TEXT_REQUEST_CODE)
            } else {
                val snackBar = Snackbar.make(
                    findViewById(R.id.root_layout),
                    getString(R.string.no_internet),
                    Snackbar.LENGTH_SHORT
                )
                snackBar.setAction(getString(R.string.enable_connection)) {
                    val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                    startActivityForResult(panelIntent, 0)
                }
                snackBar.show()

            }

        }

        copyTextImageView.setOnClickListener {
            copyText()
        }

        shareTextImageView.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, editText.text.toString())
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share To:"))
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SPEECH_TO_TEXT_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val textResult: ArrayList<String> =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                    editText.append(textResult[0])

                }
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Log.i(TAG, "onItemSelected: ")
        currentLocale = languages[p2]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


    private fun isNetworkAvailable(context: Context): Boolean {
        var isConnected = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // For 29 api or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                isConnected = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }

        } else {
            // For below 29 api
            if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting) {
                isConnected = true
            }
        }
        return isConnected
    }

    private fun copyText() {
        if (editText.text.toString().isNotEmpty()) {
            val manger: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text ", editText.text.toString())
            manger.setPrimaryClip(clipData)
            Snackbar.make(
                findViewById(R.id.root_layout),
                getString(R.string.text_copied),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            Snackbar.make(
                findViewById(R.id.root_layout),
                "no text to copy it",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }


}