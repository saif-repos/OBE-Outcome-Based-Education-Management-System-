package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.obe_mngt_sys.MODELS.LoginResponse
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.editTextText)
        passwordEditText = findViewById(R.id.editTextTextPassword)
        loginButton = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                progressBar.visibility = ProgressBar.VISIBLE
                performLogin(username, password)
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        val apiService = RetrofitInstance.apiService

        apiService.login(username, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progressBar.visibility = ProgressBar.INVISIBLE

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse.Message == "Login successful.") {
                        Toast.makeText(
                            this@Login,
                            "Login Successful, Role: ${loginResponse.Role}",
                            Toast.LENGTH_SHORT
                        ).show()

                        when (loginResponse.Role) {
                            "HOD" -> {
                                val intent = Intent(this@Login, HOD_DASHBOARD::class.java)
                                intent.putExtra(
                                    "UserName",
                                    loginResponse.UserName
                                ) // Pass UserName instead of UserID
                                intent.putExtra("UserID", loginResponse.UserID)
                                intent.putExtra("UserName", loginResponse.UserName)

                                startActivity(intent)
                                finish()
                            }

                            "Teacher" -> {
                                val intent = Intent(this@Login, TEACHER_DASHBOARD::class.java)
                                intent.putExtra(
                                    "UserName",
                                    loginResponse.UserName
                                ) // Pass the User ID
                                intent.putExtra("UserID", loginResponse.UserID)
                                startActivity(intent)
                                finish()
                            }
                            "Student" -> {
                                val intent = Intent(this@Login, PLOBASEDRESULT_OVERALL::class.java)
                                intent.putExtra(
                                    "UserName",
                                    loginResponse.UserName
                                ) // Pass the User ID
                                intent.putExtra("UserID", loginResponse.UserID)
                                startActivity(intent)
                                finish()
                            }

                            else -> {
                                Toast.makeText(
                                    this@Login,
                                    "Invalid role: ${loginResponse.Role}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@Login,
                            "Invalid username or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@Login, "Login failed. Try again.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                progressBar.visibility = ProgressBar.INVISIBLE
                Toast.makeText(this@Login, "An error occurred: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}
