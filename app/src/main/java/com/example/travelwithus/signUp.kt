package com.example.travelwithus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.travelwithus.databinding.ActivitySignUpBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class signUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    var mfirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, logIn::class.java)
            startActivity(intent)
        }
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()
            val name = binding.nameEt.text.toString()
            val lastname = binding.lastnameEt.text.toString()
            val born = binding.anoEt2.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty() && name.isNotEmpty() && lastname.isNotEmpty() && born.isNotEmpty() ) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            lifecycleScope.launch{
                                addUser()
                            }
                        } else {
                            val show =
                                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }
    }

    suspend fun addUser(){
        val email = binding.emailEt.text.toString()
        val name = binding.nameEt.text.toString()
        val lastname = binding.lastnameEt.text.toString()
        val born = binding.anoEt2.text.toString()
        val anoCurr = Calendar.getInstance().get(Calendar.YEAR).toString()
        val idade =  anoCurr.toInt() - born.toInt()
        val current = Timestamp.now()
        val db = Firebase.firestore
        val user = hashMapOf(
            "id" to mfirebaseAuth.currentUser!!.uid,
            "photoReference" to 1,
            "firstName" to name,
            "lastName" to lastname,
            "email" to email,
            "idade" to idade,
            "DocId" to 1,
            "registerDate" to current,
            "updatedAt" to current
        )
        val newUser = db.collection("users").add(user)
            .addOnSuccessListener {documentRefence ->
                val update = db.collection("users").document(documentRefence.id).update("DocId",documentRefence.id)
                    .addOnCompleteListener{
                        val intent2 = Intent(this, logIn::class.java)
                        startActivity(intent2)
                        Toast.makeText(applicationContext,"User ADDED, please Login",Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener{ documentRefence ->
                Toast.makeText(applicationContext,"User NOT ADDED, try again",Toast.LENGTH_LONG).show()
            }.await()
    }
}