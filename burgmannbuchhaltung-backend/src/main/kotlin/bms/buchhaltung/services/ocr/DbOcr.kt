package bms.buchhaltung.services.ocr

import bms.buchhaltung.db.model.receiptimport.ImportFiles
import bms.buchhaltung.db.model.receiptimport.ImportSessions
import org.jetbrains.exposed.sql.SchemaUtils

object DbOcr {
    fun initSchemas() {
        SchemaUtils.drop(ImportFiles, ImportSessions)
        SchemaUtils.create(ImportSessions, ImportFiles)
    }
}
