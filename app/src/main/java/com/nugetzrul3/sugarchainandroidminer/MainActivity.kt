package com.nugetzrul3.sugarchainandroidminer

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.widget.Toolbar
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.nio.file.Files
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    lateinit var sharedpref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedpref = SharedPref(this)
        if (sharedpref.loadNightModestate() == true) {
            setTheme(R.style.DarkTheme)
        }
        else setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val switch: Switch = findViewById(R.id.darkmode)
        if (sharedpref.loadNightModestate() == true) {
            switch.setChecked(true)
        }
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sharedpref.setNightModeState(true)
                saveConfig()
                restartapp()
                setText()
            }
            else if (!isChecked) {
                sharedpref.setNightModeState(false)
                saveConfig()
                restartapp()
                setText()
            }
        }

        //val buttonstate: Button = findViewById(R.id.button)

        /*if(sharedpref.loadButtonModestate() == true) {
            buttonstate.setText("Start")
        }
        else if (sharedpref.loadButtonModestate() == false) {
            buttonstate.setText("Stop")
        }*/

        val sugartoolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(sugartoolbar)

        val arrayspinner = arrayOf<String>("--ALGORITHM--", "yespower", "yespowersugar", "yespoweriso", "yespowernull", "yespowerlitb", "yespoweriots", "yespoweritc", "yespowermbc")
        val spinner: Spinner = findViewById(R.id.spinner)
        val adapter = ArrayAdapter<String>(this, R.layout.spinner_item, arrayspinner)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(adapter)

        changeButtonText()
        setText()
        clearLog()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()) {
            R.id.mygithub -> {
                var parse1 = Uri.parse("https://github.com/Nugetzrul3")
                startActivity(Intent(Intent.ACTION_VIEW, parse1))
                return true
                }
            R.id.website -> {
                var parse2 = Uri.parse("https://sugarchain.org")
                startActivity(Intent(Intent.ACTION_VIEW, parse2))
                return true
            }
            R.id.Sugargithub -> {
                var parse3 = Uri.parse("https://github.com/sugarchain-project")
                startActivity(Intent(Intent.ACTION_VIEW, parse3))
                return true
            }
            R.id.Donate -> {
                var parse4 = Uri.parse("https://sugarchain-blockbook.ilmango.work/address/sugar1qtl7u435t4jly2hdaa7hrcv5qkpvwa0spd9zzc7")
                startActivity(Intent(Intent.ACTION_VIEW, parse4))
            }
            R.id.settings -> {
                val intent = Intent(this, SettingsPage::class.java)
                startActivity(intent)
            }
            R.id.stats -> {
                val walletaddress: EditText = findViewById(R.id.editText2)
                val intent = Intent(this, MiningStats::class.java)
                intent.putExtra("walletaddress", walletaddress.getText().toString())
                startActivity(intent)
            }
            }


        return false
    }

    private var doublebackpressedonce = false

    override fun onBackPressed() {
        if (doublebackpressedonce) {
            super.onBackPressed()
            return
        }
        this.doublebackpressedonce = true
        Toast.makeText(this, "Click back again to Exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doublebackpressedonce = false }, 1500)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(!hasFocus)
        saveConfig()
    }

    private fun changeButtonText() {
        val start_button: Button = findViewById(R.id.button)
        if(sharedpref.loadButtonModestate() == true) {
            start_button.setText("Start")
        }
        else if (sharedpref.loadButtonModestate() == false) {
            start_button.setText("Stop")
}
        start_button.setOnClickListener {
            val changeTextView: TextView = findViewById(R.id.textView6)
            val spinner: Spinner = findViewById(R.id.spinner)

            fun stoporstart() {
            if (start_button.text == "Start") {
                start_button.setText("Stop")
                /*repeat(100) {
                    changeTextView.append("\nThe process has started")
                }*/
                var spinnerItem = spinner.selectedItem.toString()
                changeTextView.setText(spinnerItem)
                sharedpref.setButtonModeState(false)
            }
            else if (start_button.text == "Stop") {
                start_button.setText("Start")
                changeTextView.setText("\nThe Process has stopped")
                sharedpref.setButtonModeState(true)
            }
            }
            stoporstart()

        }
    }


    fun saveConfig() {

        var Pooltxt = findViewById(R.id.editText) as EditText
        var Usertxt = findViewById(R.id.editText2) as EditText
        var Passwdtxt = findViewById(R.id.editText3) as EditText
        var thrdstxt = findViewById(R.id.editText5) as EditText
        var algorithm = findViewById(R.id.spinner) as Spinner
        var algorithmtext = algorithm.selectedItemPosition



        var Settings = JSONObject()
        Settings.put("URL", Pooltxt.text)
        Settings.put("User", Usertxt.text)
        Settings.put("Passwd", Passwdtxt.text)
        Settings.put("CPU", thrdstxt.text)
        Settings.put("Algorithm", algorithmtext)




        var context = applicationContext.filesDir.path
        var file = File(context + "config.json")
        if(!file.exists()){
            file.createNewFile()
            file.writeText(Settings.toString())
        }
        else if(file.exists()){
            file.writeText(Settings.toString())
        }

    }

    fun setText() {
        var json: String

        val serverset: EditText = findViewById(R.id.editText)
        val userrset: EditText = findViewById(R.id.editText2)
        val passwdset: EditText = findViewById(R.id.editText3)
        val thrdsset: EditText = findViewById(R.id.editText5)
        val spinnerset: Spinner = findViewById(R.id.spinner)

        try {

            var context = applicationContext.filesDir.path
            val path = context + "config.json"

            val inputStream: InputStream =
                FileInputStream("$path")
            json = inputStream.bufferedReader().use { it.readText() }


            var jsobobj = JSONObject(json)

            for (i in 0..jsobobj.length() - 1) {
                var server = jsobobj.get("URL")
                var username = jsobobj.get("User")
                var password = jsobobj.get("Passwd")
                var threads = jsobobj.get("CPU")
                //var algo = jsobobj.get("Algorithm").toString()

                serverset.setText(server.toString())
                userrset.setText(username.toString())
                passwdset.setText(password.toString())
                thrdsset.setText(threads.toString())
                //spinnerset.setSelection(algo.toInt())

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    fun clearLog() {
        val logclear: TextView = findViewById(R.id.textView6)
        val clear_button: Button = findViewById(R.id.button3)

        clear_button.setOnClickListener{
            logclear.setText("")
        }
    }

    fun restartapp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


}








