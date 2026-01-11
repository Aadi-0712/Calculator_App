package com.aditya.calculator_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var tvResult: TextView
    private lateinit var tvExpression: TextView

    // Calculator state
    private var currentInput = "0"
    private var previousInput = ""
    private var currentOperator = ""
    private var shouldResetScreen = false
    private var hasDecimal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        tvResult = findViewById(R.id.tvResult)
        tvExpression = findViewById(R.id.tvExpression)

        // Setup all buttons
        setupNumberButtons()
        setupOperatorButtons()
        setupFunctionButtons()

        // Setup back button handling
        setupBackButtonHandler()

        // Initialize display
        updateDisplay()
    }

    private fun setupNumberButtons() {
        // Number buttons 0-9
        val numberButtons = listOf(
            R.id.btnZero, R.id.btnOne, R.id.btnTwo, R.id.btnThree,
            R.id.btnFour, R.id.btnFive, R.id.btnSix, R.id.btnSeven,
            R.id.btnEight, R.id.btnNine
        )

        numberButtons.forEach { buttonId ->
            findViewById<Button>(buttonId).setOnClickListener {
                val number = (it as Button).text.toString()
                appendNumber(number)
            }
        }

        // Decimal button
        findViewById<Button>(R.id.btnDecimal).setOnClickListener {
            addDecimalPoint()
        }
    }

    private fun setupOperatorButtons() {
        findViewById<Button>(R.id.btnPlus).setOnClickListener { setOperator("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { setOperator("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { setOperator("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { setOperator("÷") }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { calculatePercentage() }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculateResult() }
    }

    private fun setupFunctionButtons() {
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnPlusMinus).setOnClickListener { toggleSign() }
    }

    private fun setupBackButtonHandler() {
        // Create a callback for handling back button press
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackButton()
            }
        }

        // Add the callback to the dispatcher
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun handleBackButton() {
        if (currentInput.length > 1 && currentInput != "Error") {
            // Remove last character
            currentInput = currentInput.dropLast(1)

            // If we removed a decimal point
            if (!currentInput.contains(".")) {
                hasDecimal = false
            }

            updateDisplay()
        } else if (currentInput.length == 1 || currentInput == "Error") {
            // Reset to zero
            currentInput = "0"
            hasDecimal = false
            updateDisplay()
        } else {
            // If nothing to delete, let the system handle back press
            finish()
        }
    }

    private fun appendNumber(number: String) {
        if (shouldResetScreen) {
            currentInput = ""
            shouldResetScreen = false
            hasDecimal = false
        }

        if (currentInput == "0" || currentInput == "Error") {
            currentInput = number
        } else {
            currentInput += number
        }

        updateDisplay()
    }

    private fun addDecimalPoint() {
        if (shouldResetScreen) {
            currentInput = "0"
            shouldResetScreen = false
            hasDecimal = false
        }

        if (!hasDecimal) {
            if (currentInput.isEmpty()) {
                currentInput = "0."
            } else {
                currentInput += "."
            }
            hasDecimal = true
            updateDisplay()
        }
    }

    private fun setOperator(operator: String) {
        if (currentInput.isNotEmpty() && currentInput != "Error") {
            if (previousInput.isNotEmpty() && currentOperator.isNotEmpty() && !shouldResetScreen) {
                // Calculate previous operation first
                calculateResult()
            }

            previousInput = currentInput
            currentOperator = operator
            shouldResetScreen = true
            hasDecimal = false

            updateExpression()
        }
    }

    private fun calculateResult() {
        if (previousInput.isNotEmpty() && currentOperator.isNotEmpty() && currentInput.isNotEmpty() && currentInput != "Error") {
            try {
                val num1 = previousInput.toDouble()
                val num2 = currentInput.toDouble()
                var result = 0.0

                when (currentOperator) {
                    "+" -> result = num1 + num2
                    "-" -> result = num1 - num2
                    "×" -> result = num1 * num2
                    "÷" -> {
                        if (num2 == 0.0) {
                            currentInput = "Error"
                            Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show()
                            updateDisplay()
                            return
                        }
                        result = num1 / num2
                    }
                }

                // Format result
                currentInput = if (result % 1 == 0.0) {
                    result.toLong().toString()
                } else {
                    // Remove trailing zeros
                    String.format("%.10f", result).trimEnd('0').trimEnd('.')
                }

                // Reset for next calculation
                previousInput = ""
                currentOperator = ""
                shouldResetScreen = true
                hasDecimal = currentInput.contains(".")

                updateDisplay()
                updateExpression()

            } catch (e: Exception) {
                currentInput = "Error"
                updateDisplay()
            }
        }
    }

    private fun calculatePercentage() {
        if (currentInput.isNotEmpty() && currentInput != "Error") {
            try {
                val number = currentInput.toDouble()
                val result = number / 100

                currentInput = if (result % 1 == 0.0) {
                    result.toLong().toString()
                } else {
                    String.format("%.10f", result).trimEnd('0').trimEnd('.')
                }

                hasDecimal = currentInput.contains(".")
                updateDisplay()
            } catch (e: Exception) {
                currentInput = "Error"
                updateDisplay()
            }
        }
    }

    private fun clearAll() {
        currentInput = "0"
        previousInput = ""
        currentOperator = ""
        shouldResetScreen = false
        hasDecimal = false

        updateDisplay()
        updateExpression()
    }

    private fun toggleSign() {
        if (currentInput.isNotEmpty() && currentInput != "0" && currentInput != "Error") {
            if (currentInput.startsWith("-")) {
                currentInput = currentInput.substring(1)
            } else {
                currentInput = "-$currentInput"
            }
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        tvResult.text = currentInput
    }

    private fun updateExpression() {
        val expression = if (previousInput.isNotEmpty() && currentOperator.isNotEmpty()) {
            "$previousInput $currentOperator"
        } else {
            ""
        }
        tvExpression.text = expression
    }
}