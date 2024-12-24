package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.Meditation.Sounds.frequencies.R

class CustomEditTextWithTitle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val editText: EditText
    private val titleTextView: TextView
    private val errorMessageTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_edittext_custom, this, true)
        editText = findViewById(R.id.contentEditText)
        titleTextView = findViewById(R.id.titleTextView)
        errorMessageTextView = findViewById(R.id.errorMessageTextView)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CustomEditTextWithTitle, 0, 0)

            val title = typedArray.getString(R.styleable.CustomEditTextWithTitle_title)
            val hint = typedArray.getString(R.styleable.CustomEditTextWithTitle_hint)
            val textSize = typedArray.getDimension(R.styleable.CustomEditTextWithTitle_textSize, -1f)
            val titleTextSize = typedArray.getDimension(R.styleable.CustomEditTextWithTitle_titleTextSize, -1f)
            val errorTextSize = typedArray.getDimension(R.styleable.CustomEditTextWithTitle_errorTextSize, -1f)
            val imeOptions = typedArray.getInt(R.styleable.CustomEditTextWithTitle_imeOptions, -1)
            val inputType = typedArray.getInt(R.styleable.CustomEditTextWithTitle_inputType, -1)
            editText.doOnTextChanged { _, _, _, _ ->
                errorMessageTextView.text = ""
            }
            title?.let { titleTextView.text = it }
            hint?.let { editText.hint = it }
            if (imeOptions != -1) {
                editText.imeOptions = imeOptions
            }
            if (inputType != -1) {
                editText.inputType = inputType
            }
            if (textSize != -1f) {
                @Suppress("DEPRECATION")
                editText.textSize = textSize / resources.displayMetrics.scaledDensity
            }
            if (titleTextSize != -1f) {
                @Suppress("DEPRECATION")
                titleTextView.textSize = titleTextSize / resources.displayMetrics.scaledDensity
            }
            if (errorTextSize != -1f) {
                @Suppress("DEPRECATION")
                errorMessageTextView.textSize = errorTextSize / resources.displayMetrics.scaledDensity
            }

            typedArray.recycle()
        }
        orientation = VERTICAL
    }

    fun getText(): String {
        return editText.text.toString()
    }

    fun showError(message: String) {
        errorMessageTextView.text = message
    }

    fun clearError() {
        errorMessageTextView.text = ""
        errorMessageTextView.visibility = GONE
    }
}