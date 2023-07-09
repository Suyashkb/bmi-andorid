package com.griindset.bmi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.TextView

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        var alpha = AnimationUtils.loadAnimation(this,R.anim.alpha)
        var splashTitle : TextView= findViewById(R.id.splash_title)
        splashTitle.animation = alpha

        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity,MainActivity::class.java))
            finish()
        },4000)


    }
}