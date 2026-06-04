package com.aistudio.constructpro.kgrmqd.ui

import java.io.File

/**
 * One-shot UI events emitted by ViewModels and collected in the Activity/Composable.
 * This prevents Context leaks from ViewModel and ensures events survive configuration changes.
 */
sealed class UiEvent {
    data class ShowToast(val message: String, val long: Boolean = false) : UiEvent()
    data class ShareFile(val file: File, val mimeType: String, val title: String) : UiEvent()
    data class ShowError(val message: String, val throwable: Throwable? = null) : UiEvent()
    data object ImportSuccess : UiEvent()
    data object ExportSuccess : UiEvent()
}

/**
 * Represents the progress state of a long-running operation like JSON import/export.
 */
data class OperationProgress(
    val isActive: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0
    val message: String = ""
)

/**
 * Validation result for form fields.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Form validation utility for all entity forms.
 */
object FormValidator {

    fun validateProjectName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Project name is required")
            name.length < 2 -> ValidationResult(false, "Name must be at least 2 characters")
            name.length > 100 -> ValidationResult(false, "Name must be under 100 characters")
            else -> ValidationResult(true)
        }
    }

    fun validateLocation(location: String): ValidationResult {
        return when {
            location.isBlank() -> ValidationResult(false, "Location is required")
            else -> ValidationResult(true)
        }
    }

    fun validateBudget(budgetStr: String): ValidationResult {
        val budget = budgetStr.toDoubleOrNull()
        return when {
            budgetStr.isBlank() -> ValidationResult(false, "Budget is required")
            budget == null -> ValidationResult(false, "Enter a valid number")
            budget < 0 -> ValidationResult(false, "Budget cannot be negative")
            else -> ValidationResult(true)
        }
    }

    fun validateAmount(amountStr: String): ValidationResult {
        val amount = amountStr.toDoubleOrNull()
        return when {
            amountStr.isBlank() -> ValidationResult(false, "Amount is required")
            amount == null -> ValidationResult(false, "Enter a valid number")
            amount <= 0 -> ValidationResult(false, "Amount must be greater than 0")
            else -> ValidationResult(true)
        }
    }

    fun validateWorkerName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Name is required")
            name.length < 2 -> ValidationResult(false, "Name must be at least 2 characters")
            else -> ValidationResult(true)
        }
    }

    fun validateTaskTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult(false, "Task title is required")
            title.length < 3 -> ValidationResult(false, "Title must be at least 3 characters")
            else -> ValidationResult(true)
        }
    }

    fun validateDate(dateStr: String): ValidationResult {
        return when {
            dateStr.isBlank() -> ValidationResult(false, "Date is required")
            !dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> ValidationResult(false, "Use format YYYY-MM-DD")
            else -> ValidationResult(true)
        }
    }

    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) return ValidationResult(true) // optional
        return when {
            phone.length < 10 -> ValidationResult(false, "Phone must be at least 10 digits")
            !phone.matches(Regex("[+]?[0-9\\s-]+")) -> ValidationResult(false, "Invalid phone format")
            else -> ValidationResult(true)
        }
    }

    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult(true) // optional
        return when {
            !email.contains("@") || !email.contains(".") -> ValidationResult(false, "Invalid email format")
            else -> ValidationResult(true)
        }
    }

    fun validateDescription(desc: String): ValidationResult {
        return when {
            desc.isBlank() -> ValidationResult(false, "Description is required")
            else -> ValidationResult(true)
        }
    }

    fun validateAadhaar(aadhaar: String): ValidationResult {
        if (aadhaar.isBlank()) return ValidationResult(true) // optional
        val cleaned = aadhaar.replace(" ", "").replace("-", "")
        return when {
            cleaned.length != 12 -> ValidationResult(false, "Aadhaar must be 12 digits")
            !cleaned.all { it.isDigit() } -> ValidationResult(false, "Aadhaar must contain only digits")
            else -> ValidationResult(true)
        }
    }

    fun validatePan(pan: String): ValidationResult {
        if (pan.isBlank()) return ValidationResult(true) // optional
        return when {
            pan.length != 10 -> ValidationResult(false, "PAN must be 10 characters")
            !pan.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]")) -> ValidationResult(false, "Invalid PAN format (e.g. ABCDE1234F)")
            else -> ValidationResult(true)
        }
    }
}
