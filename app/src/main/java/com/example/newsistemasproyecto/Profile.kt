package com.example.newsistemasproyecto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import com.example.newsistemasproyecto.Mapping.showShortMessage
import com.example.newsistemasproyecto.databinding.ActivityHomeNavigationBinding
import com.example.newsistemasproyecto.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Profile : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReferenceLocation: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
//    private lateinit var email: String
    var userFromEmail = ""
    var handler: Handler = Handler()
    private val TIEMPO: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setContentView(R.layout.activity_profile)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        databaseReference = database.reference.child("Users")
        getDatabaseData()
    }

    private fun getDatabaseData() {
        val user = auth.currentUser!!.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.child(user).get().addOnSuccessListener {
            findViewById<TextView>(R.id.emailTextInfo).text = "Email: "+it.child("email").value.toString()
            findViewById<TextView>(R.id.nombreText).text = "Usuario: " + it.child("userName").value.toString()
//            getUserFromEmail(it.child("email").value.toString())
//            showShortMessage(baseContext, email)

//            userName = it.child("userName").value.toString()
        }
//        databaseReferenceLocation = FirebaseDatabase.getInstance().getReference("Location")
//        databaseReferenceLocation.child(user).get().addOnSuccessListener{
//            findViewById<TextView>(R.id.nombreText).text = "Usuario:" + it.child(userFromEmail).child("nombre").value.toString()
//        }
    }

    private fun getUserFromEmail(email: String) {
        showShortMessage(baseContext,email)
        var str = ""
        for (i in email.toCharArray()) {
            if (i != '@' && i != '.' && i != '#' && i != '$' && i != '[' && i != ']') {
                str += i
            } else if (i == '@') {
                break
            }
        }
        userFromEmail = str
    }
}