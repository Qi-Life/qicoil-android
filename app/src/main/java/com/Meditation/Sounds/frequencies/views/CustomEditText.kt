package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.Meditation.Sounds.frequencies.R

class CustomEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val editText: EditText
    private val errorTextView: TextView
    private val iconLeft: ImageView
    private val iconRight: ImageView
    private var isPasswordVisible = false
    init {
        // Inflate layout
        LayoutInflater.from(context).inflate(R.layout.view_custom_edit_text, this, true)

        // Bind views
        editText = findViewById(R.id.editText)
        errorTextView = findViewById(R.id.errorTextView)
        iconLeft = findViewById(R.id.iconLeft)
        iconRight = findViewById(R.id.iconRight)

        // Load custom attributes
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText)
        val inputType = attributes.getInt(
            R.styleable.CustomEditText_android_inputType, InputType.TYPE_CLASS_TEXT
        )
        val textSize = attributes.getDimension(R.styleable.CustomEditText_android_textSize, 16f)
        val hint = attributes.getString(R.styleable.CustomEditText_android_hint)
        val fontFamily = attributes.getString(R.styleable.CustomEditText_android_fontFamily)
        val textColor =
            attributes.getColor(R.styleable.CustomEditText_android_textColor, Color.BLACK)
        val text = attributes.getString(R.styleable.CustomEditText_android_text)
        val icLeft: Drawable? = attributes.getDrawable(R.styleable.CustomEditText_iconLeft)
        val icRight: Drawable? = attributes.getDrawable(R.styleable.CustomEditText_iconRight)

        attributes.recycle()

        editText.doOnTextChanged { s, _, _, _ ->
            errorTextView.text = ""
            if (s.toString().isNotEmpty() && icRight != null) {
                iconRight.visibility = VISIBLE
            } else {
                iconRight.visibility = GONE
            }
        }
        // Apply attributes
        editText.inputType = inputType
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        editText.hint = hint
        editText.typeface = Typeface.create(fontFamily, Typeface.NORMAL)
        editText.setTextColor(textColor)
        editText.setText(text)
        icLeft?.let {
            iconLeft.setImageDrawable(it)
        }
        icRight?.let {
            iconRight.setImageDrawable(it)
            iconRight.setOnClickListener {
                if (isPasswordVisible) {
                    // Hide password
                    editText.transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    // Show password
                    editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
                editText.setSelection(editText.text.length)
                isPasswordVisible = !isPasswordVisible
            }
        }
    }

    fun getText(): String {
        return editText.text.toString()
    }

    fun showError(error: String?) {
        errorTextView.text = error
        errorTextView.visibility = if (error.isNullOrEmpty()) GONE else VISIBLE
    }
}