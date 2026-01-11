package com.example.calculator

class Calculator {

    companion object {
        // Validate expression to prevent errors
        fun isValidExpression(expression: String): Boolean {
            if (expression.isEmpty()) return false

            // Check for invalid characters
            val validPattern = Regex("^[0-9+\\-*/.%() ]+$")
            if (!validPattern.matches(expression)) return false

            // Check for consecutive operators
            val operatorPattern = Regex("[+\\-*/.]{2,}")
            if (operatorPattern.containsMatchIn(expression)) return false

            // Check for division by zero
            if (expression.contains("/0") && !expression.contains("/0.")) {
                return false
            }

            return true
        }

        // Calculate expression safely
        fun calculate(expression: String): String {
            return try {
                if (!isValidExpression(expression)) {
                    return "Error"
                }

                // Handle percentage
                var processedExpression = expression.replace("%", "/100")

                // Handle multiplication symbol
                processedExpression = processedExpression.replace("×", "*")

                // Evaluate expression
                val result = evaluateExpression(processedExpression)

                // Format result to remove trailing zeros
                if (result % 1 == 0.0) {
                    result.toLong().toString()
                } else {
                    String.format("%.8f", result).trimEnd('0').trimEnd('.')
                }
            } catch (e: Exception) {
                "Error"
            }
        }

        private fun evaluateExpression(expression: String): Double {
            return object : Any() {
                var pos = -1
                var ch = 0

                fun nextChar() {
                    ch = if (++pos < expression.length) expression[pos].code else -1
                }

                fun eat(charToEat: Int): Boolean {
                    while (ch == ' '.code) nextChar()
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): Double {
                    nextChar()
                    val x = parseExpression()
                    if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                    return x
                }

                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        when {
                            eat('+'.code) -> x += parseTerm()
                            eat('-'.code) -> x -= parseTerm()
                            else -> return x
                        }
                    }
                }

                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        when {
                            eat('*'.code) -> x *= parseFactor()
                            eat('/'.code) -> x /= parseFactor()
                            else -> return x
                        }
                    }
                }

                fun parseFactor(): Double {
                    if (eat('+'.code)) return parseFactor()
                    if (eat('-'.code)) return -parseFactor()

                    var x: Double
                    val startPos = pos
                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                        while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                        x = expression.substring(startPos, pos).toDouble()
                    } else {
                        throw RuntimeException("Unexpected: " + ch.toChar())
                    }

                    return x
                }
            }.parse()
        }
    }
}