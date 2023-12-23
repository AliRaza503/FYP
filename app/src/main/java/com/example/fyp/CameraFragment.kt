package com.example.fyp

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.fyp.databinding.FragmentCameraBinding
import java.io.File
import java.lang.NumberFormatException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraX Fragment to handle image capture.
 */
class CameraFragment : Fragment() {

    companion object {
        private const val TAG = "CameraXFragment"
        const val FOLDER_IMAGES = "Pictures/Mobile-Images"
    }

    private var _binding: FragmentCameraBinding? = null
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var viewModel: CameraViewModel
    private lateinit var preview: Preview
    private lateinit var imageName: String
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        )
        { permission ->
            if (!permission) {
                Toast.makeText(
                    requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        if (permissionGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Set up the listeners for take photo and video capture buttons
        binding.imageCaptureButton.setOnClickListener {
            it.isEnabled = false
            takePhoto()
        }

        // Set up the view model
        viewModel = ViewModelProvider(requireActivity())[CameraViewModel::class.java]


        // Click listener to show a sample image
        binding.promptImage.setOnClickListener {
            // Navigate to Sample Image Fragment to show the sample image passing in an integer 5
            val action = CameraFragmentDirections.actionCameraFragmentToSampleImageFragment(viewModel.getDrawableIndex())
            findNavController().navigate(action)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        return binding.root
    }

    private fun setViews() {
        binding.promptImage.setImageResource(viewModel.getDrawableName())
    }

    override fun onResume() {
        super.onResume()
        // Get all the file names in the directory Pictures/Mobile-Images
        val directoryPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/Mobile-Images"
        val directory = File(directoryPath)
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                viewModel.newImageAdded(file.nameWithoutExtension)
            }
        }
        setViews()
    }

    private fun takePhoto() {
        imageName = viewModel.getImageName()
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, FOLDER_IMAGES)
            } else {
                val dir = Environment.getExternalStorageDirectory()
                val folder = File(dir, FOLDER_IMAGES)
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                put(
                    MediaStore.Images.Media.DATA,
                    "${folder.absolutePath}${File.separator}$imageName.jpg"
                )
            }
        }
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    binding.imageCaptureButton.isEnabled = true
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    viewModel.newImageAdded(imageName)
                    binding.promptImage.setImageResource(viewModel.getDrawableName())
                    Log.d(TAG, msg)
                    binding.imageCaptureButton.isEnabled = true
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Build the image capture use case and attach button click listener
                imageCapture = ImageCapture.Builder().build()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun permissionGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}