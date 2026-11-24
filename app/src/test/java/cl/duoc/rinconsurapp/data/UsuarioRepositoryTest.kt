package cl.duoc.rinconsurapp.data

import cl.duoc.rinconsurapp.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        assertEquals(
            RetrofitClient.resolverUrl("/api/usuarios/avatares/usuario_1.jpg"),
            usuario.avatarUrl
        )
    }

    @Test
    fun listarActivos_convierteListaCompleta() = runBlocking {
        val lista = repositorio.listarActivos()

        assertEquals(2, lista.size)
        assertEquals("Administrador", lista[1].rolNombre)
        assertNull(lista[1].avatarUrl)
    }

}