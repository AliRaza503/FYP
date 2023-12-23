package com.example.fyp

import android.app.Activity.RESULT_OK
import android.app.RecoverableSecurityException
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fyp.adapters.MobileImagesAdapter
import com.example.fyp.databinding.FragmentMainBinding
import com.example.fyp.utils.Permissions.Companion.REQUIRED_PERMISSIONS
import com.example.fyp.utils.sdk29AndUp
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainViewModel
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var allPermissionsGranted = true
            permissions.entries.forEach {
                // Camera Permission can be granted later on
                if (it.key != android.Manifest.permission.CAMERA) {
                    if (!it.value) {
                        allPermissionsGranted = false
                    }
                }
            }
            if (allPermissionsGranted) {
                Toast.makeText(
                    requireContext(),
                    "Permission request granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
                // Exit the app if the user denied permissions
                activity?.finish()
            }
        }
    private val imageURIs: ArrayList<Uri> = arrayListOf()
    private lateinit var imagesAdapter: MobileImagesAdapter


    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        binding.fabAddImages.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_CameraFragment)
        }

        if (!allPermissionsGranted()) {
            requestPermissions()
        }

        intentSenderLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode != RESULT_OK) {
                // Permission denied
                Toast.makeText(
                    requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.capturedImagesRv.layoutManager = GridLayoutManager(requireContext(), 2)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        binding.guidingText.visibility = View.VISIBLE

        imagesAdapter = MobileImagesAdapter(
            cardClicked = { holder, photo ->
                lifecycleScope.launch {
                    cardClicked(holder, photo)
                }
            },
            viewModel = viewModel,
            imageAdded = {
                binding.guidingText.visibility = View.GONE
            }
        )

        binding.capturedImagesRv.adapter = imagesAdapter

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        imageURIs.clear()
    }

    override fun onResume() {
        super.onResume()
        // Assuming the images are in the Pictures/Mobile-Images directory
        loadImages()
    }

    private fun loadImages() {
        lifecycleScope.launch {
            val photos = loadImagesIntoRecyclerView()
            imagesAdapter.submitList(photos)
        }
    }

    private suspend fun deleteSelectedImages() {
        withContext(Dispatchers.IO) {
            for (image in viewModel.selectedImages) {
                try {
                    requireContext().contentResolver.delete(image.contentUri, null, null)
                } catch (e: SecurityException) {
                    val intentSender = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            MediaStore.createDeleteRequest(
                                requireContext().contentResolver,
                                listOf(image.contentUri)
                            ).intentSender
                        }

                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                            val recoverableSecurityException = e as? RecoverableSecurityException
                            recoverableSecurityException?.userAction?.actionIntent?.intentSender
                        }
                        else -> null
                    }
                    intentSender?.let { sender ->
                        intentSenderLauncher.launch(
                            IntentSenderRequest.Builder(sender).build()
                        )
                    }
                }
            }
            viewModel.selectedImages.clear()
        }
    }

    private suspend fun loadImagesIntoRecyclerView(): List<Photo> {
        return withContext(Dispatchers.IO) {
            val collection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
            )
            val selection =
                "${MediaStore.Images.Media.DATA} LIKE ?"
            val selectionArgs = arrayOf("%/Pictures/Mobile-Images/%")

            val photo = mutableListOf<Photo>()
            requireContext().contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Images.Media.DATE_MODIFIED} ASC, ${MediaStore.Images.Media.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = Uri.withAppendedPath(
                        collection,
                        id.toString()
                    )
                    photo.add(Photo(id, name, width, height, contentUri))
                }
                photo.toList()
            } ?: listOf()
        }
    }

    private fun cardClicked(holder: MobileImagesAdapter.MobileImageViewHolder, photo: Photo) {
        if (viewModel.selectedImages.contains(photo)) {
            viewModel.selectedImages.remove(photo)
            holder.binding.itemCardView.isChecked = false
            holder.binding.itemCardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.opaque
                )
            )
        } else {
            viewModel.selectedImages.add(photo)
            holder.binding.itemCardView.isChecked = true
            holder.binding.itemCardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.selected_item_bg
                )
            )
        }
        // Make the delete icon in the menu bar visible
        requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).menu.findItem(R.id.action_delete)
            .setOnMenuItemClickListener {
                lifecycleScope.launch {
                    deleteSelectedImages()
                    loadImages()
                }
                it.setVisible(false)
                true
            }
            .also { menuItem ->
                menuItem.setVisible(viewModel.selectedImages.size > 0)
            }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

}