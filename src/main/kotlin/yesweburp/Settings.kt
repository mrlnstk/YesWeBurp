package yesweburp

import burp.api.montoya.persistence.Preferences
import yesweburp.api.AuthMethod
import kotlin.reflect.KProperty

private val preferences: Preferences get() = burpApi.persistence().preferences()

class PersistentString(private val key: String, private val default: String = "") {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
        preferences.getString(key)?.takeIf(String::isNotEmpty) ?: default

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) =
        preferences.setString(key, value)
}

class PersistentInt(private val key: String, private val default: Int = 0) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int =
        preferences.getInteger(key) ?: default

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) =
        preferences.setInteger(key, value)
}

class PersistentBoolean(private val key: String, private val default: Boolean = false) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
        preferences.getBoolean(key) ?: default

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) =
        preferences.setBoolean(key, value)
}

object Settings {
    var userEmail: String by PersistentString("USER_EMAIL")
    var userPassword: String by PersistentString("USER_PASSWORD")
    var rememberPassword: Boolean by PersistentBoolean("REMEMBER_PASSWORD")
    var authMethod: String by PersistentString("AUTH_METHOD", AuthMethod.ANONYMOUS.label)
    var favoriteTab: Int by PersistentInt("FAVORITE_TAB", 0)
}
