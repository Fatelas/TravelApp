package com.example.travelwithus

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.sql.Timestamp
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class UserList(
    val id:String, val firstName:String, val lastName:String, val startDate: com.google.firebase.Timestamp?, val endDate: com.google.firebase.Timestamp?,
    val destination:String){
    override fun toString(): String{
        return "${firstName} ${lastName}, ${destination} "
    }
}

class trips : AppCompatActivity() {
    private var currentUser: String? = null
    val db = Firebase.firestore
    private var DocId: String? = null
    var idCurrentTrip : String? = null
    private var dday : Int? =null
    private var mmonth : Int? =null
    private var yyear : Int? =null

    @SuppressLint("ResourceAsColor", "MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trip)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Trips")
        currentUser = FirebaseAuth.getInstance().currentUser!!.uid

        findViewById<Button>(R.id.idDelete).setOnClickListener {
            val db = Firebase.firestore
            if (idCurrentTrip!= null){
                lifecycleScope.launch{
                    db.collection("trips").document(idCurrentTrip!!).delete().await()
                    showtriplist()
                }
                idCurrentTrip = null
            }
        }

        findViewById<EditText>(R.id.startDate).setOnClickListener{
            findViewById<EditText>(R.id.startDate).text.clear()
            findViewById<EditText>(R.id.enddate).text.clear()
            showScedulePopup(findViewById<EditText>(R.id.idDest).text.toString())
        }

        findViewById<Button>(R.id.idUpdate).setOnClickListener {
            if (idCurrentTrip != null){
                lifecycleScope.launch {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.YEAR, yyear!!)
                    calendar.set(Calendar.MONTH, mmonth!!)
                    calendar.set(Calendar.DAY_OF_MONTH, dday!!)
                    val date = calendar.time
                    calendar.time = Date(date.toString())
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    val reservationDate = calendar.time
                    var dest = findViewById<EditText>(R.id.idDest).text.toString()
                    db.collection("trips").document(idCurrentTrip!!)
                        .update(
                            "startDate",
                            Timestamp.from(date.toInstant()),
                            "endDate",
                            Timestamp.from(reservationDate.toInstant()),
                            "city",
                            dest,
                            "createdAt",
                            Timestamp.from(Instant.now())
                        ).await()
                    showtriplist()
                    findViewById<EditText>(R.id.idDest).text.clear()
                    findViewById<EditText>(R.id.idFirstName).text.clear()
                    findViewById<EditText>(R.id.idLastName).text.clear()
                    findViewById<EditText>(R.id.startDate).text.clear()
                    findViewById<EditText>(R.id.enddate).text.clear()
                }
            }
        }

        lifecycleScope.launch {
            showtriplist()
        }
    }

    @SuppressLint("ResourceType", "CutPasteId")
    suspend fun showtriplist(){
        val queryUser = db.collection("trips").whereEqualTo("idUser", currentUser).get().await()

        var listItems = ArrayList<UserList>()
        queryUser.forEach {
            val id = it.id
            val firstname = it.get("name").toString()
            val lastname = it.get("lastname").toString()
            val ref = it.get("idUser").toString()
            val destination = it.get("city").toString()
            val startDate = it.getTimestamp("startDate")
            val endDate = it.getTimestamp("endDate")
            listItems.add(UserList(id,firstname,lastname, startDate, endDate ,destination))
        }
        val listView = findViewById<ListView>(R.id.id_listviewtrip)
        val adapter= ArrayAdapter(this, android.R.layout.simple_list_item_1,listItems)
        listView.adapter = adapter

        listView.setOnItemClickListener {adapterView, view, position,id ->
            val element = adapterView.getItemAtPosition(position) as UserList
            var firstname = findViewById<EditText>(R.id.idFirstName)
            var lasname = findViewById<EditText>(R.id.idLastName)
            var dest = findViewById<EditText>(R.id.idDest)
            var start = findViewById<EditText>(R.id.startDate)
            var end = findViewById<EditText>(R.id.enddate)
            idCurrentTrip = element.id
            firstname.setText(element.firstName)
            lasname.setText(element.lastName)
            dest.setText(element.destination)
            start.setText(element.startDate!!.toDate().toString())
            end.setText(element.endDate!!.toDate().toString())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

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

        calendarView.setOnDateChangeListener { calendarView, year, month, day ->
            dday = day
            mmonth = month
            yyear = year
        }
        popupView.findViewById<Button>(R.id.submit).setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, yyear!!)
            calendar.set(Calendar.MONTH, mmonth!!)
            calendar.set(Calendar.DAY_OF_MONTH, dday!!)
            val date = calendar.time
            calendar.time = Date(date.toString())
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val reservationDate = calendar.time
            findViewById<EditText>(R.id.startDate).setText(date.toString())
            findViewById<EditText>(R.id.enddate).setText(reservationDate.toString())
            popupWindow.dismiss()
        }
    }
}