package com.vdx.flagship

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.vdx.flagship.internal.FlagStore
import com.vdx.flagship.internal.FlagshipApiService
import com.vdx.flagship.model.FeatureFlag
import com.vdx.flagship.model.FlagType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicBoolean

object Flagship {
    private const val TAG = "FlagshipSDK"

    private var apiService: FlagshipApiService? = null

    @SuppressLint("StaticFieldLeak")
    private var flagStore: FlagStore? = null

    private var applicationKey: String = ""

    @Volatile
    private var memoryCache: Map<String, FeatureFlag> = emptyMap()

    private var initDefaults: Map<String, Any> = emptyMap()

    private val _isInitialized = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private const val MAX_RETRIES = 3
    private const val INITIAL_DELAY_MS = 1000L

    /**
     * Initialize the SDK.
     *
     * @param context Application Context.
     * @param apiKey Your application API Key.
     * @param baseUrl Your self-hosted Flagship Dashboard URL (Required).
     * @param loadData If true, fetches flags immediately.
     * @param defaults Optional map of default values.
     */
    fun initialize(
        context: Context,
        apiKey: String,
        baseUrl: String,
        loadData: Boolean = true,
        defaults: Map<String, Any>? = null
    ) {
        if (_isInitialized.getAndSet(true)) return

        // STRICT VALIDATION
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("Flagship: API Key cannot be empty")
        }
        if (baseUrl.isBlank()) {
            throw IllegalArgumentException("Flagship: Base URL cannot be empty")
        }

        this.applicationKey = apiKey
        this.flagStore = FlagStore(context.applicationContext)

        defaults?.let { this.initDefaults = it }

        // Ensure URL ends with /
        val safeUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        val retrofit = Retrofit.Builder()
            .baseUrl(safeUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        this.apiService = retrofit.create(FlagshipApiService::class.java)

        scope.launch(Dispatchers.IO) {
            try {
                flagStore?.flagsFlow?.collect { flags ->
                    if (flags.isNotEmpty()) {
                        memoryCache = flags
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading cache: ${e.message}")
            }
        }

        if (loadData) {
            refresh()
        }
    }


    /**
     * Fetch flags safely (Suspend).
     * Returns the flags if successful, or falls back to cache/defaults/empty.
     * Never throws an exception.
     */
    suspend fun fetchFlags(): List<FeatureFlag> {
        if (!_isInitialized.get()) {
            Log.e(TAG, "SDK not initialized. Returning fallback.")
            return getFallbackList()
        }

        return withContext(Dispatchers.IO) {
            var attempt = 0
            var delayMs = INITIAL_DELAY_MS

            // Retry Logic
            while (attempt < MAX_RETRIES) {
                attempt++
                try {
                    val fetchedFlags = apiService?.getFlags(applicationKey)
                    if (!fetchedFlags.isNullOrEmpty()) {
                        flagStore?.saveFlags(fetchedFlags)
                        memoryCache = fetchedFlags.associateBy { it.key }
                        Log.d(TAG, "Flags fetched on attempt $attempt")
                        return@withContext fetchedFlags
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Fetch attempt $attempt failed: ${e.message}")
                }
                delay(delayMs)
                delayMs *= 2
            }

            // Fallbacks
            if (memoryCache.isNotEmpty()) return@withContext memoryCache.values.toList()

            val defaults = getFallbackList()
            if (defaults.isNotEmpty()) {
                memoryCache = defaults.associateBy { it.key }
                return@withContext defaults
            }

            return@withContext emptyList()
        }
    }

    /**
     * Triggers a background refresh of flags.
     */
    fun refresh() {
        scope.launch { fetchFlags() }
    }


    private fun getFallbackList(): List<FeatureFlag> {
        if (initDefaults.isEmpty()) return emptyList()
        return initDefaults.map { (key, value) ->
            val (type, valString) = when (value) {
                is Boolean -> FlagType.BOOLEAN to null
                is Number -> FlagType.NUMBER to value.toString()
                is String -> FlagType.STRING to value
                else -> FlagType.JSON to value.toString()
            }

            val isEnabled = value as? Boolean != false

            FeatureFlag(
                key = key,
                enabled = isEnabled,
                displayName = key,
                description = "Fallback default",
                value = valString,
                type = type
            )
        }
    }


    fun isEnabled(key: String, defaultValue: Boolean = false): Boolean {
        memoryCache[key]?.let { return it.enabled }
        val initDefault = initDefaults[key]
        if (initDefault is Boolean) return initDefault
        return defaultValue
    }

    fun getString(key: String, defaultValue: String = ""): String {
        val flag = memoryCache[key]
        if (flag != null && flag.enabled && flag.type == FlagType.STRING) {
            return flag.value ?: defaultValue
        }
        val initDefault = initDefaults[key]
        if (initDefault is String) return initDefault
        return defaultValue
    }

    fun getNumber(key: String, defaultValue: Double = 0.0): Double {
        val flag = memoryCache[key]
        if (flag != null && flag.enabled && flag.type == FlagType.NUMBER) {
            return flag.value?.toDoubleOrNull() ?: defaultValue
        }
        val initDefault = initDefaults[key]
        if (initDefault is Number) return initDefault.toDouble()
        return defaultValue
    }

    fun getNumber(key: String, defaultValue: Int): Int {
        return getNumber(key, defaultValue.toDouble()).toInt()
    }

    fun getJson(key: String, defaultValue: String = "{}"): String {
        val flag = memoryCache[key]
        if (flag != null && flag.enabled && flag.type == FlagType.JSON) {
            return flag.value ?: defaultValue
        }

        val initDefault = initDefaults[key]
        if (initDefault != null && initDefault !is Boolean && initDefault !is Number) {
            return initDefault as? String ?: initDefault.toString()
        }

        return defaultValue
    }
}
