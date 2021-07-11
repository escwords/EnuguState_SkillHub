package com.words.storageapp.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.words.storageapp.R
import com.words.storageapp.ui.main.MainActivity
import kotlinx.coroutines.delay

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launchWhenCreated {
            delay(500)
            startActivity(moveToMainActivity())
            finish()
        }
    }

    private fun moveToMainActivity(): Intent {
        return Intent(this, MainActivity::class.java)
    }
}