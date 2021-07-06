package com.example.newsistemasproyecto

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.newsistemasproyecto.Mapping.User
import com.example.newsistemasproyecto.Mapping.showLongMessage
import com.example.newsistemasproyecto.Mapping.showShortMessage
import com.example.newsistemasproyecto.databinding.ActivityHomeAuthBinding
import com.google.android.gms.common.internal.Preconditions
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
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
            val userName = binding.userNameText.text.toString()
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
                    signIn(userName, email, password)
                }
            }
        }

        binding.buttonRegistrar.setOnClickListener {
            val userName = binding.userNameText.text.toString()
            val email = binding.EmailText.text.toString()
            val password = binding.ContraseniaText.text.toString()
            when {
                userName.isEmpty() -> showLongMessage(
                    this,
                    "El nombre de usuario no puede estar vacío"
                )
                email.isEmpty() -> showLongMessage(
                    this,
                    "El email no puede estar vacio!!"
                )
                password.isEmpty() -> showLongMessage(
                    this,
                    "La contraseña no puede estar vacía!!"
                )
                else -> {
                    register(userName, email, password)
                }
            }
        }
    }

    private fun signIn(userName: String, userEmail: String, userPassword: String) {
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
                    finish()
                    startActivity(intent)
                }
            }
    }

    private fun register(userName: String, userEmail: String, userPassword: String) {
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    database = FirebaseDatabase.getInstance().getReference("Users")
                    val user = User(userName, userEmail, userPassword)
                    database.child(auth.uid!!).setValue(user).addOnSuccessListener {
                        showShortMessage(baseContext, "Registro exitoso")
                    }.addOnFailureListener {
                        showLongMessage(this, "Tu cuenta fue creada pero: " + it.message!!)
                    }
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
//                    auth.signOut()
                    val intent = Intent(this, Home_navigation::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    showShortMessage(baseContext, task.exception?.message!!)
                }
            }
    }
}