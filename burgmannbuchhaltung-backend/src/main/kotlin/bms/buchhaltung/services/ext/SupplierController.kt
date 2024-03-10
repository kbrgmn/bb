package bms.buchhaltung.services.ext

import bms.buchhaltung.db.model.ext.suppliers.Supplier
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

fun Application.suppliers() {
    routing {
        authenticate("authenticated-session", "authenticated-bearer") {
            route("r/org/{organization}/suppliers", {
                request { pathParameter<String>("organization") }

                tags = listOf("Suppliers")
            }) {
                post("create", {
                    summary = "Create a new supplier"
                    request { body<Supplier>() { description = "Supplier data to create" } }
                    response { HttpStatusCode.OK to { body<String>(); description = "Created supplier ID" } }
                }) {
                    val orgId = getOrganizationId()
                    val data = call.receive<Supplier>().copy(organization = orgId)

                    call.respond(
                        SupplierService.upsert(orgId, data).toString()
                    )
                }

                get("list", {
                    summary = "List suppliers for organization"
                    response { HttpStatusCode.OK to { body<List<SupplierService.SupplierListing>>() } }
                }) {
                    val orgId = getOrganizationId()
                    call.respond(SupplierService.list(orgId))
                }

                route("{id}", {
                    request {
                        pathParameter<String>("id")
                    }
                }) {
                    fun PipelineContext<Unit, ApplicationCall>.getSupplier() =
                        UUID(call.parameters["id"] ?: throw IllegalArgumentException("No ID in request path"))

                    fun PipelineContext<Unit, ApplicationCall>.getOrgAndSupplier() =
                        Pair(getOrganizationId(), getSupplier())

                    patch({
                        request { body<Supplier>() }
                        response { HttpStatusCode.OK to { body<String>() } }
                    }) {
                        val (orgId, supplierId) = getOrgAndSupplier()
                        val data = call.receive<Supplier>().copy(id = supplierId, organization = orgId)

                        call.respond(
                            SupplierService.upsert(orgId, data)
                        )
                    }
                    delete({
                        summary = "Delete a supplier"
                    }) {
                        val (orgId, supplierId) = getOrgAndSupplier()
                        SupplierService.delete(orgId, supplierId)
                        call.respond(HttpStatusCode.Accepted)
                    }
                    get({
                        summary = "Retrieve a supplier"
                    }) {
                        val (orgId, supplierId) = getOrgAndSupplier()

                        call.respond(
                            SupplierService.get(orgId, supplierId)
                                ?: throw IllegalArgumentException("No such supplier: $orgId / $supplierId")
                        )
                    }
                }
            }
        }
    }
}
