package br.com.marcoscsouza.smpaattest.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import br.com.marcoscsouza.smpaattest.R
import br.com.marcoscsouza.smpaattest.databinding.ActivityCreateOccurrenceBinding
import br.com.marcoscsouza.smpaattest.db.Criptografia
import br.com.marcoscsouza.smpaattest.ui.user.UserLoginActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CreateOccurrenceActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCreateOccurrenceBinding.inflate(layoutInflater)
    }
    private val firebaseAuth = Firebase.auth
    private var occurrenceId: String? = null
    private val REQUEST_CAPTURE_IMAGE = 100
    private var image: ByteArray? = null
    val REQUEST_PERMISSIONS_CODE = 666
    private var latitude: String = ""
    private var longitude: String = ""

    private val locationListener: LocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                latitude = "${location.latitude}"
                longitude = "${location.longitude}"
                binding.localPreview.text = "$latitude $longitude"

            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "criar "

//        occurrenceId = intent.getStringExtra("OCCURRENCE_ID")

        binding.createOccurrenceImg.setOnClickListener {
//            CaptureImgOccurrenceDialog(this)
//                .mostrar(){
//
//                }
            when {
                checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    val picIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(picIntent, REQUEST_CAPTURE_IMAGE)
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                    Toast.makeText(this, "Erro ao abrir a camera!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAPTURE_IMAGE
                    )
                }
            }

        }
        binding.btSalvar.setOnClickListener {


            val permission_a = ContextCompat.checkSelfPermission(this@CreateOccurrenceActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)
            val permission_b = ContextCompat.checkSelfPermission(this@CreateOccurrenceActivity,android.Manifest.permission.ACCESS_COARSE_LOCATION)

            if (permission_a != PackageManager.PERMISSION_GRANTED &&permission_b != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@CreateOccurrenceActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                ) {
                    callDialog(
                        "É preciso permitir acesso à localização!",
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this@CreateOccurrenceActivity,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_CODE
                    )
                }
            } else {
                getCurrentCoordinates()
                mostrarPreview()
            }

        }

    }

    private fun mostrarPreview() {
        val campoTitulo = binding.createOccurrenceTitulo
        val titulo = campoTitulo.text.toString()

        val campoTexto = binding.createOccurrenceTexto
        val texto = campoTexto.text.toString()

        if (titulo.isNullOrBlank() || texto.isNullOrBlank()) {
            Toast.makeText(
                this,
                "É preciso preencher todos os dados da ocorrência.",
                Toast.LENGTH_SHORT
            ).show()
        } else {

            binding.pvwCard.visibility = View.VISIBLE

    //                configuração da data e hora atual
            val data = Calendar.getInstance().time
            val formatarData = SimpleDateFormat("dd.MM.yyyy", Locale.ROOT)
            val dataFormatada = formatarData.format(data)

            val nomeArquivo = "${titulo.uppercase(Locale.ROOT)}*${dataFormatada}"

            binding.tituloPreview.text = titulo
            binding.textoPreview.text = texto
            binding.imagemPreview.setImageBitmap(binding.createOccurrenceImg.drawToBitmap())
            binding.dataPreview.text = dataFormatada
//            binding.localPreview.text = "${locationListener.onLocationChanged()} e ${locationListener.}"

            Criptografia().encryptText(
                "${nomeArquivo}.txt",
                this,
                listOf(latitude, longitude, texto)
            )

            Criptografia().encryptImage(
                "${nomeArquivo}.fig",
                this,
                image!!
            )
        }
    }

    private fun getCurrentCoordinates() {
        val getGps =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = getGps.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        )
        val isNetworkEnabled = getGps.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
        if (!isGPSEnabled && !isNetworkEnabled) {
            Log.d("Permissao", "Ative os serviços necessários")
        } else {
            if (isGPSEnabled) {
                try {
                    getGps.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        30000L, 0f, locationListener
                    )
                } catch (ex: SecurityException) {
                    Log.d("Permissao", "Erro de permissão")
                }
            } else if (isNetworkEnabled) {
                try {
                    getGps.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L, 0f, locationListener
                    )
                } catch (ex: SecurityException) {
                    Log.d("Permissao", "Erro de permissão")
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (!estaLogado()) {
            val i = Intent(this, UserLoginActivity::class.java)
            startActivity(i)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )
        val imgCamera = binding.createOccurrenceImg

        if (requestCode == REQUEST_CAPTURE_IMAGE &&
            resultCode == Activity.RESULT_OK){

//                capturar imagem na activity 
            if (data != null && data.extras != null) {
                val imageBitmap = data.extras!!["data"] as Bitmap?
                imgCamera.setImageBitmap(imageBitmap)

//                encriptar a imagem da camera
                val streamOutput = ByteArrayOutputStream()
                imageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, streamOutput)
                val byteArray = streamOutput.toByteArray()
                image = byteArray
            }
        }

    }



    private fun callDialog(
        mensage_alerts: String ,
        permissions: Array<String>
    ) {
        var mDialog = AlertDialog.Builder(this)
            .setTitle("Permissão")
            .setMessage(mensage_alerts)
            .setPositiveButton("Ok")
            { dialog, id ->
                ActivityCompat.requestPermissions(
                    this@CreateOccurrenceActivity, permissions,
                    REQUEST_PERMISSIONS_CODE
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancela")
            { dialog, id ->
                dialog.dismiss()
            }
        mDialog.show()
    }


    fun estaLogado(): Boolean {
        val userFire: FirebaseUser? = firebaseAuth.currentUser
        return if (userFire != null) {
            true
        } else {
            Toast.makeText(this, "Usuário não está logado!", Toast.LENGTH_SHORT).show()
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.exitMenu -> {
                Toast.makeText(this, "Usuário deslogado.", Toast.LENGTH_SHORT).show()
                firebaseAuth.signOut()
                val i = Intent(this, UserLoginActivity::class.java)
                startActivity(i)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}