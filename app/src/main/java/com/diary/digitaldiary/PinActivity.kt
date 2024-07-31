package com.diary.digitaldiary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.diary.digitaldiary.databinding.ActivityPinBinding

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("DiaryAppPrefs", Context.MODE_PRIVATE)

        // Check if PIN is already set
        val isPinSet = sharedPreferences.contains("userPin")

        if(isPinSet){
            binding.loginSignupButton.text = "Login"
            binding.welcomeTextView.text ="Welcome again, please enter your login pin"
        }

        binding.loginSignupButton.setOnClickListener {
            val enteredPin = binding.pinEditText1.text.toString() +
                    binding.pinEditText2.text.toString() +
                    binding.pinEditText3.text.toString() +
                    binding.pinEditText4.text.toString()

            if (enteredPin.length == 4) {
                if (isPinSet) {
                    val savedPin = sharedPreferences.getString("userPin", "")

                    if (enteredPin == savedPin) {
                        // PIN matches, proceed to the next activity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Incorrect PIN
                        showToast("Incorrect PIN")
                    }
                } else {
                    // Save the new PIN
                    with(sharedPreferences.edit()) {
                        putString("userPin", enteredPin)
                        apply()
                    }
                    showToast("PIN set successfully")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                showToast("Please enter a 4-digit PIN")
            }
        }

        // Add TextWatcher to move focus to the next EditText
        setupPinEditTexts()

    }
    private fun setupPinEditTexts() {
        binding.pinEditText1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) binding.pinEditText2.requestFocus()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.pinEditText2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) binding.pinEditText3.requestFocus()
                else if (s.isNullOrEmpty()) binding.pinEditText1.requestFocus()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.pinEditText3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) binding.pinEditText4.requestFocus()
                else if (s.isNullOrEmpty()) binding.pinEditText2.requestFocus()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.pinEditText4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) binding.pinEditText3.requestFocus()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}