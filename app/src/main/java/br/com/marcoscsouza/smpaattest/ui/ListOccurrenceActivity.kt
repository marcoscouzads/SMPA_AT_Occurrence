package br.com.marcoscsouza.smpaattest.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.marcoscsouza.smpaattest.R
import br.com.marcoscsouza.smpaattest.adapter.OccurrenceAdapter
import br.com.marcoscsouza.smpaattest.databinding.ActivityListOccurrenceBinding
import br.com.marcoscsouza.smpaattest.db.Criptografia
import br.com.marcoscsouza.smpaattest.db.Occurrence
import br.com.marcoscsouza.smpaattest.ui.user.UserLoginActivity
import com.android.billingclient.api.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ListOccurrenceActivity : AppCompatActivity(),
    BillingClientStateListener,
    SkuDetailsResponseListener,
    PurchasesUpdatedListener,
    ConsumeResponseListener {
    private val binding by lazy {
        ActivityListOccurrenceBinding.inflate(layoutInflater)
    }
    private lateinit var userApp: BillingClient
    lateinit var auth: FirebaseAuth
    private var mUser: FirebaseUser? = null
//    private val firebaseAuth = Firebase.auth
//    private val firestore = Firebase.firestore
//    private val adapter = OccurrenceAdapter(this,fillDatalist())
    private var currentSku = "android.test.purchased"
    private val PREF_FILE = "PREF_FILE"
    private lateinit var sharedPref: SharedPreferences
    private var mapSku = HashMap<String,SkuDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = "Lista de ocorrências"

        auth = FirebaseAuth.getInstance()

        MobileAds.initialize(this)
        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

//        firestore.collection("occurrences")
        btnFab()

        binding.btnRemoveAds.setOnClickListener {
            val skuDetails = mapSku[currentSku]
            val purchaseParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails).build()
            userApp.launchBillingFlow(this, purchaseParams)
        }

        userApp = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        userApp.startConnection(this)

        checkBuy()

    }

    override fun onDestroy() {
        userApp.endConnection()
        super.onDestroy()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        mUser = auth.currentUser
        updateView()
        listAnnotation()

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun listAnnotation(){
        val rv = binding.rvOccurrence
        rv.adapter = OccurrenceAdapter(fillDatalist())
        rv.layoutManager = LinearLayoutManager(this)
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun fillDatalist(): List<Occurrence> {
        val pathArq = File(this.filesDir.toURI())
        var prefix = ""
        val data = mutableListOf<Occurrence>()
        val files = pathArq.listFiles()

        files?.forEach {
            if ("$prefix.txt" != it.name && "$prefix.fig" != it.name) {
                prefix = it.name.removeSuffix(".txt")
                prefix = prefix.removeSuffix(".fig")

                data.add(getDatabase(prefix))
            }
        }

        return data
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDatabase(prefix: String): Occurrence {
        var delSufix = prefix.removeSuffix(".fig")
        delSufix = delSufix.removeSuffix(".txt")
        val img: ByteArray = Criptografia().encryptReadImage("$delSufix.fig", this)
        val text: String = Criptografia().encryptReadText("$delSufix.txt", this)[2]
        val title = prefix.split("*")[0]
        val data = prefix.split("*")[1].removeSuffix("*")
        val bitmap = BitmapFactory.decodeByteArray(img, 0, img.size)


        return Occurrence(titulo = title, texto =  text, data =  data, foto =  bitmap)
    }

    fun updateView(){
        binding.userFirebase.text = "Olá, ${mUser!!.email}!"
    }

    override fun onBillingServiceDisconnected() {
        Log.d("COMPRA>>","Serviço InApp desconectado")

    }

    override fun onBillingSetupFinished(billingResult: BillingResult?) {
        if(billingResult?.responseCode ==
            BillingClient.BillingResponseCode.OK){
            Log.d("COMPRA>>","Serviço InApp conectado")
            val skuList = arrayListOf(currentSku)
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(
                BillingClient.SkuType.INAPP)
            userApp.querySkuDetailsAsync(params.build(), this)
        }
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult?,
                                      skuDetailsList: MutableList<SkuDetails>?) {
        if(billingResult?.responseCode ==
            BillingClient.BillingResponseCode.OK){
            mapSku.clear()
            skuDetailsList?.forEach{
                    t ->
                mapSku[t.sku] = t
                val preco = t.price
                val descricao = t.description
                Log.d("COMPRA>>",
                    "Produto Disponivel ($preco): $descricao")
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode ==
            BillingClient.BillingResponseCode.OK &&
            purchases != null){

            for (purchase in purchases) {
                GlobalScope.launch (Dispatchers.IO){
                    handlePurchase(purchase)
                }
            }
        }
        else if (billingResult.responseCode ==
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            Log.d("COMPRA JA REALIZADA>>",
                "Produto já foi comprado")

            val userId = auth.currentUser?.uid
            val editor =
                getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit()
            editor.putBoolean(userId, true)
            editor.commit()

        }
        else if (billingResult.responseCode ==
            BillingClient.BillingResponseCode.USER_CANCELED){
            Log.d("COMPRA CANCELADA>>",
                "Usuário cancelou a compra")

        }
        else{
            Log.d("ERRO NA COMPRA>>",
                "Código de erro desconhecido: ${billingResult.responseCode}")
        }

    }

    suspend fun handlePurchase (purchase: Purchase) {
        if (purchase.purchaseState === Purchase.PurchaseState.PURCHASED){
            Log.d("COMPRA>>","Parabéns pela compra!Reinicie o app para acessar a versão premium!")
            val userId = auth.currentUser?.uid
            val editor =
                getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit()
            editor.putBoolean(userId, true)
            editor.apply()

            if (!purchase.isAcknowledged){
                val acknowledgePurchaseParams = AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)

                val ackPurchaseResult = withContext(Dispatchers.IO){
                    userApp.acknowledgePurchase(
                        acknowledgePurchaseParams.build())
                }
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        if (!estaLogado()) {
//            val i = Intent(this, UserLoginActivity::class.java)
//            startActivity(i)
//        }
//    }

    private fun btnFab() {
        val fab = binding.fabListOccurrence
        fab.setOnClickListener {
            val intent = Intent(this, CreateOccurrenceActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkBuy() {
        val preferences =
            getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val userId = auth.currentUser?.uid
        val isPurchase = preferences.getBoolean(userId, false)
        if (isPurchase) {
            binding.adView.setVisibility(View.GONE)
            binding.btnRemoveAds.setVisibility(View.GONE)
        }
    }

    override fun onConsumeResponse(billingResult: BillingResult?, string: String?) {
        if (billingResult?.responseCode ==
            BillingClient.BillingResponseCode.OK){
            Log.d( "COMPRA>>" , "Produto Consumido" )
        }
    }


//    fun estaLogado(): Boolean {
//        val userFire: FirebaseUser? = firebaseAuth.currentUser
//        return if (userFire != null) {
//            true
//        } else {
//            Toast.makeText(this, "Usuário não está logado!", Toast.LENGTH_SHORT).show()
//            false
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.exitMenu -> {
                Toast.makeText(this, "Usuário deslogado.", Toast.LENGTH_SHORT).show()
                auth .signOut()
                mUser = null
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}