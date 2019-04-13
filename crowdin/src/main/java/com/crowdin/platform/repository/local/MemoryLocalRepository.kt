package com.crowdin.platform.repository.local

import com.crowdin.platform.repository.SearchResultData
import com.crowdin.platform.repository.remote.api.LanguageData
import java.util.*

/**
 * A LocalRepository which keeps the stringsData ONLY in memory.
 */
internal class MemoryLocalRepository : LocalRepository {

    private val stringsData = LinkedHashMap<String, LanguageData>()

    override fun saveLanguageData(languageData: LanguageData) {
        val data = stringsData[languageData.language]
        if (data == null) {
            stringsData[languageData.language] = languageData
        } else {
            data.updateResources(languageData)
        }
    }

    override fun setString(language: String, key: String, value: String) {
        val data = stringsData[language]
        if (data == null) {
            stringsData[language] = LanguageData(language)
        } else {
            data.resources[key] = value
        }
    }

    override fun getString(language: String, key: String): String? {
        val languageData = stringsData[language]
        return if (languageData == null || !languageData.resources.containsKey(key)) {
            null
        } else {
            languageData.resources[key]
        }
    }

    override fun getLanguageData(language: String): LanguageData? =
            if (!stringsData.containsKey(language)) {
                null
            } else {
                stringsData[language]
            }

    override fun getStringArray(key: String): Array<String>? {
        stringsData[Locale.getDefault().toString()]?.arrays?.forEach { array ->
            if (array.name == key) {
                return array.values
            }
        }

        return null
    }

    override fun getStringPlural(resourceKey: String, quantityKey: String): String? {
        stringsData[Locale.getDefault().toString()]?.plurals?.forEach { pluralData ->
            if (pluralData.name == resourceKey) {
                return pluralData.quantity[quantityKey]
            }
        }
        return null
    }

    override fun isExist(language: String): Boolean {
        return stringsData[language] != null
    }

    override fun getTextData(text: String): SearchResultData {
        val searchResultData = SearchResultData()
        val languageData = stringsData[Locale.getDefault().toString()]


        searchInResources(languageData, text, searchResultData)

        languageData?.arrays?.forEach { arrayData ->
            val arrayName = arrayData.name
            arrayData.values?.forEachIndexed { index, item ->
                if (item == text) {
                    searchResultData.arrayName = arrayName
                    searchResultData.arrayIndex = index
                    return searchResultData
                }
            }
        }

        val languageReserveData = stringsData["${Locale.getDefault().language}-copy"]
        searchInResources(languageReserveData, text, searchResultData)

        return searchResultData
    }

    private fun searchInResources(languageData: LanguageData?, text: String, searchResultData: SearchResultData): SearchResultData {
        languageData?.resources?.forEach {
            if (it.value == text) {
                searchResultData.key = it.key
                return searchResultData
            }
        }
        return searchResultData
    }
}