package com.madrat.diabeteshelper.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.madrat.diabeteshelper.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory


object NetworkClient {
    private const val serverUrl = "https://193.38.235.203:8443/dh_server/"
    private const val keystorePass = "123456"
    
    fun readKeyStore(context: Context): KeyStore? {
        val keyStore = KeyStore.getInstance(
            //KeyStore.getDefaultType()
            //
            "PKCS12"
        )
        
        // get user password and file input stream
        //val password: CharArray = getPassword()
        
        var inputStream: InputStream? = null
        try {
            inputStream = context.resources.openRawResource(R.raw.tomcat);
            keyStore.load(inputStream, keystorePass.toCharArray())
        } finally {
            inputStream?.close()
        }
        return keyStore
    }
    
    fun getRetrofit(context: Context): Retrofit {
        val keyStore = readKeyStore(context) //your method to obtain KeyStore
    
        val sslContext = SSLContext.getInstance("SSL")
        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(keyStore)
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, keystorePass.toCharArray())
        sslContext.init(
            keyManagerFactory.keyManagers,
            trustManagerFactory.trustManagers,
            SecureRandom()
        )
        
        val client = OkHttpClient.Builder().apply {
            hostnameVerifier { _, _ -> true }
            sslSocketFactory(sslContext.socketFactory)
            connectTimeout(10, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            addInterceptor(
                ChuckerInterceptor.Builder(context).apply {
                    collector(ChuckerCollector(context))
                    maxContentLength(250000L)
                    redactHeaders(emptySet())
                    alwaysReadResponseBody(false)
                }.build()
            )
        }.build()
        
        val rxJava3Adapter = RxJava3CallAdapterFactory.create()
        
        return Retrofit.Builder().apply {
            baseUrl(serverUrl)
            addConverterFactory(GsonConverterFactory.create())
            addCallAdapterFactory(rxJava3Adapter)
            client(client)
        }.build()
    }
    
    fun getService(context: Context): NetworkInterface {
        return getRetrofit(context).create(NetworkInterface::class.java)
    }
    
    //fun getChuckerCollector(): ChuckerCollector = ChuckerCollector(context)
}