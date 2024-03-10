package bms.buchhaltung.services.users

import bms.buchhaltung.db.model.users.UserSession
import bms.buchhaltung.db.model.users.Users
import org.jetbrains.exposed.sql.SchemaUtils

object DbUsers {
    fun initSchemas() {
        SchemaUtils.drop(UserSession, Users)
        SchemaUtils.create(Users, UserSession)

        UserService.createUser(
            name = "Kevin Burgmann",
            email = "kevin@burgmann.systems",

            // pw: kevin@burgmann.systems
            password = "\$argon2i\$v=19\$m=65536,t=10,p=1$8WdnHRLDTSGiPJZbrMCI6w\$NZwM5mLluOw8/BwoZkjEMT26+nrEkt7xuDmjIua0Hy8"
        )

        UserService.createUser(
            name = "Example user",
            email = "string@string.string",

            // pw: kevin@burgmann.systems
            password = "\$argon2i\$v=19\$m=16,t=2,p=1\$TE5DZzVObjlaY24yQ0xGcA\$o+jc+hvNp8PoYUrQiyT2EA"
        )
    }
}
