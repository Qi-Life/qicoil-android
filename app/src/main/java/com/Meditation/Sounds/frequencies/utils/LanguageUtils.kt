package com.Meditation.Sounds.frequencies.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.codeLanguage
import com.Meditation.Sounds.frequencies.models.Language
import java.util.Locale


class LanguageUtils {
    companion object {
        fun getLanguages(context: Context): List<Language> {
            return listOf(
                Language(
                    R.drawable.ic_united_kingdom,
                    "English",
                    context.getString(R.string.lang_en),
                    "en"
                ),
                Language(
                    R.drawable.ic_spain,
                    "Spanish",
                    context.getString(R.string.lang_es),
                    "es"
                ),
                Language(
                    R.drawable.ic_france,
                    "French",
                    context.getString(R.string.lang_fr),
                    "fr"
                ),
                Language(
                    R.drawable.ic_china,
                    "China",
                    context.getString(R.string.lang_zh),
                    "zh"
                ),
                Language(
                    R.drawable.ic_china,
                    "Japanese",
                    context.getString(R.string.lang_ja),
                    "ja"
                ),
                Language(
                    R.drawable.ic_china,
                    "Vietnamese",
                    context.getString(R.string.lang_vi),
                    "vi"
                ),
                Language(
                    R.drawable.ic_china,
                    "German",
                    context.getString(R.string.lang_de),
                    "de"
                ),
                Language(
                    R.drawable.ic_china,
                    "Chinese - Traditional",
                    context.getString(R.string.lang_zh_hant),
                    "zh_hant"
                ),
                Language(
                    R.drawable.ic_china,
                    "Chinese - Simplified",
                    context.getString(R.string.lang_zh_hans),
                    "zh_hans"
                ),
                Language(
                    R.drawable.ic_china,
                    "Hindi",
                    context.getString(R.string.lang_hi),
                    "hi"
                ),
                Language(
                    R.drawable.ic_china,
                    "Arabic",
                    context.getString(R.string.lang_ar),
                    "ar"
                ),
                Language(
                    R.drawable.ic_china,
                    "Portuguese",
                    context.getString(R.string.lang_pt),
                    "pt"
                ),
                Language(
                    R.drawable.ic_china,
                    "Korean",
                    context.getString(R.string.lang_ko),
                    "ko"
                ),
                Language(
                    R.drawable.ic_china,
                    "Italian",
                    context.getString(R.string.lang_it),
                    "it"
                ),
                Language(
                    R.drawable.ic_china,
                    "Bengali",
                    context.getString(R.string.lang_bn),
                    "bn"
                ),
            )
        }

        fun getLanguage(languageCode: String, context: Context): Language {
            return getLanguages(context).firstOrNull { it.code == languageCode } ?: Language(
                R.drawable.ic_united_kingdom, "English", context.getString(R.string.lang_en), "en"
            )
        }

        fun changeLanguage(context: Context, codeLanguage: String) {
            PreferenceHelper.preference(context).codeLanguage = codeLanguage
            val locale = Locale(codeLanguage)
            Locale.setDefault(locale)
            val otherLanguages = getLanguages(context).filter { it.code != codeLanguage }
                .joinToString(",") { it.code }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("$codeLanguage,$otherLanguages"))
        }
    }
}