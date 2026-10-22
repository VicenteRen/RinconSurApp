package cl.duoc.rinconsurapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duoc.rinconsurapp.model.RegistroObservacion
import cl.duoc.rinconsurapp.model.Rol
import cl.duoc.rinconsurapp.model.Usuario

class RinconSurDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "rinconsur.db"
        private const val VERSION_BD = 1

        // 1. Tabla ROLES
        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        // 2. Tabla USUARIOS
        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_RUT = "rut"
        private const val USU_CONTRASENA = "contrasena"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_TELEFONO = "telefono"
        private const val USU_ROL = "id_rol"
        private const val USU_AVATAR = "avatar"

        // 3. Tabla REGISTROS DE OBSERVACIÓN
        private const val TABLA_REGISTROS = "registros_observacion"
        private const val REG_ID = "id_registro"
        private const val REG_CLASIFICACION = "clasificacion"
        private const val REG_RANGO = "rango_etario"
        private const val REG_COMPANIA = "compania"
        private const val REG_COMPRAS = "compras_otra_tienda" // 1 = Sí, 0 = No
        private const val REG_DESTINO = "acceso_destino"
        private const val REG_FECHA_HORA = "fecha_hora"
        private const val REG_USUARIO = "id_usuario"

        // SQL Creación de Tablas
        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                    "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ROL_NOMBRE TEXT NOT NULL UNIQUE)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_RUT TEXT NOT NULL UNIQUE, " +
                    "$USU_CONTRASENA TEXT NOT NULL, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_CORREO TEXT NOT NULL, " +
                    "$USU_TELEFONO TEXT NOT NULL, " +
                    "$USU_AVATAR BLOB, " + // NUEVO (Avatar)
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        private const val SQL_CREAR_REGISTROS =
            "CREATE TABLE $TABLA_REGISTROS (" +
                    "$REG_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$REG_CLASIFICACION TEXT NOT NULL, " +
                    "$REG_RANGO TEXT NOT NULL, " +
                    "$REG_COMPANIA TEXT NOT NULL, " +
                    "$REG_COMPRAS INTEGER NOT NULL, " +
                    "$REG_DESTINO TEXT, " +
                    "$REG_FECHA_HORA TEXT NOT NULL, " +
                    "$REG_USUARIO INTEGER NOT NULL REFERENCES $TABLA_USUARIOS($USU_ID))"

        // Datos precargados (Usa los mismos RUT y claves que programó tu compañero)
        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                    "(1, 'Observador'), (2, 'Administrador')"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_ID, $USU_RUT, $USU_CONTRASENA, $USU_NOMBRE, $USU_CORREO, $USU_TELEFONO, $USU_ROL) VALUES " +
                    "(1, '214297353', 'observador123', 'Vicente Ponce', 'v.ponce@rinconsur.cl', '987654321', 1), " +
                    "(2, '228405566', 'admin123', 'Jefe Administrador', 'admin@rinconsur.cl', '911223344', 2)"

        // Consultas base con INNER JOIN (igual que el profesor)
        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"

        private const val SQL_SELECT_REGISTROS =
            "SELECT reg.*, u.$USU_NOMBRE AS $USU_NOMBRE " +
                    "FROM $TABLA_REGISTROS reg " +
                    "INNER JOIN $TABLA_USUARIOS u ON reg.$REG_USUARIO = u.$USU_ID"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_CREAR_REGISTROS)
        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_REGISTROS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }

    // ---------------------------- USUARIOS (Login y Perfil) ----------------------------

    fun autenticar(rut: String, contrasena: String): Usuario? =
        consultarUsuarios(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_RUT = ? AND u.$USU_CONTRASENA = ?",
            arrayOf(rut, contrasena)
        ).firstOrNull()

    fun buscarUsuarioPorId(id: Long): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()

    fun actualizarContactoUsuario(id: Long, nuevoCorreo: String, nuevoTelefono: String): Int {
        val valores = ContentValues().apply {
            put(USU_CORREO, nuevoCorreo)
            put(USU_TELEFONO, nuevoTelefono)
        }
        return writableDatabase.update(TABLA_USUARIOS, valores, "$USU_ID = ?", arrayOf(id.toString()))
    }

    fun actualizarAvatar(id: Long, avatar: ByteArray): Int {
        val valores = ContentValues().apply {
            put(USU_AVATAR, avatar)
        }
        return writableDatabase.update(TABLA_USUARIOS, valores, "$USU_ID = ?", arrayOf(id.toString()))
    }

    // ---------------------------- REGISTROS DE OBSERVACIÓN ----------------------------

    // CREATE -> INSERT (Para tu botón Guardar)
    fun insertarRegistro(registro: RegistroObservacion): Long {
        val valores = ContentValues().apply {
            put(REG_CLASIFICACION, registro.clasificacion)
            put(REG_RANGO, registro.rangoEtario)
            put(REG_COMPANIA, registro.compania)
            put(REG_COMPRAS, if (registro.tieneComprasExternas) 1 else 0)
            put(REG_DESTINO, registro.accesoDestino)
            put(REG_FECHA_HORA, registro.fechaHora)
            put(REG_USUARIO, registro.usuarioId)
        }
        return writableDatabase.insert(TABLA_REGISTROS, null, valores)
    }

    // READ Global -> Para el perfil Administrador (ve los conteos de todos)
    fun listarTodosLosRegistros(): List<RegistroObservacion> =
        consultarRegistros("$SQL_SELECT_REGISTROS ORDER BY reg.$REG_ID DESC", null)

    // READ por Empleado -> Para el perfil Observador (ve solo los suyos)
    fun listarRegistrosPorUsuario(idUsuario: Long): List<RegistroObservacion> =
        consultarRegistros(
            "$SQL_SELECT_REGISTROS WHERE reg.$REG_USUARIO = ? ORDER BY reg.$REG_ID DESC",
            arrayOf(idUsuario.toString())
        )

    // DELETE -> Por si el observador o admin anula/elimina un registro equivocado
    fun eliminarRegistro(idRegistro: Long): Int =
        writableDatabase.delete(TABLA_REGISTROS, "$REG_ID = ?", arrayOf(idRegistro.toString()))

    // ---------------------------- AYUDANTES PRIVADOS ----------------------------

    private fun consultarUsuarios(sql: String, args: Array<String>?): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Usuario(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
                        rut = cursor.getString(cursor.getColumnIndexOrThrow(USU_RUT)),
                        contrasena = cursor.getString(cursor.getColumnIndexOrThrow(USU_CONTRASENA)),
                        nombreCompleto = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
                        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
                        telefono = cursor.getString(cursor.getColumnIndexOrThrow(USU_TELEFONO)),
                        rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
                        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE)),
                        avatar = cursor.getColumnIndexOrThrow(USU_AVATAR).let { col ->
                            if (cursor.isNull(col)) null else cursor.getBlob(col)
                        }
                    )
                )
            }
        }
        return lista
    }

    private fun consultarRegistros(sql: String, args: Array<String>?): List<RegistroObservacion> {
        val lista = mutableListOf<RegistroObservacion>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                val indiceDestino = cursor.getColumnIndexOrThrow(REG_DESTINO)
                lista.add(
                    RegistroObservacion(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(REG_ID)),
                        clasificacion = cursor.getString(cursor.getColumnIndexOrThrow(REG_CLASIFICACION)),
                        rangoEtario = cursor.getString(cursor.getColumnIndexOrThrow(REG_RANGO)),
                        compania = cursor.getString(cursor.getColumnIndexOrThrow(REG_COMPANIA)),
                        tieneComprasExternas = cursor.getInt(cursor.getColumnIndexOrThrow(REG_COMPRAS)) == 1,
                        accesoDestino = if (cursor.isNull(indiceDestino)) null else cursor.getString(indiceDestino),
                        fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(REG_FECHA_HORA)),
                        usuarioId = cursor.getLong(cursor.getColumnIndexOrThrow(REG_USUARIO)),
                        nombreObservador = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE))
                    )
                )
            }
        }
        return lista
    }
}