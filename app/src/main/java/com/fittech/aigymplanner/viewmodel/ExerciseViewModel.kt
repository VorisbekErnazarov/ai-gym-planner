package com.fittech.aigymplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fittech.aigymplanner.data.AppDatabase
import com.fittech.aigymplanner.data.ExerciseRepository
import com.fittech.aigymplanner.data.RetrofitInstance
import com.fittech.aigymplanner.model.Exercise
import com.fittech.aigymplanner.model.FitnessLevel
import com.fittech.aigymplanner.model.WorkoutDay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ExerciseViewModel
 * Holds all UI state and exposes it as StateFlow.
 * The UI (Compose screens) collects these flows and re-renders when they change.
 */
class ExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExerciseRepository(
        api = RetrofitInstance.api,
        dao = AppDatabase.getDatabase(application).exerciseDao()
    )

    // Full list of exercises from API or mock
    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    // Exercises saved by user in Room
    val savedExercises: StateFlow<List<Exercise>> = repository
        .getSavedExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Selected fitness level - defaults to Beginner
    private val _selectedLevel = MutableStateFlow(FitnessLevel.BEGINNER)
    val selectedLevel: StateFlow<FitnessLevel> = _selectedLevel.asStateFlow()

    // Generated workout plan
    private val _workoutPlan = MutableStateFlow<List<WorkoutDay>>(emptyList())
    val workoutPlan: StateFlow<List<WorkoutDay>> = _workoutPlan.asStateFlow()

    // Muscle group filter for exercise list
    private val _selectedMuscle = MutableStateFlow("all")
    val selectedMuscle: StateFlow<String> = _selectedMuscle.asStateFlow()

    // Filtered exercises (combines full list + muscle filter)
    val filteredExercises: StateFlow<List<Exercise>> = combine(
        _exercises, _selectedMuscle
    ) { list, muscle ->
        if (muscle == "all") list else list.filter { it.muscle == muscle }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadExercises()
    }

    /** Fetch exercises from API (falls back to mock data automatically). */
    fun loadExercises(muscle: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.fetchExercises(muscle = muscle)
                .onSuccess { list ->
                    _exercises.value = list
                    _workoutPlan.value = repository.generateWorkoutPlan(_selectedLevel.value, list)
                }
                .onFailure { e ->
                    _errorMessage.value = "Could not load exercises: ${e.message}"
                }
            _isLoading.value = false
        }
    }

    /** Change fitness level and regenerate the workout plan. */
    fun setFitnessLevel(level: FitnessLevel) {
        _selectedLevel.value = level
        _workoutPlan.value = repository.generateWorkoutPlan(level, _exercises.value)
    }

    fun setMuscleFilter(muscle: String) { _selectedMuscle.value = muscle }

    fun saveExercise(exercise: Exercise) {
        viewModelScope.launch { repository.saveExercise(exercise) }
    }

    fun removeSavedExercise(name: String) {
        viewModelScope.launch { repository.deleteExercise(name) }
    }

    fun clearError() { _errorMessage.value = null }

    fun getMuscleGroups(): List<String> =
        listOf("all") + _exercises.value.mapNotNull { it.muscle }.distinct().sorted()
}
