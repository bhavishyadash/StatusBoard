package com.example.statusboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.statusboard.ui.theme.StatusBoardTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.statusboard.nav.AppNavGraph
import com.example.statusboard.ui.theme.StatusBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        val testData = hashMapOf(
            "message" to "Hello StatusBoard!",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("debug")
            .add(testData)
            .addOnSuccessListener { documentReference ->
                Log.d("MainActivity", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("MainActivity", "Error adding document", e)
            }



        enableEdgeToEdge()
        setContent {
            StatusBoardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        fun createFirestoreUserIfNeeded() {
            val auth = FirebaseAuth.getInstance()
            val db = Firebase.firestore

            val user = auth.currentUser ?: return
            val docRef = db.collection("users").document(user.uid)

            docRef.get().addOnSuccessListener {
                if (!it.exists()) {
                    val profile = mapOf(
                        "uid" to user.uid,
                        "name" to (user.displayName ?: ""),
                        "email" to user.email,
                        "photoUrl" to user.photoUrl?.toString(),
                        "status" to "FREE",
                        "lastUpdated" to System.currentTimeMillis()
                    )

                    docRef.set(profile)
                }
            }
        }

    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StatusBoardTheme {
        Greeting("Android")
    }
}