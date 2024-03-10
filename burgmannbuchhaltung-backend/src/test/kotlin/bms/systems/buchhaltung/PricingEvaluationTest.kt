package bms.systems.buchhaltung

import bms.buchhaltung.services.usagebilling.db.PricingData
import kotlin.test.Test

class PricingEvaluationTest {

    fun PricingData.test(unit: Int, expected: Double) {
        val evaluated = evalFor(unit)
        check(evaluated == expected) {
            "--- Fail for $unit: Expected $expected but evaluated $evaluated"
        }
        println("--- OK for $unit -> $expected")
    }

    private fun PricingData.evalFor(int: Int): Double {
        println("> EVAL FOR: $int")
        val total = this.evaluateTotalPricing(int)
        println("> TOTAL $int: $total")
        println("------------")
        return total
    }

    // Fixed pricing
    @Test
    fun testSimpleFixedPricing() {
        val pricing = PricingData(
            listOf(
                PricingData.PricingDataEntry(1, null, 1.123)
            ), "EUR"
        )

        pricing.run {
            validatePricing()

            test(0, (0.0))
            test(1, (1.123))
            test(7000, (7000 * 1.123))
        }
    }

    @Test
    fun testFixedPricingWithFee() {
        val pricing = PricingData(
            listOf(
                PricingData.PricingDataEntry(1, null, 1.123, 10.50)
            ), "EUR"
        )

        pricing.run {
            validatePricing()

            test(0, (0.0))
            test(1, (1.123 + 10.50))
            test(7000, (7000 * 1.123 + 10.50))
        }
    }

    // Graduated pricing

    @Test
    fun testGraduatedPricing() {
        val pricing = PricingData(
            listOf(
                PricingData.PricingDataEntry(1, 100, 5.0),
                PricingData.PricingDataEntry(101, 1000, 4.0),
                PricingData.PricingDataEntry(1001, 5000, 3.0),
                PricingData.PricingDataEntry(5001, null, 1.0),
            ), "EUR"
        )

        pricing.run {
            validatePricing()

            test(0, (0.0))
            test(1, (1 * 5.0))

            test(99, (99 * 5.0))
            test(100, (100 * 5.0))
            test(101, (100 * 5.0 + 1 * 4.0))

            test(999, (100 * 5.0 + 899 * 4.0))
            test(1000, (100 * 5.0 + 900 * 4.0))
            test(1001, (100 * 5.0 + 900 * 4.0 + 1 * 3.0))

            test(5000, (100 * 5.0 + 900 * 4.0 + 4000 * 3.0))

            test(5001, (100 * 5.0 + 900 * 4.0 + 4000 * 3.0 + 1 * 1.0))
            test(10000, (100 * 5.0 + 900 * 4.0 + 4000 * 3.0 + 5000 * 1.0))
        }
    }

    @Test
    fun testSimplePackagePricing() {
        val pricing = PricingData(
            listOf(
                PricingData.PricingDataEntry(1, null, 0.0, flatFee = 10.0, repeatFeeEvery = 1000),
            ), "EUR"
        )

        pricing.run {
            validatePricing()

            test(0, (0.0))
            test(1, (10.0))
            test(999, (10.0))
            test(1000, (10.0))
            test(1001, (20.0))
            test(1999, (20.0))
            test(2000, (20.0))
            test(2001, (30.0))
        }
    }

    @Test
    fun testFixedAndPackagePricing() {
        val pricing = PricingData(
            listOf(
                PricingData.PricingDataEntry(1, null, 0.01, flatFee = 75.0, repeatFeeEvery = 1000),
            ), "EUR"
        )

        pricing.run {
            validatePricing()

            test(0, (0.0))

            test(1, (0.01 * 1 + 75.0))
            test(1000, (0.01 * 1000 + 75.0))

            test(1001, (0.01 * 1001 + 75.0 * 2))
            test(1002, (0.01 * 1002 + 75.0 * 2))

            test(2000, (0.01 * 2000 + 75.0 * 2))
            test(2001, (0.01 * 2001 + 75.0 * 3))
        }
    }

    @Test
    fun testMultipleRepeating() {
        val pricing = PricingData(
            listOf(
                PricingData.PricingDataEntry(1, 1000, 0.0, flatFee = 100.0, repeatFeeEvery = 100),
                PricingData.PricingDataEntry(1001, 5000, 0.0, flatFee = 100.0, repeatFeeEvery = 250),
                PricingData.PricingDataEntry(5001, null, 0.0, flatFee = 100.0, repeatFeeEvery = 500),
            ), "EUR"
        )

        pricing.run {
            validatePricing()

            test(0, 0.0)
            test(1, 100.0)
            test(100, 100.0)

            test(101, 200.0)

            test(500, 500.0)

            test(1000, 1000.0)

            test(1001, 1100.0)
            test(1250, 1100.0)
            test(1250, ((1000 / 100) * 100.0) + (((1250 - 1000) / 250) * 100.0))

            test(1251, 1200.0)

            test(1500, ((1000 / 100) * 100.0) + (((1500 - 1000) / 250) * 100.0))

            test(5000, ((1000 / 100) * 100.0) + (((5000 - 1000) / 250) * 100.0))
            test(5500, ((1000 / 100) * 100.0) + (((5000 - 1000) / 250) * 100.0) + 100)
            test(5501, ((1000 / 100) * 100.0) + (((5000 - 1000) / 250) * 100.0) + 100 * 2)
        }
    }

    @Test
    fun testVolumePricing() {
        val pricing = PricingData(
            listOf(
                PricingData.PricingDataEntry(1, 100, 1.0),
                PricingData.PricingDataEntry(101, 200, 0.7),
                PricingData.PricingDataEntry(201, null, 0.3),
            ), "EUR", onlyConsiderLastEntry = true
        )

        pricing.run {
            validatePricing()

            pricing.test(0, 0.0)
            pricing.test(1, 1.0)
            pricing.test(100, 100.0)

            pricing.test(150, 150 * 0.7)
            pricing.test(200, 200 * 0.7)

            pricing.test(201, 201 * 0.3)
            pricing.test(1000, 1000 * 0.3)
        }
    }

    @Test
    fun testFixedWithGlobalMinimumCharge() {
        val pricing = PricingData(
            listOf(
                PricingData.PricingDataEntry(1, 1000, 2.0),
                PricingData.PricingDataEntry(1001, 5000, 1.5),
                PricingData.PricingDataEntry(5001, null, 1.0),
            ), "EUR", minimumCharge = 100.00
        )

        pricing.run {
            validatePricing()

            pricing.test(20, 100.00)
        }
    }

}
