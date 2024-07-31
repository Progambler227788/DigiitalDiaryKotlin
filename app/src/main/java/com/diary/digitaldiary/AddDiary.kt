package com.diary.digitaldiary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.diary.digitaldiary.database.DiaryDatabaseHelper
import com.diary.digitaldiary.databinding.ActivityAddDiaryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.Locale
import ja.burhanrashid52.photoeditor.PhotoEditor

class AddDiary : AppCompatActivity() {

    private lateinit var binding : ActivityAddDiaryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private lateinit var diaryDatabaseHelper: DiaryDatabaseHelper
    private var currentPhotoPath: String? = null
    private var currentVoicePath: String? = null
    private val PHOTO_EDITOR_REQUEST_CODE = 2 // Define a request code for PhotoEditor Activity
    private var diaryEntry : Long = 0

    private lateinit var photoEditor: PhotoEditor





    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_PERMISSION_STORAGE = 2
        private const val REQUEST_PERMISSION_RECORD_AUDIO = 3
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val YOUR_PHOTO_EDITOR_REQUEST_CODE = 1002

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        diaryDatabaseHelper = DiaryDatabaseHelper(this)

        photoEditor = PhotoEditor.Builder(this, binding.imageViewPhoto).setPinchTextScalable(true).build()

        // Check if the activity is in "update" mode
        val isUpdate = intent.getBooleanExtra("Update", false)
        if (isUpdate) {
            // Extract the diary entry ID from the intent
            diaryEntry  = intent.getLongExtra("DIARY_ENTRY_ID", -1)
            if (diaryEntry != -1L) {
                // Load the existing diary entry from the database using the ID
                val diaryEntry = diaryDatabaseHelper.getDiaryEntry(diaryEntry)
                if (diaryEntry != null) {
                    // Populate the UI with the existing diary entry data
                    binding.editTextTitle.setText(diaryEntry.title)
                    binding.editTextNote.setText(diaryEntry.note)
                    binding.textViewLocation.setText(diaryEntry.location)
                    // Load and display the existing photo
                    if (!diaryEntry.photoPath.isNullOrEmpty()) {
                        val bitmap = BitmapFactory.decodeFile(diaryEntry.photoPath)
                        binding.imageViewPhoto.source.setImageBitmap(bitmap)
                    }
                    // Update the current photo path
                    // Update the current photo path
                    currentPhotoPath = diaryEntry.photoPath
                 // Update the current voice path
                    currentVoicePath = diaryEntry.voicePath

                    audioFilePath = currentVoicePath

                    binding.textViewVoiceRecording.text = "Recording saved: $audioFilePath"


                    Log.d("Voice",currentVoicePath.toString())

                }
            }
            binding.buttonSaveEntry.text = "UPDATE ENTRY"
        }


//        // Check and request location permission if needed
//        if (checkLocationPermission()) {
//            Log.d("Location called"," in if")
//            startLocationUpdates()
//        }

        if (checkLocationPermission()) {
            getLastKnownLocation { latitude, longitude ->
                fetchTownName(latitude, longitude) { townName ->
                    if (townName != null) {
                        Toast.makeText(this, "Town: $townName", Toast.LENGTH_LONG).show()
                        binding.textViewLocation.text = "Location: $townName"
                    } else {
                        Toast.makeText(this, "Unable to fetch town name", Toast.LENGTH_LONG).show()
                        binding.textViewLocation.text = "Location: Not Found"
                    }
                }
            }
        } else {
            requestLocationPermission()
        }

        binding.buttonAddPhoto.setOnClickListener {
            if (checkStoragePermission()) {
                pickImageFromGallery()
            }
        }
        binding.buttonPlayVoice.setOnClickListener{
            playRecording()
        }

        binding.buttonRecordVoice.setOnClickListener {
            if (checkAudioPermission()) {
                if (mediaRecorder == null) {
                    startRecording()
                } else {
                    stopRecording()
                }
            }
        }
        binding.imageViewPhoto.setOnClickListener {
            val intent = Intent(this, PhotoEditorActivity::class.java)
            Log.d("Current Photo Path",currentVoicePath.toString())
            intent.putExtra("imageUri", currentPhotoPath)
            startActivityForResult(intent, YOUR_PHOTO_EDITOR_REQUEST_CODE )
        }

        binding.buttonSaveEntry.setOnClickListener {
            if (binding.buttonSaveEntry.text.toString() == "UPDATE ENTRY") {
                updateDiaryEntry()
                updateBack()

            }
            else {
                saveDiaryEntry()
            }

        }
    }
    private fun updateDiaryEntry(){
        diaryDatabaseHelper.updateDiaryEntry(diaryEntry ,binding.editTextTitle.text.toString(),binding.textViewLocation.text.toString(),binding.editTextNote.text.toString(),
            currentPhotoPath,currentVoicePath)
    }


    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun getLastKnownLocation(callback: (Double, Double) -> Unit) {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    callback(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fetchTownName(latitude: Double, longitude: Double, callback: (String?) -> Unit) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val townName = addresses[0].locality
                    callback(townName)
                } else {
                    callback(null)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            callback(null)
        }
    }

    private fun checkStoragePermission(): Boolean {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION_STORAGE)
            return false
        }
        return true
    }

    private fun checkAudioPermission(): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION_RECORD_AUDIO)
            return false
        }
        return true
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
//    private fun launchPhotoEditor(imageUri: Uri) {
//        val intent = PhotoEditorIntentBuilder(this)
//            .setImageSource(imageUri)
//            .build()
//        startActivityForResult(intent, PHOTO_EDITOR_REQUEST_CODE)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            if (selectedImageUri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                binding.imageViewPhoto.source.setImageBitmap(bitmap)

                // Get the real path of the image
                currentPhotoPath = getRealPathFromUri(selectedImageUri)
            }
        }
        else if (requestCode == YOUR_PHOTO_EDITOR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Check if a photo was saved and needs to be replaced
            val savedImagePath = data?.getStringExtra("SAVED_IMAGE_PATH")
            currentPhotoPath = savedImagePath
            savedImagePath?.let { imagePath ->
                // Load and display the saved image
                val bitmap = BitmapFactory.decodeFile(imagePath)
                binding.imageViewPhoto.source.setImageBitmap(bitmap)
            }
        }
    }

    // Convert URI to file path
    private fun getRealPathFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val path = cursor?.getString(columnIndex ?: 0)
        cursor?.close()
        return path ?: ""
    }


    private fun startRecording() {
        try {
            audioFilePath = "${externalCacheDir?.absolutePath}/audiorecord.3gp"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            binding.buttonRecordVoice.text = "Stop Recording"
            binding.textViewVoiceRecording.text = "Recording..."
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        binding.buttonRecordVoice.text = "Record Voice"
        binding.textViewVoiceRecording.text = "Recording saved: $audioFilePath"
        currentVoicePath = audioFilePath
    }
    private fun playRecording() {
        if (audioFilePath!=null) {
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(audioFilePath)
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            mediaPlayer?.setOnCompletionListener {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                }
            }
            REQUEST_PERMISSION_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording()
                }
            }
        }
    }


    fun updateBack(){
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }





    private fun saveDiaryEntry() {
        // Save the diary entry to SQLite database
        // Retrieve data from editTextNote, textViewLocation, currentPhotoPath, and currentVoicePath

       val newRowId =  diaryDatabaseHelper.insertDiaryEntry(binding.editTextTitle.text.toString(),binding.textViewLocation.text.toString(),binding.editTextNote.text.toString(),
            currentPhotoPath,currentVoicePath)

        if (newRowId != -1L) {
            Log.d("Adding voice",currentVoicePath.toString())
            // Show toast message indicating successful insertion
            Toast.makeText(this, "Diary entry added", Toast.LENGTH_SHORT).show()
            // After saving the diary entry in AddDiary activity
            updateBack()


        } else {
            // Handle error if insertion failed
            Toast.makeText(this, "Failed to add diary entry", Toast.LENGTH_SHORT).show()
        }
    }


}
