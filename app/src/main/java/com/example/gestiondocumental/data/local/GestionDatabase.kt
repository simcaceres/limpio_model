package com.example.gestiondocumental.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        DocumentoEntity::class,
        VersionEntity::class,
        SolicitudEntity::class,
        AuditoriaEntity::class,
        DependenciaEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GestionDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun versionDao(): VersionDao
    abstract fun solicitudDao(): SolicitudDao
    abstract fun auditoriaDao(): AuditoriaDao
    abstract fun dependenciaDao(): DependenciaDao

    companion object {
        @Volatile
        private var INSTANCIA: GestionDatabase? = null

        /** Migración v1 -> v2: agrega el adjunto del documento y la observación de la solicitud. */
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documentos ADD COLUMN archivoNombre TEXT")
                db.execSQL("ALTER TABLE solicitudes ADD COLUMN observacion TEXT")
            }
        }

        // Nombre solicitado explícitamente por el PRD.
        val MIGRATION_1_2 = MIGRACION_1_2

        fun obtener(context: Context): GestionDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    GestionDatabase::class.java,
                    "gestion_documental.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCIA = it }
            }
    }
}
