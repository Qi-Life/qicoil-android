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
                    context.getString(R.string.lang_en),
                    "en"
                ),
                Language(
                    R.drawable.ic_spain,
                    context.getString(R.string.lang_es),
                    "es"
                ),
                Language(
                    R.drawable.ic_france,
                    context.getString(R.string.lang_fr),
                    "fr"
                ),
//                Language(
//                    R.drawable.ic_china,
//                    "China",
//                    context.getString(R.string.lang_zh),
//                    "zh"
//                ),
                Language(
                    R.drawable.ic_japan,
                    context.getString(R.string.lang_ja),
                    "ja"
                ),
                Language(
                    R.drawable.ic_vietnam,
                    context.getString(R.string.lang_vi),
                    "vi"
                ),
                Language(
                    R.drawable.ic_german,
                    context.getString(R.string.lang_de),
                    "de"
                ),
                Language(
                    R.drawable.ic_china_tradi,
                    context.getString(R.string.lang_zh_hant),
                    "zh-rTW"
                ),
                Language(
                    R.drawable.ic_china_impli,
                    context.getString(R.string.lang_zh_hans),
                    "zh-rCN"
                ),
                Language(
                    R.drawable.ic_hindi,
                    context.getString(R.string.lang_hi),
                    "hi"
                ),
                Language(
                    R.drawable.ic_arabic_flag,
                    context.getString(R.string.lang_ar),
                    "ar"
                ),
                Language(
                    R.drawable.ic_portu,
                    context.getString(R.string.lang_pt),
                    "pt"
                ),
                Language(
                    R.drawable.ic_korea,
                    context.getString(R.string.lang_ko),
                    "ko"
                ),
                Language(
                    R.drawable.ic_italia,
                    context.getString(R.string.lang_it),
                    "it"
                ),
                Language(
                    R.drawable.ic_bengali,
                    context.getString(R.string.lang_bn),
                    "bn"
                ),
            )
        }

        fun getLanguage(languageCode: String, context: Context): Language {
            return getLanguages(context).firstOrNull { it.code == languageCode } ?: Language(
                R.drawable.ic_united_kingdom, context.getString(R.string.lang_en), "en"
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