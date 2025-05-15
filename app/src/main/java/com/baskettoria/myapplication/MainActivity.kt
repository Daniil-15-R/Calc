package com.baskettoria.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    private lateinit var inputField: EditText
    private lateinit var resultField: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var btnHistory: Button

    private var currentInput = ""
    private var currentResult = ""
    private var lastOperation = ""
    private var isHistoryVisible = false
    private val historyItems = mutableListOf<HistoryItem>()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputField = findViewById(R.id.inputField)
        resultField = findViewById(R.id.resultField)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        btnHistory = findViewById(R.id.btnHistory)

        // Инициализация истории
        historyAdapter = HistoryAdapter(historyItems) { item ->
            inputField.setText(item.result)
            currentInput = item.result
            resultField.text = item.calculation
            toggleHistoryVisibility()
        }

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        btnHistory.setOnClickListener {
            toggleHistoryVisibility()
        }

        // Цифровые кнопки
        setNumberButtonClickListener(R.id.btn0, "0")
        setNumberButtonClickListener(R.id.btn1, "1")
        setNumberButtonClickListener(R.id.btn2, "2")
        setNumberButtonClickListener(R.id.btn3, "3")
        setNumberButtonClickListener(R.id.btn4, "4")
        setNumberButtonClickListener(R.id.btn5, "5")
        setNumberButtonClickListener(R.id.btn6, "6")
        setNumberButtonClickListener(R.id.btn7, "7")
        setNumberButtonClickListener(R.id.btn8, "8")
        setNumberButtonClickListener(R.id.btn9, "9")
        setNumberButtonClickListener(R.id.btnDecimal, ".")

        // Основные операции
        setOperationButtonClickListener(R.id.btnPlus, "+")
        setOperationButtonClickListener(R.id.btnMinus, "-")
        setOperationButtonClickListener(R.id.btnMultiply, "×")
        setOperationButtonClickListener(R.id.btnDivide, "÷")
        setOperationButtonClickListener(R.id.btnPercent, "%")

        // Инженерные функции
        setFunctionButtonClickListener(R.id.btnSin, "sin")
        setFunctionButtonClickListener(R.id.btnCos, "cos")
        setFunctionButtonClickListener(R.id.btnTan, "tan")
        setFunctionButtonClickListener(R.id.btnLn, "ln")
        setFunctionButtonClickListener(R.id.btnLog, "log")
        setFunctionButtonClickListener(R.id.btnSqrt, "√")
        setFunctionButtonClickListener(R.id.btnPower, "^")
        setFunctionButtonClickListener(R.id.btnFact, "!")
        setFunctionButtonClickListener(R.id.btnInverse, "1/")
        setFunctionButtonClickListener(R.id.btnExp, "E")

        // Константы
        setConstantButtonClickListener(R.id.btnPi, "π", PI.toString())
        setConstantButtonClickListener(R.id.btnE, "e", E.toString())

        // Скобки
        setBracketButtonClickListener(R.id.btnOpenBracket, "(")
        setBracketButtonClickListener(R.id.btnCloseBracket, ")")

        // Управление
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { backspace() }
        findViewById<Button>(R.id.btnSign).setOnClickListener { changeSign() }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculateResult() }
    }

    private fun toggleHistoryVisibility() {
        isHistoryVisible = !isHistoryVisible
        historyRecyclerView.visibility = if (isHistoryVisible) View.VISIBLE else View.GONE
        btnHistory.text = if (isHistoryVisible) "Калькулятор" else "История"
    }

    private fun addToHistory(calculation: String, result: String) {
        val newItem = HistoryItem(calculation, result)
        historyItems.add(0, newItem)
        historyAdapter.notifyItemInserted(0)

        if (historyItems.size > 50) {
            historyItems.removeAt(historyItems.size - 1)
            historyAdapter.notifyItemRemoved(historyItems.size)
        }
    }

    private fun setNumberButtonClickListener(buttonId: Int, value: String) {
        findViewById<Button>(buttonId).setOnClickListener {
            if (isHistoryVisible) toggleHistoryVisibility()

            if (currentInput == "0" && value != ".") {
                currentInput = value
            } else {
                currentInput += value
            }
            updateInputField()
        }
    }

    private fun setOperationButtonClickListener(buttonId: Int, operation: String) {
        findViewById<Button>(buttonId).setOnClickListener {
            if (isHistoryVisible) toggleHistoryVisibility()

            if (currentInput.isNotEmpty()) {
                currentResult = currentInput
                currentInput = ""
                lastOperation = operation
                updateResultField()
            } else if (currentResult.isNotEmpty()) {
                lastOperation = operation
            }
        }
    }

    private fun setFunctionButtonClickListener(buttonId: Int, function: String) {
        findViewById<Button>(buttonId).setOnClickListener {
            if (isHistoryVisible) toggleHistoryVisibility()

            try {
                val value = if (currentInput.isNotEmpty()) currentInput.toDouble()
                else if (currentResult.isNotEmpty()) currentResult.toDouble()
                else 0.0
                val result = when (function) {
                    "sin" -> sin(Math.toRadians(value))
                    "cos" -> cos(Math.toRadians(value))
                    "tan" -> tan(Math.toRadians(value))
                    "ln" -> ln(value)
                    "log" -> log10(value)
                    "√" -> sqrt(value)
                    "!" -> factorial(value.toInt()).toDouble()
                    "1/" -> 1 / value
                    "E" -> value * 10.0.pow(if (currentInput.isNotEmpty()) currentInput.toDouble() else 0.0)
                    else -> value
                }

                val calculation = when (function) {
                    "1/" -> "1/($currentInput)"
                    else -> "$function(${if (currentInput.isEmpty()) currentResult else currentInput})"
                }

                addToHistory(calculation, formatResult(result))
                currentInput = formatResult(result)
                updateInputField()
            } catch (e: Exception) {
                currentInput = "Error"
                updateInputField()
            }
        }
    }

    private fun setConstantButtonClickListener(buttonId: Int, constant: String, value: String) {
        findViewById<Button>(buttonId).setOnClickListener {
            if (isHistoryVisible) toggleHistoryVisibility()
            currentInput = value
            addToHistory(constant, value)
            updateInputField()
        }
    }

    private fun setBracketButtonClickListener(buttonId: Int, bracket: String) {
        findViewById<Button>(buttonId).setOnClickListener {
            if (isHistoryVisible) toggleHistoryVisibility()
            currentInput += bracket
            updateInputField()
        }
    }

    private fun calculateResult() {
        if (isHistoryVisible) {
            toggleHistoryVisibility()
            return
        }

        if (currentInput.isNotEmpty() && currentResult.isNotEmpty() && lastOperation.isNotEmpty()) {
            try {
                val num1 = currentResult.toDouble()
                val num2 = currentInput.toDouble()
                val calculation = "$currentResult $lastOperation $currentInput"

                val result = when (lastOperation) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "×" -> num1 * num2
                    "÷" -> num1 / num2
                    "%" -> num1 % num2
                    "^" -> num1.pow(num2)
                    else -> num2
                }

                val formattedResult = formatResult(result)
                addToHistory(calculation, formattedResult)
                currentInput = formattedResult
                currentResult = ""
                lastOperation = ""

                updateInputField()
                updateResultField()
            } catch (e: Exception) {
                currentInput = "Error"
                updateInputField()
            }
        }
    }

    private fun clearAll() {
        if (isHistoryVisible) {
            historyItems.clear()
            historyAdapter.notifyDataSetChanged()
            return
        }

        currentInput = ""
        currentResult = ""
        lastOperation = ""
        updateInputField()
        updateResultField()
    }

    private fun backspace() {
        if (isHistoryVisible) return

        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            if (currentInput.isEmpty()) currentInput = "0"
            updateInputField()
        }
    }

    private fun changeSign() {
        if (isHistoryVisible) return

        if (currentInput.isNotEmpty() && currentInput != "0") {
            currentInput = if (currentInput.startsWith("-")) {
                currentInput.substring(1)
            } else {
                "-$currentInput"
            }
            updateInputField()
        }
    }

    private fun updateInputField() {
        inputField.setText(currentInput)
    }

    private fun updateResultField() {
        resultField.text = if (currentResult.isNotEmpty()) "$currentResult $lastOperation" else ""
    }

    private fun formatResult(value: Double): String {
        val df = DecimalFormat("#.##########")
        return df.format(value).replace(",", ".")
    }

    private fun factorial(n: Int): Int {
        return if (n <= 1) 1 else n * factorial(n - 1)
    }
}

data class HistoryItem(
    val calculation: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HistoryAdapter(
    private val items: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val calculation: TextView = view.findViewById(R.id.tvCalculation)
        val result: TextView = view.findViewById(R.id.tvResult)
        val timestamp: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.calculation.text = item.calculation
        holder.result.text = "= ${item.result}"
        holder.timestamp.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp))
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}