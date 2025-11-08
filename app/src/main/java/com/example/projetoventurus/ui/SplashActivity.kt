package com.example.projetoventurus.ui // ◀️ CORRIGIDO

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.projetoventurus.databinding.ActivitySplashBinding // ◀️ CORRIGIDO

//SuppressLint("CustomSplashScreen")
// Suprimimos o aviso padrão, pois estamos criando nossa própria lógica de splash
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_DELAY: Long = 2000 // 2 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usamos um Handler para atrasar a transição
        Handler(Looper.getMainLooper()).postDelayed({
            // Intenção de ir da SplashActivity para a MainActivity
            // A importação da MainActivity não é mais necessária,
            // pois ambas estarão no mesmo pacote 'ui'
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Finaliza a SplashActivity para que o usuário não possa voltar para ela
            finish()
        }, SPLASH_DELAY)
    }
}