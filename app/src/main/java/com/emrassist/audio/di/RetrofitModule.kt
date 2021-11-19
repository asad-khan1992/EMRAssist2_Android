package com.emrassist.audio.di

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.emrassist.audio.App
import com.emrassist.audio.BuildConfig
import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.ApiManager
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {
    @Singleton
    @Provides
    fun provideGsonBuilder(): Gson {
        return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_MAIN)
            .addConverterFactory(GsonConverterFactory.create(gson))

    }

    @Singleton
    @Provides
    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 1

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .dispatcher(dispatcher)
            .connectTimeout(60 * 2, TimeUnit.SECONDS)
            .readTimeout(60 * 2, TimeUnit.SECONDS)
            .addInterceptor(providesOkhttpInterceptor())
            .build()

    }

    @Singleton
    @Provides
    fun provideApiClient(retrofitBuilder: Retrofit.Builder): ApiClient {
        return retrofitBuilder
            .client(provideOkHttp())
            .build()
            .create(ApiClient::class.java)
    }

    @Singleton
    @Provides
    fun provideApiManager(client: ApiClient): ApiManager {
        return ApiManager(client)
    }

    fun providesOkhttpInterceptor(): Interceptor {
        return Interceptor { chain: Interceptor.Chain ->

            synchronized(this) {

                val context = App.context

                if (!isNetworkConnected(context))
                    throw NoInternetException()

                var request = chain.request()
                val builder = request.newBuilder()
//
                val authToken = SharedPrefsUtils.user?.token
                if (!authToken.isNullOrBlank()) {
                    Log.d("retrofit", "token: $authToken")
                    builder.addHeader("token", authToken.toString())
                }
                request = builder.build()

                chain.proceed(request)
            }
        }
    }


    @Provides
    @Singleton
    public fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    class NoInternetException : IOException() {

        override fun getLocalizedMessage(): String {
            return "Please check your internet connection and try again."
        }
    }
}