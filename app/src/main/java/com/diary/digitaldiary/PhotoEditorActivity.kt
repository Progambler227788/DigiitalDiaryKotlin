package com.diary.digitaldiary

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.diary.digitaldiary.databinding.ActivityPhotoEditorBinding
import com.google.android.material.snackbar.Snackbar
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.SaveFileResult
import ja.burhanrashid52.photoeditor.ViewType
import kotlinx.coroutines.launch
import java.io.File


class PhotoEditorActivity : AppCompatActivity() {

    private lateinit var photoEditor: PhotoEditor
    private lateinit var binding: ActivityPhotoEditorBinding
    private var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra("imageUri")
        filePath = imageUri?: ""


        // Setup the photo editor
        photoEditor = PhotoEditor.Builder(this, binding.photoEditorView)
            .setPinchTextScalable(true)
            .build()

        binding.photoEditorView.source.setImageURI(Uri.parse(imageUri))


        val colorCode = Color.RED
        binding.addText.setOnClickListener {
            Toast.makeText(this,binding.dataEnter.text.toString(),Toast.LENGTH_SHORT).show()
            photoEditor.addText(binding.dataEnter.text.toString(), colorCode)
        }


        // Set listener for editing text
        photoEditor.setOnPhotoEditorListener(object : OnPhotoEditorListener {


            override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {

            }

            override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {

                    // Edit the text
                    photoEditor.editText(rootView, text, Color.RED)
            }

            override fun onRemoveViewListener(viewType: ViewType, numberOfAddedViews: Int) {

            }

            override fun onStartViewChangeListener(viewType: ViewType) {

            }

            override fun onStopViewChangeListener(viewType: ViewType) {

            }

            override fun onTouchSourceImage(event: MotionEvent) {

            }
        })
        binding.saveImage.setOnClickListener {
            if(filePath!="")
              saveImage()
        }
    }

    private fun saveImage() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
        } else {
            // Permission is granted, proceed with saving the image
            saveImageToFile()
        }
    }

    private fun saveImageToFile() {
        // Create a new directory in external storage (e.g., Pictures/DigitalDiary)
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DigitalDiary")

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Generate a unique file name for the edited image
        val fileName = "Edited_${System.currentTimeMillis()}.jpg"

        // Create a new file in the directory with the generated file name
        val file = File(directory, fileName)

        // Save the image to the new file path
        lifecycleScope.launch {
            try {
                val result = photoEditor.saveAsFile(file.absolutePath)
                if (result is SaveFileResult.Success) {
                    // Image saved successfully
                    showSnackbar("Image saved to ${file.absolutePath}")
                    // Pass the saved image path back to the AddDiary activity
                    val returnIntent = Intent().apply {
                        putExtra("SAVED_IMAGE_PATH", file.absolutePath)
                    }
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish() // Finish the PhotoEditorActivity
                } else {
                    // Error occurred while saving image
                    showSnackbar("Couldn't save image")
                }
            } catch (e: SecurityException) {
                // Handle SecurityException here, show a message to the user
                e.printStackTrace()
                showSnackbar("Permission denied. Unable to save image.")
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, save the image
                saveImageToFile()
            } else {
                // Permission denied, show a message to the user
                showSnackbar("Permission denied. Unable to save image.")
            }
        }
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 4

    }
}

