package bms.buchhaltung.services.usagebilling.db

import bms.buchhaltung.db.model.organizations.Organization
import bms.buchhaltung.db.model.organizations.Organizations
import bms.buchhaltung.utils.CronSetupData
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/*
- EmailBilling
  - Personal - 5€
    - 3 EmailAccounts (extra: 5€/Account)
    - 50 email / day (extra: 0.15€/Email)
    - 2 GB (extra: 1€/GB)
    - Basic support (Upgrade to advanced support: 35€)
  - Small Business - 29€
    - 15 EmailAccounts (extra: 3€/Account)
    - 750 email / day (extra: 0.05€/Email)
    - 10 GB (extra: 1€/GB)
    - Basic support (Upgrade to advanced support: 35€)
  - Enterprise - 300€
    - 100 EmailAccounts (extra: 2€/Account)
    - 5000 email / day (extra: 0.01€/Email)
    - 75 GB (extra: 0,50€/GB)
    - Advanced support

- WebsiteBilling
  - SMB (100€)
    - 4 domains
    - simple Website
    - User visit: 0.001€


  - Webshop (250€)
    - 4 domains
    - Webshop
    - User visit: 0.0005€

  - Large Business (250€)
    - 15 domains
    - advanced website
    - User visit: 0.0001€


Stadtoptik GmbH:
  - EmailBilling: Small business (+1 extra account)
  - WebsiteBilling: SMB

Autoteile Online GmbH:
  - EmailBilling: Small business (+Advanced support)
  - WebsiteBilling: Webshop

Large business GmbH:
  - EmailBilling: Enterprise (+20 users, +20000 emails, +20 GB)
  - WebsiteBilling: Large Business
*/

fun main() {
    val org = transaction { Organization(Organizations.selectAll().first()) }
    val orgId = org.id!!

    fun addBilling(name: String, cycle: CronSetupData) = AutomatedBillingConfigurations.insert {
        it[organization] = orgId
        it[id] = UUID.generateUUID()
        it[AutomatedBillingConfigurations.name] = name
        it[defaultBillingCycle] = CronSetupData.END_MONTHLY
    }[AutomatedBillingConfigurations.id]

    val emailBilling = addBilling("EmailBilling", CronSetupData.END_MONTHLY)
    val websiteBilling = addBilling("WebsiteBilling", CronSetupData.END_MONTHLY)

    fun addMetrics(config: UUID, name: String) = TrackableMetrics.insert {
        it[organization] = orgId
        it[billingConfig] = config
        it[id] = UUID.generateUUID()
        it[TrackableMetrics.name] = name
    }[TrackableMetrics.id]

    // Email metrics
    val emailAccounts = addMetrics(emailBilling, "Email accounts")
    val emailsPerDay = addMetrics(emailBilling, "Emails/day")
    val emailGb = addMetrics(emailBilling, "Emails GB")

    // Website metrics
    val websiteDomains = addMetrics(websiteBilling, "Website domains")
    val websiteUserVisit = addMetrics(websiteBilling, "User visits")

    // Tariff plans
    fun addTariffPlan(billingConfig: UUID, name: String) = TariffPlans.insert {
        it[organization] = orgId
        it[TariffPlans.billingConfig] = billingConfig
        it[id] = UUID.generateUUID()
        it[TariffPlans.name] = name
    }[TariffPlans.id]

    val emailPersonal = addTariffPlan(emailBilling, "Personal")
    val emailSmallBusiness = addTariffPlan(emailBilling, "Small business")
    val emailEnterprise = addTariffPlan(emailBilling, "Enterprise")

    val websiteSMB = addTariffPlan(websiteBilling, "SMB")
    val websiteWebshop = addTariffPlan(websiteBilling, "Webshop")
    val websiteLargeBusiness = addTariffPlan(websiteBilling, "Large Business")

    // Tariff plan pricing
    //val emailAccountPricingData = PricingData()
}
