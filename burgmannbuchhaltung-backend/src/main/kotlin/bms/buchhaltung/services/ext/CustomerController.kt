package bms.buchhaltung.services.ext

import bms.buchhaltung.db.model.ext.customers.Customer
import bms.buchhaltung.web.getOrganizationId
import io.github.smiley4.ktorswaggerui.dsl.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.uuid.UUID

fun Application.customers() {
    routing {
        authenticate("authenticated-session", "authenticated-bearer") {
            route("r/org/{organization}/customers", {
                request { pathParameter<String>("organization") }

                tags = listOf("Customers")
            }) {
                post("create", {
                    summary = "Create a new customer"
                    request { body<Customer>() { description = "Customer data to create" } }
                    response { HttpStatusCode.OK to { body<String>(); description = "Created customer ID" } }
                }) {
                    val orgId = getOrganizationId()
                    val data = call.receive<Customer>().copy(organization = orgId)

                    call.respond(
                        CustomerService.upsert(orgId, data).toString()
                    )
                }

                get("list", {
                    summary = "List customers for organization"
                    response { HttpStatusCode.OK to { body<List<CustomerService.CustomerListing>>() } }
                }) {
                    val orgId = getOrganizationId()
                    call.respond(CustomerService.list(orgId))
                }

                route("{id}", {
                    request {
                        pathParameter<String>("id")
                    }
                }) {
                    fun PipelineContext<Unit, ApplicationCall>.getCustomer() =
                        UUID(call.parameters["id"] ?: throw IllegalArgumentException("No ID in request path"))

                    fun PipelineContext<Unit, ApplicationCall>.getOrgAndCustomer() =
                        Pair(getOrganizationId(), getCustomer())

                    patch({
                        request { body<Customer>() }
                        response { HttpStatusCode.OK to { body<String>() } }
                    }) {
                        val (orgId, customerId) = getOrgAndCustomer()
                        val data = call.receive<Customer>().copy(id = customerId, organization = orgId)

                        call.respond(
                            CustomerService.upsert(orgId, data)
                        )
                    }
                    delete({
                        summary = "Delete a customer"
                    }) {
                        val (orgId, customerId) = getOrgAndCustomer()
                        CustomerService.delete(orgId, customerId)
                        call.respond(HttpStatusCode.Accepted)
                    }
                    get({
                        summary = "Retrieve a customer"
                    }) {
                        val (orgId, customerId) = getOrgAndCustomer()

                        call.respond(
                            CustomerService.get(orgId, customerId)
                                ?: throw IllegalArgumentException("No such customer: $orgId / $customerId")
                        )
                    }
                }
            }
        }
    }
}
