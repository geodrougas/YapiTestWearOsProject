package com.coldhammer.teg.mytestwearapplication


import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import com.coldhammer.teg.mytestwearapplication.databinding.ActivityMainBinding
import com.mollin.yapi.YeelightDevice
import com.mollin.yapi.enumeration.YeelightEffect
import java.lang.Integer.max
import java.net.SocketException
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.log
import kotlin.math.min

class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding
    private var device: YeelightDevice? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var brightness = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executor.execute {
             connectToDevice(IP)
        }

        binding.button5.setOnClickListener {
            executor.execute {
                device?.use {
                    it.connect()
                    toggleLight()
                }
            }
        }
        binding.root.isFocusable = true
        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()
        binding.root.setOnGenericMotionListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_SCROLL &&
                    motionEvent.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER))
            {
                val delta = -motionEvent.getAxisValue(MotionEventCompat.AXIS_SCROLL) *
                        ViewConfigurationCompat.getScaledVerticalScrollFactor(
                            ViewConfiguration.get(this), this
                        )

                executor.execute {
                    val normalized = (abs(delta) / delta).toInt()
                    device?.use {
                        it.connect()
                        dimTheLights(normalized * 10)
                    }
                }

                true
            } else {
                false
            }
        }
        Log.d(TAG, "onCreate: Hello")
    }

    private fun connectToDevice(ip: String) {
        try {
            device = YeelightDevice(ip, 55443, YeelightEffect.SMOOTH, 200)

            binding.button5.foreground =
                resources.getDrawable(R.drawable.ic_baseline_power_settings_new_24, null)
        }catch (ex: java.lang.Exception) {
            logError(ex)
        }
    }

    private fun dimTheLights(amount: Int) {
        try {
            brightness = max(min(brightness + amount, 100), 0)
            Log.d(TAG, "onCreate: $brightness")

            device?.setBrightness(brightness)

            binding.progressBar.progress = brightness

            Log.d(TAG, "onCreate: rotation")
        } catch (ex: java.lang.Exception) {
            logError(ex)
        }
    }

    private fun toggleLight() {
        try {
            device?.toggle()
        }
        catch (ex: Exception) {
            logError(ex)
        }
    }

    private fun logError(ex: Exception) {
        runOnUiThread{
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            Log.e(TAG, "onCreate: ", ex)
        }
    }

    override fun onStop() {
        super.onStop()

    }

    companion object {
        private const val TAG = "MainActivity"
        private const val IP = "192.168.1.133"
    }
}