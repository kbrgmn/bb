package bms.buchhaltung.services.usagebilling.db

import bms.buchhaltung.db.model.organizations.Organizations
import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table

object TariffPlans : Table() {
    val organization = reference("organization", Organizations)
    val billingConfig = kotlinxUUID("billing_config")
    val id = kotlinxUUID("id")

    val name = varchar("name", 128)

    override val primaryKey = PrimaryKey(organization, billingConfig, id)

    init {
        foreignKey(organization, billingConfig, target = AutomatedBillingConfigurations.primaryKey)
    }
}
