package com.example.newsistemasproyecto

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.newsistemasproyecto.Mapping.User
import com.example.newsistemasproyecto.Mapping.showLongMessage
import com.example.newsistemasproyecto.Mapping.showShortMessage
import com.example.newsistemasproyecto.databinding.ActivityHomeAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_auth)
        binding = ActivityHomeAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        binding.buttonAcceder.setOnClickListener {
            val email = binding.EmailText.text.toString()
            val password = binding.ContraseniaText.text.toString()
            //Animacion de carga
//            binding.progressBarLogin.visibility = View.VISIBLE
            when {
                email.isEmpty() || password.isEmpty() -> {
                    showShortMessage(this, "La autenticación falló!!")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    signIn(email, password)
                }
            }
        }

        binding.buttonRegistrar.setOnClickListener {
            val email = binding.EmailText.text.toString()
            val password = binding.ContraseniaText.text.toString()
            when {
                email.isEmpty() -> showLongMessage(
                    this,
                    "El email no puede estar vacio!!"
                )
                password.isEmpty() -> showLongMessage(
                    this,
                    "La contraseña no puede estar vacía!!"
                )
                else -> {
                    register(email, password)
                }
            }
        }
    }

    private fun signIn(userEmail: String, userPassword: String) {
        auth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    showShortMessage(baseContext, "Inicio Sesion correctamente!")
                    val intent = Intent(this, Home_navigation::class.java)
                    finishAffinity()
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                    showShortMessage(baseContext, "La autenticación falló!!")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                }
            }
    }

    private fun register(userEmail: String, userPassword: String) {
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    database = FirebaseDatabase.getInstance().getReference("Users")
                    val user = User(userEmail, userPassword)
                    database.child(auth.uid!!).setValue(user).addOnSuccessListener {
                        showShortMessage(baseContext, "Registro exitoso")
                    }.addOnFailureListener {
                        showLongMessage(this, "Tu cuenta fue creada pero: " + it.message!!)
                    }
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
//                    auth.signOut()
                    val intent = Intent(this, Home_navigation::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                } else {
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    showShortMessage(baseContext, task.exception?.message!!)
                }
            }
    }
}