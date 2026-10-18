package cl.duoc.rinconsurapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.rinconsurapp.data.RinconSurDbHelper
import cl.duoc.rinconsurapp.data.SesionPreferencias
import cl.duoc.rinconsurapp.model.RegistroObservacion
import cl.duoc.rinconsurapp.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistorialAdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RinconSurDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _registros = MutableStateFlow<List<RegistroObservacion>>(emptyList())
    val registros: StateFlow<List<RegistroObservacion>> = _registros.asStateFlow()

    private val _autorizado = MutableStateFlow(false)
    val autorizado: StateFlow<Boolean> = _autorizado.asStateFlow()

    init {
        cargarRegistros()
    }

    fun cargarRegistros() {
        val usuario = db.buscarUsuarioPorId(sesion.leer())

        if (usuario?.rolId == Rol.ID_ADMIN) {
            _autorizado.value = true
            _registros.value = db.listarTodosLosRegistros()
        } else {
            _autorizado.value = false
            _registros.value = emptyList()
        }
    }


    fun eliminarRegistro(idRegistro: Long) {
        val usuario = db.buscarUsuarioPorId(sesion.leer())

        if (usuario?.rolId == Rol.ID_ADMIN) {
            db.eliminarRegistro(idRegistro)
            cargarRegistros()
        }
    }


    override fun onCleared() {
        super.onCleared()
        db.close()
    }
}