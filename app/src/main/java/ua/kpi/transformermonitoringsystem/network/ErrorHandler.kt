package ua.kpi.transformermonitoringsystem.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Централізований обробник помилок мережевих запитів.
 * Підтримує перевірку інтернет-з'єднання, логування у файл error_log.txt,
 * автоматичний механізм повторних спроб для тимчасових збоїв та диференційоване
 * інформування користувача залежно від HTTP-коду помилки.
 */
object ErrorHandler {

    /**
     * Перевірка наявності активного інтернет-з'єднання на пристрої.
     */
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Реєстрація помилок у внутрішній файл error_log.txt у сховищі пристрою.
     */
    fun logErrorToFile(context: Context, message: String) {
        try {
            val file = File(context.filesDir, "error_log.txt")
            FileOutputStream(file, true).bufferedWriter().use { writer ->
                writer.write("${System.currentTimeMillis()}: $message\n")
            }
        } catch (e: Exception) {
            Log.e("ErrorHandler", "Cannot log error to file: ${e.localizedMessage}")
        }
    }

    /**
     * Централізована обробка винятків із перевіркою мережі та HTTP-кодів.
     * Повертає зрозуміле українськомовне повідомлення для відображення користувачеві в UI.
     */
    fun handleException(context: Context, e: Throwable): String {
        val message = when {
            !isInternetAvailable(context) -> "Відсутнє з'єднання з Інтернетом. Перевірте мережу."
            e is UnknownHostException || e is ConnectException -> "Не вдалося з'єднатися з сервером. Перевірте мережу."
            e is SocketTimeoutException -> "Перевищено час очікування відповіді від сервера (Timeout)."
            e is HttpException -> {
                when (e.code()) {
                    400 -> "Некоректний запит до сервера (HTTP 400)."
                    401 -> "Неавторизований доступ (HTTP 401). Перевірте логін та токен."
                    403 -> "Доступ заборонено (HTTP 403). У вас недостатньо прав."
                    404 -> "Запитуваний ресурс або трансформатор не знайдено (HTTP 404)."
                    500 -> "Внутрішня помилка сервера (HTTP 500). Спробуйте пізніше."
                    502, 503, 504 -> "Сервер тимчасово недоступний (HTTP ${e.code()})."
                    else -> "Помилка сервера: HTTP ${e.code()}"
                }
            }
            e is IOException -> "Помилка вводу-виводу при передачі даних по мережі."
            else -> e.localizedMessage ?: "Сталася невідома помилка"
        }
        
        Log.e("ErrorHandler", "Handled exception: $message", e)
        logErrorToFile(context, message)
        return message
    }

    /**
     * Автоматичний механізм повторення запитів при тимчасових мережевих збоях.
     */
    suspend fun <T> retryRequest(times: Int = 3, delayMs: Long = 1500L, block: suspend () -> T): T {
        var currentAttempt = 0
        var lastError: Exception? = null
        while (currentAttempt < times) {
            try {
                return block()
            } catch (e: Exception) {
                lastError = e
                currentAttempt++
                val isTransientError = (e is HttpException && e.code() in listOf(500, 502, 503, 504)) || e is IOException
                if (isTransientError && currentAttempt < times) {
                    delay(delayMs.milliseconds)
                } else {
                    break
                }
            }
        }
        throw lastError ?: Exception("Unknown error during request retry")
    }
}
