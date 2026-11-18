package ua.kpi.transformermonitoringsystem.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import ua.kpi.transformermonitoringsystem.data.TransformerRepository
import ua.kpi.transformermonitoringsystem.data.NetworkStats
import ua.kpi.transformermonitoringsystem.database.entity.Transformer
import ua.kpi.transformermonitoringsystem.database.entity.TransformerStatus
import ua.kpi.transformermonitoringsystem.network.ErrorHandler

/**
 * ViewModel для керування станом моніторингу трансформаторів.
 */
@HiltViewModel
class TransformerViewModel @Inject constructor(
    private val repository: TransformerRepository,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val appContext = context.applicationContext

    // Список трансформаторів після сортування та фільтрації
    private val _transformers = MutableStateFlow<List<Transformer>>(emptyList())
    val transformers: StateFlow<List<Transformer>> = _transformers.asStateFlow()

    // Агрегована статистика мережі
    private val _stats = MutableStateFlow<NetworkStats?>(null)
    val stats: StateFlow<NetworkStats?> = _stats.asStateFlow()

    // Стан помилки для інформування користувача в UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Стан завантаження (прогрес-бар)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Стан успішного виконання форми або дії
    private val _isSubmitted = MutableStateFlow(false)
    val isSubmitted: StateFlow<Boolean> = _isSubmitted.asStateFlow()

    // Пошуковий запит
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Порядок сортування (за зростанням чи спаданням)
    private val _isAscending = MutableStateFlow(true)
    val isAscending: StateFlow<Boolean> = _isAscending.asStateFlow()

    // Обраний трансформатор та його ідентифікатор
    private val _selectedTransformerId = MutableStateFlow<Int?>(null)
    private val _selectedTransformer = MutableStateFlow<Transformer?>(null)
    val selectedTransformer: StateFlow<Transformer?> = _selectedTransformer.asStateFlow()

    // Історія статусів обраного трансформатора
    private val _statuses = MutableStateFlow<List<TransformerStatus>>(emptyList())
    val statuses: StateFlow<List<TransformerStatus>> = _statuses.asStateFlow()

    // Середня температура та навантаження
    private val _avgTemperature = MutableStateFlow(0.0)
    val avgTemperature: StateFlow<Double> = _avgTemperature.asStateFlow()

    private val _avgLoad = MutableStateFlow(0.0)
    val avgLoad: StateFlow<Double> = _avgLoad.asStateFlow()

    private var allTransformerEntities = emptyList<Transformer>()

    private var loadDataJob: Job? = null
    private var selectTransformerJob: Job? = null
    private var loadStatusesJob: Job? = null

    fun loadData(forceRefresh: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            val localData = if (!forceRefresh) {
                repository.getAllTransformers(localOnly = true).getOrDefault(emptyList())
            } else {
                emptyList()
            }

            if (localData.isNotEmpty()) {
                allTransformerEntities = localData
                applyFilters()
                _stats.value = repository.getNetworkStats(localData, localOnly = true).getOrNull()
                _isLoading.value = false

                launch {
                    val result = repository.getAllTransformers(forceRefresh = true)
                    result.onSuccess { freshData ->
                        allTransformerEntities = freshData
                        applyFilters()
                        _stats.value = repository.getNetworkStats(freshData).getOrNull()
                    }
                }
            } else {
                _isLoading.value = true
                val result = repository.getAllTransformers(forceRefresh = true)
                result.onSuccess { data ->
                    allTransformerEntities = data
                    applyFilters()
                    _stats.value = repository.getNetworkStats(data).getOrNull()
                }.onFailure { error ->
                    val fallbackData = repository.getAllTransformers(localOnly = true).getOrDefault(emptyList())
                    if (fallbackData.isNotEmpty()) {
                        allTransformerEntities = fallbackData
                        applyFilters()
                        _stats.value = repository.getNetworkStats(fallbackData, localOnly = true).getOrNull()
                    } else {
                        _errorMessage.value = ErrorHandler.handleException(appContext, error)
                    }
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Встановлення пошукового рядка та застосування фільтрації.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    /**
     * Перемикання напрямку сортування за назвою.
     */
    fun toggleSortOrder() {
        _isAscending.value = !_isAscending.value
        applyFilters()
    }

    /**
     * Фільтрація та сортування у пам'яті.
     */
    private fun applyFilters() {
        var filtered = allTransformerEntities.filter {
            it.name.contains(_searchQuery.value, ignoreCase = true) ||
                    it.location.contains(_searchQuery.value, ignoreCase = true)
        }
        filtered = if (_isAscending.value) {
            filtered.sortedBy { it.name }
        } else {
            filtered.sortedByDescending { it.name }
        }
        _transformers.value = filtered
    }

    /**
     * Вибір трансформатора за ID та завантаження його телеметрії.
     */
    fun selectTransformer(id: Int) {
        _errorMessage.value = null
        _selectedTransformerId.value = id
        _selectedTransformer.value = allTransformerEntities.find { it.id == id }

        selectTransformerJob?.cancel()
        selectTransformerJob = viewModelScope.launch {
            if (_selectedTransformer.value == null) {
                repository.getTransformerById(id, localOnly = true).onSuccess { cached ->
                    _selectedTransformer.value = cached
                }
            }

            repository.getTransformerById(id)
                .onSuccess { fresh ->
                    _selectedTransformer.value = fresh
                }
                .onFailure { e ->
                    if (_selectedTransformer.value == null) {
                        _errorMessage.value = ErrorHandler.handleException(appContext, e)
                    }
                }
        }
        loadStatuses(id)
    }

    /**
     * Завантаження історії статусів та SQL-агрегованих показників.
     */
    fun loadStatuses(transformerId: Int) {
        loadStatusesJob?.cancel()
        loadStatusesJob = viewModelScope.launch {
            val cachedStatuses = repository.getStatuses(transformerId, localOnly = true).getOrDefault(emptyList())
            if (cachedStatuses.isNotEmpty()) {
                _statuses.value = cachedStatuses
                _avgTemperature.value = repository.getAverageTemperature(transformerId).getOrDefault(0.0)
                _avgLoad.value = repository.getAverageLoad(transformerId).getOrDefault(0.0)
            } else {
                _statuses.value = emptyList()
                val currentNode = _selectedTransformer.value
                    ?: repository.getTransformerById(transformerId, localOnly = true).getOrNull()
                _avgTemperature.value = currentNode?.temperature ?: 0.0
                _avgLoad.value = currentNode?.currentLoad ?: 0.0
            }

            try {
                repository.getStatuses(transformerId)
                    .onSuccess { freshStatuses ->
                        _statuses.value = freshStatuses
                        if (freshStatuses.isNotEmpty()) {
                            _avgTemperature.value = repository.getAverageTemperature(transformerId).getOrDefault(0.0)
                            _avgLoad.value = repository.getAverageLoad(transformerId).getOrDefault(0.0)
                        }
                    }
                    .onFailure { e ->
                        if (cachedStatuses.isEmpty() && _selectedTransformer.value == null) {
                            _errorMessage.value = ErrorHandler.handleException(appContext, e)
                        }
                    }
            } catch (e: Exception) {
                if (cachedStatuses.isEmpty() && _selectedTransformer.value == null) {
                    _errorMessage.value = ErrorHandler.handleException(appContext, e)
                }
            }
        }
    }

    /**
     * Додавання нового трансформатора з валідацією полів та підтримкою maxLoad.
     */
    fun addTransformer(
        name: String,
        location: String,
        voltageStr: String,
        tempStr: String,
        loadStr: String,
        maxLoadStr: String = "100.0"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSubmitted.value = false

            try {
                if (name.trim().length < 3) throw Exception("Назва має містити мінімум 3 символи")
                if (location.isBlank()) throw Exception("Локація не може бути порожньою")

                val v = voltageStr.toDoubleOrNull() ?: throw Exception("Введіть коректну напругу (число)")
                val t = tempStr.toDoubleOrNull() ?: throw Exception("Введіть коректну температуру (число)")
                val l = loadStr.toDoubleOrNull() ?: throw Exception("Введіть коректне навантаження (число)")
                val maxL = maxLoadStr.toDoubleOrNull() ?: throw Exception("Введіть коректне макс. навантаження (число)")

                if (v <= 0) throw Exception("Напруга має бути більшою за 0 В")
                if (t !in -50.0..150.0) throw Exception("Температура має бути в межах від -50 до 150 °C")
                if (l < 0.0) throw Exception("Поточне навантаження не може бути від'ємним")
                if (maxL <= 0.0) throw Exception("Максимальне навантаження має бути більшим за 0")

                val transformer = Transformer(
                    name = name.trim(),
                    location = location.trim(),
                    currentLoad = l,
                    temperature = t,
                    maxLoad = maxL
                )
                val status = TransformerStatus(voltage = v, temperature = t, load = l, transformerId = 0)

                repository.insertTransformerWithData(transformer, listOf(status))
                    .onSuccess {
                        _isSubmitted.value = true
                        loadData(forceRefresh = true)
                    }
                    .onFailure { throw it }
            } catch (e: Exception) {
                _errorMessage.value = ErrorHandler.handleException(appContext, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Додавання нового статусу для обраного трансформатора.
     */
    fun addStatusForSelectedTransformer(voltageStr: String, tempStr: String, loadStr: String) {
        val transformerId = _selectedTransformerId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val v = voltageStr.toDoubleOrNull() ?: throw Exception("Введіть коректну напругу (число)")
                val t = tempStr.toDoubleOrNull() ?: throw Exception("Введіть коректну температуру (число)")
                val l = loadStr.toDoubleOrNull() ?: throw Exception("Введіть коректне навантаження (число)")

                if (v <= 0) throw Exception("Напруга має бути більшою за 0 В")
                if (t !in -50.0..150.0) throw Exception("Температура має бути в межах від -50 до 150 °C")
                if (l < 0.0) throw Exception("Навантаження не може бути від'ємним")

                val status = TransformerStatus(voltage = v, temperature = t, load = l, transformerId = transformerId)
                repository.addStatus(status)
                    .onSuccess {
                        repository.getTransformerById(transformerId).onSuccess { updated ->
                            _selectedTransformer.value = updated
                        }
                        loadStatuses(transformerId)
                        loadData(forceRefresh = true)
                        _isSubmitted.value = true
                    }
                    .onFailure { throw it }
            } catch (e: Exception) {
                _errorMessage.value = ErrorHandler.handleException(appContext, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Видалення показника телеметрії.
     */
    fun deleteStatus(status: TransformerStatus) {
        viewModelScope.launch {
            repository.deleteStatus(status)
                .onSuccess {
                    val id = _selectedTransformerId.value ?: return@onSuccess
                    loadStatuses(id)
                }
                .onFailure { e ->
                    _errorMessage.value = ErrorHandler.handleException(appContext, e)
                }
        }
    }

    /**
     * Оновлення обраного трансформатора з підтримкою maxLoad.
     */
    fun updateSelectedTransformer(name: String, location: String, maxLoadStr: String) {
        val transformer = _selectedTransformer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSubmitted.value = false
            try {
                if (name.trim().length < 3) throw Exception("Назва має містити мінімум 3 символи")
                if (location.isBlank()) throw Exception("Локація не може бути порожньою")
                val maxL = maxLoadStr.toDoubleOrNull() ?: throw Exception("Введіть коректне макс. навантаження")
                if (maxL <= 0.0) throw Exception("Максимальне навантаження має бути більшим за 0")

                val updatedTransformer = transformer.copy(
                    name = name.trim(),
                    location = location.trim(),
                    maxLoad = maxL
                )
                repository.updateTransformer(updatedTransformer)
                    .onSuccess { saved ->
                        _selectedTransformer.value = saved
                        _isSubmitted.value = true
                        loadData(forceRefresh = true)
                    }
                    .onFailure { throw it }
            } catch (e: Exception) {
                _errorMessage.value = ErrorHandler.handleException(appContext, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Видалення обраного трансформатора.
     */
    fun deleteSelectedTransformer() {
        val transformer = _selectedTransformer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.deleteTransformer(transformer)
                loadData(forceRefresh = true)
                _isSubmitted.value = true
            } catch (e: Exception) {
                _errorMessage.value = ErrorHandler.handleException(appContext, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSubmitState() { _isSubmitted.value = false }
    fun clearError() { _errorMessage.value = null }
}