package bms.buchhaltung.services.organizations

import bms.buchhaltung.db.model.organizations.Organization
import bms.buchhaltung.db.model.organizations.OrganizationUsers
import bms.buchhaltung.db.model.organizations.Organizations
import bms.buchhaltung.db.model.taxes.AustrianLegalForm
import bms.buchhaltung.db.model.users.Users
import bms.buchhaltung.services.users.UserService
import bms.buchhaltung.utils.sqlTransaction
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object OrganizationService {
    fun initDatabaseSchemas() {
        SchemaUtils.drop(OrganizationUsers, Organizations)
        SchemaUtils.create(Organizations, OrganizationUsers)

        val users = UserService.listAllUsers()


        listOf(
            UserOrganizationEntry(
                UUID.generateUUID(),
                "Mustermann GmbH",
                "https://images.unsplash.com/photo-1560179707-f14e90ef3623?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8Y29tcGFueXxlbnwwfHwwfHx8MA%3D%3D&auto=format&fit=crop&w=500&q=60",
                "admin",

                AustrianLegalForm.Gesellschaft_mit_beschränkter_Haftung,
                "AT", "1010", "Wien", "Am Hof 1"
            ),
            UserOrganizationEntry(
                UUID.generateUUID(),
                "Max Mustermann e.U.",
                "https://images.unsplash.com/photo-1537511446984-935f663eb1f4?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NXx8Y29uc3VsdGFudHxlbnwwfHwwfHx8MA%3D%3D&fit=crop&crop=faces&w=250&h=250&q=60",
                "admin",

                AustrianLegalForm.Eingetragener_Unternehmer,
                "AT", "1010", "Wien", "Am Hof 1"
            ),
            UserOrganizationEntry(
                UUID.generateUUID(),
                "Erste Österreichische Musterfirma AG",
                "https://images.unsplash.com/photo-1617526738882-1ea945ce3e56?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjB8fGNvbXBhbnl8ZW58MHx8MHx8fDA%3D&auto=format&fit=crop&w=500&q=60",
                "abc",

                AustrianLegalForm.Aktiengesellschaft,
                "AT", "1010", "Wien", "Am Hof 1"
            ),
            UserOrganizationEntry(
                UUID.generateUUID(),
                "1+ Steuerberatungsgesellschaft mbH",
                "https://images.unsplash.com/photo-1512403754473-27835f7b9984?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTh8fGNvbXBhbnl8ZW58MHx8MHx8fDA%3D&auto=format&fit=crop&w=500&q=60",
                "accountant",

                AustrianLegalForm.Gesellschaft_mit_beschränkter_Haftung,
                "AT", "1010", "Wien", "Am Hof 1"
            ),
        ).forEach {
            val createdOrganizationId = createOrganization(
                Organization(
                    id = null,
                    name = it.name,
                    image = it.image,
                    country = it.country,
                    zip = it.zip,
                    city = it.city,
                    address = it.address,
                    legalForm = it.legalForm,
                    kleinunternehmer = false
                )
            )

            users.forEach { user ->
                addUserToOrganization(createdOrganizationId, user.id, it.role)
            }
        }
    }

    @Serializable
    data class CreateOrganizationRequest(val name: String, val image: String? = null)

    fun createOrganization(org: Organization) = sqlTransaction {
        Organizations.insertAndGetId {
            it[name] = org.name
            it[phone] = org.phone
            it[email] = org.email
            it[website] = org.website
            it[address] = org.address
            it[zip] = org.zip
            it[city] = org.city
            it[country] = org.country
            it[legalForm] = org.legalForm
            it[bank] = org.bank
            it[iban] = org.iban
            it[image] = org.image
            it[vat] = org.vat
            it[taxNr] = org.taxNr
            it[taxNrCodesList] = org.taxNrCodesList
            it[eori] = org.eori
            it[companyRegisterNr] = org.companyRegisterNr
            it[companyRegister] = org.companyRegister
            it[kleinunternehmer] = org.kleinunternehmer
        }.value
    }

    fun deleteOrganization(organization: UUID) = sqlTransaction {
        val org = organization
        OrganizationUsers.deleteWhere { OrganizationUsers.organization eq org }
        Organizations.deleteWhere { id eq org }
    }

    fun getOrganization(organization: UUID): Organization = sqlTransaction {
        val org = organization
        Organization(Organizations.select { Organizations.id eq org }.single())
    }


    @Serializable
    data class UserOrganizationEntry(
        val id: UUID, // organizationId
        val name: String, // organizationName
        val image: String?, // organizationImage
        val role: String, // Users role in organization

        val legalForm: AustrianLegalForm,

        val country: String,
        val zip: String,
        val city: String,
        val address: String
    )

    fun getUserRoleInOrganization(organization: UUID, user: UUID): String? = sqlTransaction {
        val userId = user
        val organizationId = organization
        OrganizationUsers.select { (OrganizationUsers.organization eq organizationId) and (OrganizationUsers.user eq userId) }.firstOrNull()
            ?.getOrNull(OrganizationUsers.role)
    }

    fun getUserOrganizations(user: UUID) = sqlTransaction {
        val userId = user
        (OrganizationUsers innerJoin Organizations)
            .select { OrganizationUsers.user eq userId }
            .map {
                UserOrganizationEntry(
                    id = it[Organizations.id].value,
                    name = it[Organizations.name],
                    image = it[Organizations.image],
                    role = it[OrganizationUsers.role],

                    country = it[Organizations.country],
                    zip = it[Organizations.zip],
                    city = it[Organizations.city],
                    address = it[Organizations.address],

                    legalForm = it[Organizations.legalForm]
                )
            }
    }

    @Serializable
    data class OrganizationUserEntry(
        val userId: String,
        val userName: String,
        val role: String
    )

    fun getOrganizationUsers(organization: UUID) = sqlTransaction {
        val orgId = organization
        (OrganizationUsers innerJoin Users)
            .select { OrganizationUsers.organization eq orgId }
            .map {
                OrganizationUserEntry(
                    userId = it[Users.id].value.toString(),
                    userName = it[Users.name],
                    role = it[OrganizationUsers.role]
                )
            }
    }

    fun addUserToOrganization(organization: UUID, user: UUID, role: String) = sqlTransaction {
        OrganizationUsers.insert {
            it[OrganizationUsers.organization] = organization
            it[OrganizationUsers.user] = user
            it[OrganizationUsers.role] = role
        }
    }

    fun removeUserFromOrganization(organization: UUID, user: UUID) = sqlTransaction {
        val organizationId = organization
        val userId = user
        OrganizationUsers.deleteWhere {
            (OrganizationUsers.organization eq organizationId) and (OrganizationUsers.user eq userId)
        }
    }

    fun updateOrganizationUserRole(organization: UUID, user: UUID, newRole: String) = sqlTransaction {
        val userId = user
        val organizationId = organization
        OrganizationUsers.update({
            (OrganizationUsers.organization eq organizationId) and (OrganizationUsers.user eq userId)
        }) {
            it[role] = newRole
        }
    }

}
