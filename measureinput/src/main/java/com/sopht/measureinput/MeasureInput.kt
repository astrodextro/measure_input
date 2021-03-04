package com.sopht.measureinput

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat
import com.sopht.measureinput.databinding.LayoutMeasureInputBinding
import java.util.*

class MeasureInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private lateinit var binding: LayoutMeasureInputBinding
    private var value = 0f
    var unit = 0
    var unitSystem = 0
    private var enabled = false
    private val UNITS = intArrayOf(R.array.height_units, R.array.weight_units, R.array.temperature_units)
    private val HEIGHT = 0
    private val WEIGHT = 1
    private val TEMPERATURE = 2
    private var textStyle = 0
    private fun init(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.MeasureInput,
            0,
            0
        )
        value = a.getFloat(R.styleable.MeasureInput_value, 0f)
        unit = a.getInt(R.styleable.MeasureInput_unit, 0)
        unitSystem = a.getInt(R.styleable.MeasureInput_unitSystem, 0)
        enabled = attrs!!.getAttributeBooleanValue(DEFAULT_SCHEMA, "enabled", true)
        textStyle = if (a.hasValue(R.styleable.MeasureInput_textStyle)) {
            a.getInt(R.styleable.MeasureInput_textStyle, 0)
        } else {
            // use default schema
            attrs.getAttributeIntValue(DEFAULT_SCHEMA, "textStyle", 0)
        }
        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = LayoutMeasureInputBinding.inflate(LayoutInflater.from(context), this, true)
        val units = listOf(*resources.getStringArray(UNITS[unit]))
        binding.unit.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, units)
        binding.unit.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == unitSystem) {
                    return
                }
                unitSystem = position
                val text = binding.value.text
                if (TextUtils.isEmpty(text)) {
                    return
                }
                value = text.toString().toFloat()
                if (unitSystem == 0) {
                    convertToMetric()
                } else {
                    convertToImperial()
                }
                setValue(value)
                (parent?.getChildAt(0) as EditText).setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.value.setText(if (value == 0f) null else String.format(Locale.getDefault(), "%.2f", value))
        binding.value.isEnabled = enabled
    }

    fun getValue(): Float {
        val text = binding.value.text
        if (TextUtils.isEmpty(text)) {
            return 0F
        }
        value = text.toString().toFloat()
        if (unitSystem == 1) {
            convertToMetric()
        }
        return value
    }

    fun setValue(value: Float) {
        this.value = value
        binding.value.setText(if (value == 0f) null else String.format(Locale.getDefault(), "%.2f", value))
    }

    fun convertToImperial() {
        when (unit) {
            HEIGHT -> value /= 30.48f
            WEIGHT -> value /= 0.45359237f
            TEMPERATURE -> value = value * 9 / 5 + 32
            else -> throw IllegalArgumentException("Unreadable unit")
        }
    }

    fun convertToMetric() {
        when (unit) {
            HEIGHT -> value *= 30.48f
            WEIGHT -> value *= 0.45359237f
            TEMPERATURE -> value = (value - 32) * 5 / 9
            else -> throw IllegalArgumentException("Unreadable unit")
        }
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    companion object {
        private const val DEFAULT_SCHEMA = "xmlns:android=\"http://schemas.android.com/apk/res/android\""
    }

    init {
        init(attrs)
    }
}
