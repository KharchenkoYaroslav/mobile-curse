package ua.kpi.transformermonitoringsystem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.kpi.transformermonitoringsystem.data.local.Transformer
import ua.kpi.transformermonitoringsystem.data.local.TransformerStatus
import ua.kpi.transformermonitoringsystem.data.TransformerRepository

// ViewModel для управління станом UI та взаємодії з бізнес-логікою (MVVM)
class TransformerViewModel(private val repository: TransformerRepository) : ViewModel() {

    // Локальний список для пошуку та фільтрації без повторних запитів до БД
    private var allTransformers = emptyList<Transformer>()

    // StateFlow для реактивного відображення списку в UI
    private val _transformers = MutableStateFlow<List<Transformer>>(emptyList())
    val transformers: StateFlow<List<Transformer>> = _transformers.asStateFlow()

    // Стан пошукового запиту
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Стан напрямку сортування (true = А-Я, false = Я-А)
    private val _isAscending = MutableStateFlow(true)
    val isAscending: StateFlow<Boolean> = _isAscending.asStateFlow()

    // ID обраного трансформатора
    private val _selectedTransformerId = MutableStateFlow<Int?>(null)

    // Об'єкт обраного трансформатора для детального перегляду
    private val _selectedTransformer = MutableStateFlow<Transformer?>(null)
    val selectedTransformer: StateFlow<Transformer?> = _selectedTransformer.asStateFlow()

    // StateFlow для списку статусів обраного трансформатора
    private val _statuses = MutableStateFlow<List<TransformerStatus>>(emptyList())
    val statuses: StateFlow<List<TransformerStatus>> = _statuses.asStateFlow()

    // Агреговані показники (з БД)
    private val _avgTemperature = MutableStateFlow(0.0)
    val avgTemperature: StateFlow<Double> = _avgTemperature.asStateFlow()

    private val _avgLoad = MutableStateFlow(0.0)
    val avgLoad: StateFlow<Double> = _avgLoad.asStateFlow()

    // Стан завантаження (індикатор прогресу)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Стан успішного виконання операції (додавання/видалення)
    private val _isSubmitted = MutableStateFlow(false)
    val isSubmitted: StateFlow<Boolean> = _isSubmitted.asStateFlow()

    // Повідомлення про помилки
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Завантаження даних у фоновому потоці через viewModelScope
    fun loadTransformers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Отримуємо дані через Repository
                allTransformers = repository.getAllTransformers(forceRefresh)
                applyFilters()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Оновлення тексту пошуку
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    // Зміна порядку сортування
    fun toggleSortOrder() {
        _isAscending.value = !_isAscending.value
        applyFilters()
    }

    // Локальна фільтрація та сортування
    private fun applyFilters() {
        var filtered = allTransformers.filter {
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

    // Вибір трансформатора та завантаження його деталей
    fun selectTransformer(id: Int) {
        _selectedTransformerId.value = id
        _selectedTransformer.value = allTransformers.find { it.id == id }
        loadStatuses(id)
    }

    // Завантаження статусів та агрегованих показників
    fun loadStatuses(transformerId: Int) {
        viewModelScope.launch {
            try {
                _statuses.value = repository.getStatuses(transformerId)
                _avgTemperature.value = repository.getAverageTemperature(transformerId)
                _avgLoad.value = repository.getAverageLoad(transformerId)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Збереження нового трансформатора у БД
    fun addTransformer(name: String, location: String, voltageStr: String, tempStr: String, loadStr: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSubmitted.value = false
            
            try {
                // Додаткова перевірка бізнес-логіки
                if (name.length < 3) throw Exception("Назва має містити мінімум 3 символи")
                if (location.isBlank()) throw Exception("Локація не може бути порожньою")
                
                val v = voltageStr.toDoubleOrNull() ?: throw Exception("Введіть коректну напругу (число)")
                val t = tempStr.toDoubleOrNull() ?: throw Exception("Введіть коректну температуру (число)")
                val l = loadStr.toDoubleOrNull() ?: throw Exception("Введіть коректне навантаження (число)")

                if (v <= 0) throw Exception("Напруга має бути більшою за 0")
                if (t !in -50.0..150.0) throw Exception("Температура має бути в межах від -50 до 150 °C")
                if (l !in 0.0..150.0) throw Exception("Навантаження має бути в межах від 0 до 150 %")

                val transformer = Transformer(name = name, location = location)
                val status = TransformerStatus(voltage = v, temperature = t, load = l, transformerId = 0)

                // Виклик репозиторію
                repository.insertTransformerWithData(transformer, listOf(status))
                
                _isSubmitted.value = true
                loadTransformers(forceRefresh = true)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Додавання статусу для вже обраного трансформатора
    fun addStatusForSelectedTransformer(voltageStr: String, tempStr: String, loadStr: String) {
        val transformerId = _selectedTransformerId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val v = voltageStr.toDoubleOrNull() ?: throw Exception("Введіть коректну напругу (число)")
                val t = tempStr.toDoubleOrNull() ?: throw Exception("Введіть коректну температуру (число)")
                val l = loadStr.toDoubleOrNull() ?: throw Exception("Введіть коректне навантаження (число)")

                if (v <= 0) throw Exception("Напруга має бути більшою за 0")
                if (t !in -50.0..150.0) throw Exception("Температура має бути в межах від -50 до 150 °C")
                if (l !in 0.0..150.0) throw Exception("Навантаження має бути в межах від 0 до 150 %")

                val status = TransformerStatus(voltage = v, temperature = t, load = l, transformerId = transformerId)
                repository.addStatus(status)
                loadStatuses(transformerId)
                _isSubmitted.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Видалення конкретного статусу
    fun deleteStatus(status: TransformerStatus) {
        viewModelScope.launch {
            try {
                repository.deleteStatus(status)
                val id = _selectedTransformerId.value ?: return@launch
                loadStatuses(id)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // Оновлення обраного трансформатора
    fun updateSelectedTransformer(name: String, location: String) {
        val transformer = _selectedTransformer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSubmitted.value = false
            try {
                if (name.length < 3) throw Exception("Назва має містити мінімум 3 символи")
                if (location.isBlank()) throw Exception("Локація не може бути порожньою")

                val updatedTransformer = transformer.copy(name = name, location = location)
                repository.updateTransformer(updatedTransformer)
                
                _selectedTransformer.value = updatedTransformer
                _isSubmitted.value = true
                loadTransformers(forceRefresh = true)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Видалення обраного трансформатора
    fun deleteSelectedTransformer() {
        val transformer = _selectedTransformer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.deleteTransformer(transformer)
                loadTransformers(forceRefresh = true)
                _isSubmitted.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Скидання стану після успішної навігації
    fun resetSubmitState() {
        _isSubmitted.value = false
    }

    // Очищення повідомлення про помилку
    fun clearError() {
        _errorMessage.value = null
    }
}

// Фабрика для ін'єкції залежностей у ViewModel
class TransformerViewModelFactory(private val repository: TransformerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransformerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransformerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
