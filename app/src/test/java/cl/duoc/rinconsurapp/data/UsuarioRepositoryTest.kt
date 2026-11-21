package cl.duoc.rinconsurapp.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UsuarioRepositoryTest {

    private lateinit var apiFalsa: FakeUsuarioApi
    private lateinit var repositorio: UsuarioRepository

    @Before
    fun preparar() {
        apiFalsa = FakeUsuarioApi()
        repositorio = UsuarioRepository(usuarioApi = apiFalsa)
    }

    @Test
    fun login_conRutConEspacios_limpiaRutYConvierteUsuario() = runBlocking {
        val usuario = repositorio.login("  214297353  ", "Observador123*")

        assertEquals("214297353", apiFalsa.ultimoRutLogin)
        assertEquals(1L, usuario.id)
        assertEquals("Vicente Ponce", usuario.nombreCompleto)
        assertEquals("http://127.0.0.1:8080/api/usuarios/avatares/usuario_1.jpg", usuario.avatarUrl)
    }

    @Test
    fun listarActivos_convierteListaCompleta() = runBlocking {
        val lista = repositorio.listarActivos()

        assertEquals(2, lista.size)
        assertEquals("Administrador", lista[1].rolNombre)
        assertNull(lista[1].avatarUrl)
    }

    @Test
    fun actualizarAvatar_enviaArchivoMultipartYResuelveUrl() = runBlocking {
        val actualizado = repositorio.actualizarAvatar(1L, byteArrayOf(1, 2, 3))

        assertTrue(apiFalsa.recibioArchivoAvatar)
        assertEquals("http://127.0.0.1:8080/api/usuarios/avatares/usuario_1_nuevo.jpg", actualizado.avatarUrl)
    }

    @Test
    fun sinConexion_lanzaExcepcionConMensajeDeRed() {
        apiFalsa.sinConexion = true

        val error = assertThrows(Exception::class.java) {
            runBlocking { repositorio.listarActivos() }
        }

        assertEquals("Error de red: IOException - Servidor apagado", error.message)
    }
}