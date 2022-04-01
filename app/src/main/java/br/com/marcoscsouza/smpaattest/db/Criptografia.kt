package br.com.marcoscsouza.smpaattest.db

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.*

class Criptografia {

    private fun encryptFile(nome: String , context: Context): EncryptedFile {
        val masterKeyAlias: String =
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val getingFiles = File(context.filesDir, nome)
        return EncryptedFile.Builder(
            getingFiles,
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB)
            .build()
    }

    fun encryptText(nome: String , context: Context , txt : List<String>){
        val encryptedOut: FileOutputStream =
            encryptFile(nome, context).openFileOutput()
        val printInScreen = PrintWriter(encryptedOut)
        txt.forEach{
            printInScreen.println(it)
        }
        printInScreen.flush()
        encryptedOut.close()
    }

    fun encryptImage(nome: String , context: Context , img: ByteArray){
        val showCryptoImage: FileOutputStream =
            encryptFile(nome, context).openFileOutput()
        showCryptoImage.write(img)
        showCryptoImage.close()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun encryptReadText(nome: String , context: Context) : List<String>{
        val encryptedIn: FileInputStream =
            encryptFile(nome, context).openFileInput()
        val readTextCrypto = BufferedReader(InputStreamReader(encryptedIn))
        val result = mutableListOf<String>()
        readTextCrypto.lines().forEach{
            result.add(it)
        }
        encryptedIn.close()
        return result
    }

    fun encryptReadImage(nome: String , context: Context) : ByteArray{
        val takeCryptoImage: FileInputStream =
            encryptFile(nome, context).openFileInput()
        val readCryptoImg = ByteArrayInputStream(takeCryptoImage.readBytes())
        return readCryptoImg.readBytes()
    }
}