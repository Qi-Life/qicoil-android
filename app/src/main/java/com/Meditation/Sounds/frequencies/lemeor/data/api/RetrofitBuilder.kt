package com.Meditation.Sounds.frequencies.lemeor.data.api

import android.annotation.SuppressLint
import android.content.Context
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.lemeor.data.api.ApiConfig.TIME_OUT
import com.chuckerteam.chucker.api.ChuckerInterceptor
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitBuilder(val context: Context) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else
            HttpLoggingInterceptor.Level.NONE
    }

    private val authInterceptor = ApiInterceptor(context)

    private val client: OkHttpClient = unSafeOkHttpClient()
        .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(TIME_OUT, TimeUnit.SECONDS)
        .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(ChuckerInterceptor.Builder(context).build())
        .retryOnConnectionFailure(true)
        .build()

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }


//    @SuppressLint("TrustAllX509TrustManager")
//    private fun provideUnsafeOkhttpClient(context: Context): OkHttpClient {
//        /* Trust anything*/
//        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
//            override fun getAcceptedIssuers(): Array<X509Certificate> {
//                return emptyArray()
//            }
//
//            @Throws(CertificateException::class)
//            override fun checkClientTrusted(
//                    chain: Array<X509Certificate>,
//                    authType: String,
//            ) {
//            }
//
//            @Throws(CertificateException::class)
//            override fun checkServerTrusted(
//                    chain: Array<X509Certificate>,
//                    authType: String,
//            ) {
//            }
//        })
//
//        val sslContext = SSLContext.getInstance("SSL")
//        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
//        val sslSocketFactory = sslContext.socketFactory
//
//        val client = OkHttpClient.Builder()
//            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
//            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
//            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
//            .addInterceptor(loggingInterceptor)
//            .addInterceptor(authInterceptor)
//            .addInterceptor(ChuckerInterceptor.Builder(context).build())
//
//
//        client.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
//        client.hostnameVerifier { _, _ -> true }
//        /* Rest of config*/
//        client.connectTimeout(TIME_OUT, TimeUnit.SECONDS)
//            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
//            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
//            .addInterceptor(loggingInterceptor)
//            .addInterceptor(authInterceptor)
//            .addInterceptor(ChuckerInterceptor.Builder(context).build())
//        if (!BuildConfig.DEBUG) {
//            throw RuntimeException("You fool. Do not use this in production!!!")
//        }
//
//        return client.build()
//    }

    @SuppressLint("TrustAllX509TrustManager")
    fun unSafeOkHttpClient(): OkHttpClient.Builder {
        val okHttpClient = OkHttpClient.Builder().retryOnConnectionFailure(true)
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts: Array<TrustManager> = arrayOf(@SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            if (trustAllCerts.isNotEmpty() && trustAllCerts.first() is X509TrustManager) {
                okHttpClient.sslSocketFactory(sslSocketFactory, trustAllCerts.first() as X509TrustManager)
                okHttpClient.hostnameVerifier { _, _ -> true }
            }

            return okHttpClient
        } catch (e: Exception) {
            return okHttpClient
        }
    }


    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}