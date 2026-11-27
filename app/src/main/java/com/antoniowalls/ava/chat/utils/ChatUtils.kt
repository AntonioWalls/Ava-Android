package com.antoniowalls.ava.chat.utils // Paquete corregido

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatUtils { // La clase original era Utils, la mantenemos así internamente pero el archivo es ChatUtils.kt

    companion object {
        private val auth = FirebaseAuth.getInstance()
        // El firestore instance aquí puede ser redundante si cada ViewModel/Repo maneja el suyo.
        // Por ahora lo dejamos si el código original lo usaba extensivamente desde aquí.
        // val firestore = FirebaseFirestore.getInstance()

        // Constantes que estaban en el Utils original
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2
        const val MESSAGE_RIGHT = 1 // Usado en el MessageAdapter original
        const val MESSAGE_LEFT = 2  // Usado en el MessageAdapter original

        // Este CHANNEL_ID es para notificaciones, asegúrate que coincida con el de FirebaseService
        // y Constants.kt dentro de la carpeta notifications.
        // const val CHAT_NOTIFICATION_CHANNEL_ID = "com.antoniowalls.ava.chat" // Ejemplo

        fun getUidLoggedIn(): String {
            return auth.currentUser?.uid ?: ""
        }

        /**
         * Returns the current time formatted as "HH:mm:ss".
         * El ChatAppViewModel original usaba esto para el campo 'time' de los mensajes.
         * Si tu modelo ChatMessage.time ahora es Date, esta función podría no ser necesaria
         * para el timestamp del mensaje en Firestore (ya que se usa @ServerTimestamp o new Date()),
         * pero el MessageAdapter original la usaba para mostrar la hora.
         * La dejamos por si la UI la necesita en ese formato.
         */
        fun getFormattedTime(): String {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return formatter.format(Date(System.currentTimeMillis()))
        }

        // Si necesitas la fecha actual como objeto Date:
        fun getCurrentDate(): Date {
            return Date(System.currentTimeMillis())
        }
    }
}