package bms.buchhaltung.services.organizations

import bms.buchhaltung.db.model.organizations.Organization
import bms.buchhaltung.web.InsufficientPermissionsException
import bms.buchhaltung.web.getOrganizationId
import bms.buchhaltung.web.getRoleInOrganization
import bms.buchhaltung.web.getUserUUID
import io.github.smiley4.ktorswaggerui.dsl.*
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.uuid.UUID

fun Application.organizations() {
    routing {
        authenticate("authenticated-session", "authenticated-bearer") {
            route("/r/organizations", {
                tags = listOf("Organizations")
            }) {
                authenticatedRoutes()
            }
        }
    }
}

private fun Route.authenticatedRoutes() {
    get({
        summary = "List organizations that the user is apart of"
        response { HttpStatusCode.OK to { body<List<OrganizationService.UserOrganizationEntry>>() } }
    }) {
        val user = getUserUUID()
        call.respond(OrganizationService.getUserOrganizations(user))
    }



    post("create", {
        summary = "Create a new organization"
        request { body<OrganizationService.CreateOrganizationRequest>() }
        response { HttpStatusCode.Created to { body<String> { description = "Organization ID" } } }
    }) {
        val req = call.receive<Organization>()

        val organizationId = OrganizationService.createOrganization(req)
        OrganizationService.addUserToOrganization(organizationId, getUserUUID(), "admin")

        call.respond(HttpStatusCode.Created, organizationId.toString())
    }

    route("org/{organization}", {
        request {
            pathParameter<String>("organization") {
                description = "Organization UUID"
            }
        }
    }) {

        delete({
            summary = "Delete organization"
        }) {
            if (getRoleInOrganization() == "admin") {
                OrganizationService.deleteOrganization(getOrganizationId())
                call.respond(HttpStatusCode.Accepted)
            } else throw InsufficientPermissionsException("User does not have admin permission for organization to delete.")
        }

        get("view", {
            summary = "View organizatiion"
        }) {
            val orgId = getOrganizationId()
            call.respond(OrganizationService.getOrganization(orgId))
        }

        route("users") {
            get({
                summary = "List users in organization"
                response { HttpStatusCode.OK to { body<OrganizationService.OrganizationUserEntry>() } }
            }) {
                context.respond(OrganizationService.getOrganizationUsers(getOrganizationId()))
            }

            route("{user}", {
                request {
                    pathParameter<String>("user")
                }
            }) {

                fun PipelineContext<Unit, ApplicationCall>.getSuppliedUser(): UUID =
                    UUID(call.parameters["user"] ?: throw IllegalArgumentException("No user id supplied!"))

                put({
                    summary = "Invite user to organization"
                    response { HttpStatusCode.OK to { body<OrganizationService.OrganizationUserEntry>() } }
                }) {
                    val user = getSuppliedUser()
                    TODO("Invite system")
                }

                delete({
                    summary = "Delete user from organization"
                    response { HttpStatusCode.OK to { body<OrganizationService.OrganizationUserEntry>() } }
                }) {
                    if (getRoleInOrganization() == "admin") {
                        OrganizationService.removeUserFromOrganization(getOrganizationId(), getSuppliedUser())
                        call.respond(HttpStatusCode.Accepted)
                    } else throw InsufficientPermissionsException("User does not have admin permission for organization to delete.")
                }

                patch("role", {
                    summary = "Update user role in organization"
                    request { queryParameter<String>("newRole") }
                    response { HttpStatusCode.OK to { body<OrganizationService.OrganizationUserEntry>() } }
                }) {
                    if (getRoleInOrganization() == "admin") {
                        OrganizationService.updateOrganizationUserRole(
                            getOrganizationId(),
                            getSuppliedUser(),
                            call.request.queryParameters["newRole"]
                                ?: throw IllegalArgumentException("No new role passed!")
                        )
                        call.respond(HttpStatusCode.Accepted)
                    } else throw InsufficientPermissionsException("User does not have admin permission for organization to delete.")

                }
            }


        }


    }
}
