package com.example.travelwithus.ui.home

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.travelwithus.R
import com.example.travelwithus.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private var currentUser: String? = null
    private val binding get() = _binding!!
    private var place: String? = null
    private var dday : Int? = null
    private var mmonth : Int? = null
    private var yyear : Int? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var n =  context
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.cardDestination1.setOnClickListener{
            place = binding.textDestination1.text.toString()
            showScedulePopup(place!!)
        }

        binding.cardDestination2.setOnClickListener{
            place = binding.textDestination2.text.toString()
            showScedulePopup(place!!)
        }

        binding.cardDestination3.setOnClickListener{
            place = binding.textDestination3.text.toString()
            showScedulePopup(place!!)
        }

        binding.cardDestination4.setOnClickListener{
            place = binding.textDestination4.text.toString()
            showScedulePopup(place!!)
        }

        binding.cardDestination5.setOnClickListener{
            place = binding.textDestination5.text.toString()
            showScedulePopup(place!!)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    fun showScedulePopup(title: String) {
        val inflater = this.layoutInflater

        val popupView = inflater.inflate(R.layout.popup_schedule, null)
        val calendarView = popupView.findViewById<CalendarView>(R.id.calendar_view_trip)

        // set the title of the popup
        val popupTitle = popupView.findViewById<TextView>(R.id.card_title)
        popupTitle.text = title

        // create the popup window
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        calendarView.setOnDateChangeListener(){ calendarView, year, month, day ->
            dday = day
            mmonth = month
            yyear = year

        }
        popupView.findViewById<Button>(R.id.submit).setOnClickListener {
            Log.e("dday", dday.toString())
            if (dday != null && mmonth != null && yyear != null) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, yyear!!)
                calendar.set(Calendar.MONTH, mmonth!!)
                calendar.set(Calendar.DAY_OF_MONTH, dday!!)
                val date = calendar.time
                calendar.time = Date(date.toString())
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                val reservationDate = calendar.time
                lifecycleScope.launch {
                    currentUser = FirebaseAuth.getInstance().currentUser!!.uid
                    val db = Firebase.firestore
                    val usersRef =
                        Firebase.firestore.collection("users").whereEqualTo("id", currentUser)
                            .get().await()

                    val name = usersRef.documents[0].get("firstName").toString()
                    val lastname = usersRef.documents[0].get("lastName").toString()
                    val trip = hashMapOf(
                        "idUser" to currentUser,
                        "city" to title,
                        "name" to name,
                        "lastname" to lastname,
                        "startDate" to Timestamp.from(date.toInstant()),
                        "endDate" to Timestamp.from(reservationDate.toInstant()),
                        "createdAt" to Timestamp.from(Instant.now())
                    )
                    val newPost = Firebase.firestore.collection("trips").add(trip)
                    newPost.addOnCompleteListener {
                        Toast.makeText(requireContext(), "Trip Added!", Toast.LENGTH_LONG).show()
                        popupWindow.dismiss()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "You need to select a date", Toast.LENGTH_LONG).show()
            }
        }
    }
}