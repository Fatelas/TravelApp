package com.example.travelwithus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class forgotPassword : AppCompatActivity() {
    var firebaseAuth = FirebaseAuth.getInstance()
    private var currentUser: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        findViewById<Button>(R.id.bt_signup).setOnClickListener {
            val intent4 = Intent(this, logIn::class.java)
            startActivity(intent4)
        }
        findViewById<Button>(R.id.bt_forget).setOnClickListener {
            val email: String = findViewById<TextView>(R.id.et_email).text.toString().trim{ it <= ' ' }
            if (email.isEmpty()){
                Toast.makeText(
                    this,
                    "EMAIL IS EMPTY",
                    Toast.LENGTH_LONG).show()
            }
            else{
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        Toast.makeText(
                            this,
                            "EMAIL SEND SUCCESSFULlY TO RESET PASS",
                            Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else   {
                        Toast.makeText(
                            this,
                            task.exception!!.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}