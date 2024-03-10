package bms.buchhaltung.services.usagebilling.db

import bms.buchhaltung.db.model.ext.customers.Customers
import bms.buchhaltung.db.model.organizations.Organizations
import bms.buchhaltung.utils.CronSetupData
import kotlinx.serialization.json.Json
import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

object AssignedTariffPlans : Table() {
    val organization= reference("organization", Organizations)

    val customer = kotlinxUUID("customer")
    val config = kotlinxUUID("config")

    val customBillingCycle = json("billing_cycle", Json, CronSetupData.serializer())
        .nullable()



    init {
        foreignKey(organization, customer, target = Customers.primaryKey)
        foreignKey(organization, config, target = AutomatedBillingConfigurations.primaryKey)
    }

}
