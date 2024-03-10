package bms.buchhaltung.services.usagebilling.db

import bms.buchhaltung.db.model.organizations.Organizations
import kotlinx.serialization.json.Json
import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

object TariffPlanPricings : Table() {
    val organization = reference("organization", Organizations)
    val billingConfig = kotlinxUUID("billing_config")
    val tariffPlan = kotlinxUUID("tariff_plan")

    val metric = kotlinxUUID("metric")

    val pricing = json("pricing", Json, PricingData.serializer())

    val tariffIncludes = integer("tariff_includes")

    init {
        foreignKey(organization, billingConfig, target = AutomatedBillingConfigurations.primaryKey)
        foreignKey(organization, billingConfig, tariffPlan, target = TariffPlans.primaryKey)
        foreignKey(organization, billingConfig, metric, target = TrackableMetrics.primaryKey)
    }
}
