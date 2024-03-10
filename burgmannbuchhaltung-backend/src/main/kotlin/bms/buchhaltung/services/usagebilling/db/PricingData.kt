package bms.buchhaltung.services.usagebilling.db

import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.max

@Serializable
data class PricingData(
    val pricing: List<PricingDataEntry>,
    val currency: String,

    val minimumCharge: Double? = null,

    val onlyConsiderLastEntry: Boolean = false
) {

    @Serializable
    data class PricingDataEntry(
        val unitsFrom: Int,
        val unitsTo: Int?,
        val pricePerPiece: Double,
        val flatFee: Double? = null,
        val repeatFeeEvery: Int? = null,
        val minimumCharge: Double? = null
    ) {
        override fun toString(): String {
            return "[PricingEntry:${if (unitsTo != null) " (${unitsTo - (unitsFrom - 1)} units)" else ""} From $unitsFrom - to ${unitsTo ?: "open"}${if (repeatFeeEvery != null) ", repeating every $repeatFeeEvery" else ""}: Price per unit = $pricePerPiece${if (flatFee != null) ", flat fee: $flatFee" else ""}]"
        }

        fun isFullyMaxedReachedByPieceCount(int: Int): Boolean =
            int > unitsFrom && unitsTo != null && int >= unitsTo

        //fun maxPriceWhenFullyReached() = ((1 + unitsTo!! - unitsFrom) * pricePerPiece) + flatFee
        fun maxPriceWhenFullyReached() = calculatePartialPricingForUnitCount(unitsTo!!)

        fun calculatePartialPricingForUnitCount(unitCount: Int): Double {
            val unitsOfThisEntry = (1 + unitCount - unitsFrom)
            return when {
                repeatFeeEvery != null -> (unitsOfThisEntry * pricePerPiece) + (ceil(unitsOfThisEntry / repeatFeeEvery.toDouble())) * flatFee
                else -> (unitsOfThisEntry * pricePerPiece) + flatFee.let { if (unitCount > 0) it else null }
            }.run { if (minimumCharge != null) max(minimumCharge, this) else this }
        }

    }

    fun validatePricing() {
        check(pricing.isNotEmpty()) { "No pricing is included, empty list" }

        pricing.forEachIndexed { index, currentEntry ->
            currentEntry.unitsTo?.let { check(currentEntry.unitsFrom < currentEntry.unitsTo) { "Current entry (idx $index) has lower range end than range start" } }

            fun err(msg: String, previousEntry: PricingDataEntry?) =
                "Error validating pricing at entry ${index + 1}/${pricing.size}: Current entry: $currentEntry${if (previousEntry != null) ", previous entry (${index}/${pricing.size}): $previousEntry" else ""} -> $msg"

            fun isLast() = index + 1 == pricing.size

            if (index == 0) {
                check(currentEntry.unitsFrom == 1) { err("Initial pricing range does not start at 0 units", null) }
            } else {
                val previousEntry = pricing[index - 1]
                fun err(msg: String) = err(msg, previousEntry)

                check(previousEntry.unitsTo != null) { err("Previous entry has open range, but there are still entries remaining") }
                check(previousEntry.unitsTo < currentEntry.unitsFrom) { err("Check ${previousEntry.unitsTo} < ${currentEntry.unitsFrom} failed: Previous entry has overlapping range with this entry") }

                check(currentEntry.unitsFrom == previousEntry.unitsTo + 1) { err("Pricing gap between ${previousEntry.unitsTo} and ${currentEntry.unitsFrom} units - ${currentEntry.unitsFrom - (previousEntry.unitsTo + 1)} units are unaccounted for") }

            }

            if (!isLast()) {
                /*check(currentEntry.repeatFeeEvery == null) {
                    err(
                        "Entry is setup to be repeating, but is not the last entry!",
                        null
                    )
                }*/
            }
        }

        fun err(msg: String) = "Error validating pricing at last entry: $msg"

        pricing.last().run {
            check(unitsTo == null) { err("Last entry does not have open range, but there are no further entries remaining") }
        }
    }

    // TODO THIS
    fun evaluateSinglePricing(currentUnitNum: Int): Double {
        return 0.12
    }

    fun evaluateTotalPricing(units: Int): Double {
        var price = 0.0

        if (onlyConsiderLastEntry) {
            // Volume-pricing like calculation

            pricing.findLast { it.unitsFrom <= units }?.run {
                price += pricePerPiece * units + flatFee
            }
        } else {
            // Graduated-pricing like calculation
            val fullyReached = pricing.takeWhile { it.isFullyMaxedReachedByPieceCount(units) }
            fullyReached.forEachIndexed { index, it ->
                println("Fully reached #${index + 1}: $it - ${it.maxPriceWhenFullyReached()}")
                price += it.maxPriceWhenFullyReached()
            }

            val lastFittingEntry = pricing.findLast {
                it.unitsFrom <= units
                        && (it.unitsTo?.let { units < it } ?: true)
            }

            if (lastFittingEntry != null && lastFittingEntry != fullyReached.lastOrNull()) {
                println(
                    """Last fitting (${units - (lastFittingEntry.unitsFrom - 1)} units): $lastFittingEntry - ${
                        lastFittingEntry.calculatePartialPricingForUnitCount(units)
                    }"""
                )

                price += lastFittingEntry.calculatePartialPricingForUnitCount(units)
            }
        }

        return if (minimumCharge != null)
            max(minimumCharge, price)
        else price
    }

}

private operator fun Double.times(other: Double?): Double = this * (other ?: 1.0)
private operator fun Double.plus(other: Double?): Double = other?.let { other + this } ?: this
