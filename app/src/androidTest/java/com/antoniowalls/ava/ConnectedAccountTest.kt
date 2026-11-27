package com.antoniowalls.ava

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ConnectedAccountTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun registersSignsOutAndSignsBackInWithLocalFirebase() {
        assumeFalse(BuildConfig.USE_CLOUD_FIREBASE)
        val id = UUID.randomUUID().toString().take(7)
        val email = "ava-$id@example.test"
        val password = "ava-test-${UUID.randomUUID()}"

        tap(R.string.ui_crear_cuenta)
        waitFor(R.string.ui_registrate)
        fill(0, email)
        fill(1, "5551234")
        fill(2, password)
        fill(3, "Cuenta de prueba")
        fill(4, "Prueba$id")
        tap(R.string.ui_crear_cuenta)
        waitFor(R.string.ui_omitir_por_ahora)
        tap(R.string.ui_omitir_por_ahora)
        waitFor(R.string.ui_selecciona_un_modulo_para_comenzar)

        tap(R.string.ui_foro)
        val editProfile = compose.activity.getString(R.string.ui_foto_de_perfil)
        compose.onNode(
            androidx.compose.ui.test.hasContentDescription(editProfile)
        ).performClick()
        waitFor(R.string.ui_editar_perfil)
        tap(R.string.ui_cerrar_sesion)
        waitFor(R.string.ui_inicia_sesion)
        fill(0, email)
        fill(1, password)
        tap(R.string.ui_iniciar_sesion)
        waitFor(R.string.ui_selecciona_un_modulo_para_comenzar)
    }

    private fun fill(index: Int, text: String) {
        compose.onAllNodes(hasSetTextAction())[index].performTextInput(text)
    }

    private fun tap(id: Int) {
        compose.onNodeWithText(compose.activity.getString(id)).performClick()
    }

    private fun waitFor(id: Int) {
        val text = compose.activity.getString(id)
        compose.waitUntil(20_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
}
