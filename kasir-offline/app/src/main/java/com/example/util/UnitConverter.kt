package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode

enum class UnitType {
    WEIGHT,
    VOLUME,
    LENGTH,
    DISCRETE
}

object UnitConverter {
    val ALL_UNITS = listOf(
        "kg", "gram", "ons",
        "liter", "ml",
        "meter", "cm",
        "pcs", "buah", "pack", "box", "botol"
    )

    fun getUnitType(unit: String): UnitType {
        return when (unit.lowercase().trim()) {
            "kg", "kilogram", "gram", "g", "ons" -> UnitType.WEIGHT
            "liter", "l", "ml" -> UnitType.VOLUME
            "meter", "m", "cm" -> UnitType.LENGTH
            else -> UnitType.DISCRETE
        }
    }

    /**
     * Standardizes unit label (e.g. "g" -> "gram", "l" -> "liter")
     */
    fun normalizeUnit(unit: String): String {
        return when (unit.lowercase().trim()) {
            "kilogram" -> "kg"
            "g" -> "gram"
            "l" -> "liter"
            "m" -> "meter"
            else -> unit.lowercase().trim()
        }
    }

    /**
     * Returns alternate convertible units for the given base unit.
     */
    fun getConvertibleUnits(baseUnit: String): List<String> {
        return when (getUnitType(baseUnit)) {
            UnitType.WEIGHT -> listOf("kg", "gram", "ons")
            UnitType.VOLUME -> listOf("liter", "ml")
            UnitType.LENGTH -> listOf("meter", "cm")
            UnitType.DISCRETE -> listOf(normalizeUnit(baseUnit))
        }
    }

    /**
     * Converts a quantity in inputUnit to baseUnit.
     * E.g. 250 gram with baseUnit "kg" -> 0.25 kg
     * E.g. 1.5 ons with baseUnit "kg" -> 0.15 kg
     * E.g. 500 ml with baseUnit "liter" -> 0.5 liter
     * E.g. 50 cm with baseUnit "meter" -> 0.5 meter
     */
    fun convertToBaseUnitQty(qty: Double, inputUnit: String, baseUnit: String): Double {
        val inNorm = normalizeUnit(inputUnit)
        val baseNorm = normalizeUnit(baseUnit)

        if (inNorm == baseNorm) return qty

        val inBigDecimal = BigDecimal.valueOf(qty)
        val result = when (baseNorm) {
            "kg" -> when (inNorm) {
                "gram" -> inBigDecimal.divide(BigDecimal(1000), 6, RoundingMode.HALF_UP)
                "ons" -> inBigDecimal.divide(BigDecimal(10), 6, RoundingMode.HALF_UP) // 1 kg = 10 ons = 1000 g
                else -> inBigDecimal
            }
            "gram" -> when (inNorm) {
                "kg" -> inBigDecimal.multiply(BigDecimal(1000))
                "ons" -> inBigDecimal.multiply(BigDecimal(100))
                else -> inBigDecimal
            }
            "ons" -> when (inNorm) {
                "kg" -> inBigDecimal.multiply(BigDecimal(10))
                "gram" -> inBigDecimal.divide(BigDecimal(100), 6, RoundingMode.HALF_UP)
                else -> inBigDecimal
            }
            "liter" -> when (inNorm) {
                "ml" -> inBigDecimal.divide(BigDecimal(1000), 6, RoundingMode.HALF_UP)
                else -> inBigDecimal
            }
            "ml" -> when (inNorm) {
                "liter" -> inBigDecimal.multiply(BigDecimal(1000))
                else -> inBigDecimal
            }
            "meter" -> when (inNorm) {
                "cm" -> inBigDecimal.divide(BigDecimal(100), 6, RoundingMode.HALF_UP)
                else -> inBigDecimal
            }
            "cm" -> when (inNorm) {
                "meter" -> inBigDecimal.multiply(BigDecimal(100))
                else -> inBigDecimal
            }
            else -> inBigDecimal
        }
        return result.toDouble()
    }

    /**
     * Calculates the subtotal in a decimal-safe manner.
     * basePrice is the price per baseUnit.
     * inputQty is the quantity entered by the cashier.
     * inputUnit is the unit entered (e.g. gram).
     * baseUnit is the product's defined unit (e.g. kg).
     *
     * Example 1:
     * basePrice = 15,000 / kg
     * inputQty = 0.5, inputUnit = kg, baseUnit = kg
     * Result = 7,500
     *
     * Example 2:
     * basePrice = 40,000 / kg
     * inputQty = 250, inputUnit = gram, baseUnit = kg
     * Qty in base = 0.25 kg -> 0.25 * 40,000 = 10,000
     *
     * Example 3:
     * basePrice = 25,000 / kg
     * inputQty = 350, inputUnit = gram, baseUnit = kg
     * 350 * (25,000 / 1000 = 25) = 8,750
     */
    fun calculateItemTotal(
        basePrice: Double,
        inputQty: Double,
        inputUnit: String,
        baseUnit: String,
        discountPerItem: Double = 0.0
    ): Double {
        if (inputQty <= 0.0 || basePrice <= 0.0) return 0.0

        val baseQty = convertToBaseUnitQty(inputQty, inputUnit, baseUnit)
        val priceBD = BigDecimal.valueOf(basePrice)
        val qtyBD = BigDecimal.valueOf(baseQty)
        val discountBD = BigDecimal.valueOf(discountPerItem)

        val gross = priceBD.multiply(qtyBD)
        val net = gross.subtract(discountBD).max(BigDecimal.ZERO)

        // Round to nearest integer Rupiah (or 2 decimal places if needed, standard IDR is integer)
        return net.setScale(0, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Formats decimal quantity cleanly:
     * e.g. 1.0 -> "1", 0.5 -> "0,5", 1.25 -> "1,25"
     */
    fun formatQty(qty: Double): String {
        return if (qty % 1.0 == 0.0) {
            qty.toLong().toString()
        } else {
            String.format(java.util.Locale.GERMANY, "%.3f", qty)
                .trimEnd('0')
                .trimEnd(',')
        }
    }
}
