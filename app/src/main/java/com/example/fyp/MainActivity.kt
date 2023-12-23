package com.example.fyp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.fyp.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.findNavController()
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_share -> {
                // Compress the images and share them using an email intent
                val filesToBeShared = arrayListOf<String>()
                val directoryPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString() + "/Mobile-Images"
                val directory = File(directoryPath)
                if (directory.exists()) {
                    directory.listFiles()?.forEach { file ->
                        filesToBeShared.add(file.absolutePath)
                    }
                }
                // Creating an email intent and attaching the filesToBeShared
                // Create an email intent
                val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
                emailIntent.type = "text/plain"
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("bcsf20a503@pucit.edu.pk"))
                emailIntent.putExtra(
                    Intent.EXTRA_CC,
                    arrayOf(
                        "bcsf20a536@pucit.edu.pk",
                        "bcsf20a521@pucit.edu.pk",
                        "bcsf20a504@pucit.edu.pk",
                        "bcsf19a522@pucit.edu.pk"
                    )
                )
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out these images!")

                // Attach the images to the email
                val uris = ArrayList<Uri>()
                for (filePath in filesToBeShared) {
                    val file = File(filePath)
                    val uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "com.example.fyp.fileprovider",
                        file
                    )
                    uris.add(uri)
                }

                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)

                // Grant read permission to the receiving app
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // Start the email intent
                startActivity(Intent.createChooser(emailIntent, "Send email..."))

                true
            }
            else -> false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}