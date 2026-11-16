package yesweburp.ui.options

import swinger.*
import yesweburp.Events.programsLoaded
import yesweburp.Settings
import yesweburp.VERSION
import yesweburp.burpApi
import yesweburp.api.API
import yesweburp.api.AuthMethod
import yesweburp.api.Program
import yesweburp.ui.DisabledTextField
import yesweburp.ui.Padding
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import javax.swing.Box.createHorizontalGlue
import javax.swing.JTextField
import javax.swing.SwingWorker

class APIPanel : GridBagPanel() {

    private val errorMsg = DisabledTextField()

    init {
        alignmentX = LEFT_ALIGNMENT
        addTitleBorder("API")
        val auth = AuthMethod.fromValue(Settings.authMethod)

        val email = TextField(Settings.userEmail) {
            isEnabled = auth.requireEmail
        }
        val password = PasswordField(Settings.userPassword) {
            isEnabled = auth.requirePassword
        }
        val otp = TextField {
            isEnabled = auth.requireOTP
        }
        val rememberPassword = CheckBox {
            isSelected = Settings.rememberPassword
            isEnabled = auth.requirePassword
        }

        errorMsg.isVisible = false
        val authMethodList = ComboBox(AuthMethod.labels()) {
            selectedIndex = values.indexOf(auth.label)
            onChange { selected ->
                val method = AuthMethod.fromValue(selected)
                email.isEnabled = method.requireEmail
                password.isEnabled = method.requirePassword
                rememberPassword.isEnabled = method.requirePassword
                otp.isEnabled = method.requireOTP
            }
        }

        val fetchButton = Button("Fetch programs")
        fetchButton.onClick {
            val authEmail = email.text
            val authPassword = password.value
            val authOTP = otp.text
            val authMethod = AuthMethod.fromValue(authMethodList.selected)

            Settings.userEmail = authEmail
            if (rememberPassword.isSelected) {
                Settings.rememberPassword = true
                Settings.userPassword = authPassword
            } else {
                Settings.rememberPassword = false
                Settings.userPassword = ""
                password.value = ""
            }
            Settings.authMethod = authMethod.label
            otp.text = ""

            clearError()
            fetchButton.isEnabled = false
            FetchProgramsWorker(authMethod, authEmail, authPassword, authOTP, fetchButton).execute()
        }

        addSetting("Version", TextField(VERSION) {
            isEditable = false
            isOpaque = true
            isRequestFocusEnabled = false
            horizontalAlignment = JTextField.CENTER
        })
        addSetting("API URL", DisabledTextField("https://api.yeswehack.com"))
        addSetting("Authentication", authMethodList)
        addSetting("Email", email)
        addSetting("Password", password)
        addSetting("OTP", otp)
        addSetting("Remember password", rememberPassword)
        addSetting(null, fetchButton)
        add(
            errorMsg,
            gridX = 0,
            gridWidth = 2,
            weightX = 2.0,
            fill = GridBagConstraints.HORIZONTAL,
            insets = Padding(5),
            anchor = GridBagConstraints.WEST
        )
        capHeight()
    }

    /**
     * Authenticates and fetches programs off the EDT; [done] runs back on the EDT, so every
     * Swing mutation below stays on the right thread.
     */
    private inner class FetchProgramsWorker(
        private val authMethod: AuthMethod,
        private val email: String,
        private val password: String,
        private val otp: String,
        private val fetchButton: Button
    ) : SwingWorker<List<Program>, Unit>() {

        override fun doInBackground(): List<Program> {
            when (authMethod) {
                AuthMethod.ANONYMOUS -> API.auth()
                AuthMethod.EMAIL_PASSWORD -> API.auth(email, password)
                AuthMethod.EMAIL_PASSWORD_OTP -> API.auth(email, password, otp)
            }
            return API.fetchPrograms()
        }

        override fun done() {
            fetchButton.isEnabled = true
            try {
                programsLoaded.fire(get())
            } catch (e: Exception) {
                val cause = e.cause ?: e
                burpApi.logging().logToError("Failed to fetch programs", cause)
                showError(cause.message ?: cause.toString())
            }
        }
    }

    private fun clearError() {
        errorMsg.isVisible = false
        revalidate()
        capHeight()
    }

    private fun showError(message: String) {
        errorMsg.text = message
        errorMsg.isVisible = true
        revalidate()
        capHeight()
    }

    private fun capHeight() {
        maximumSize = Dimension(600, preferredSize.height)
    }

    private fun addSetting(label: String?, setting: Component) {
        val left = if (label == null) createHorizontalGlue() else Label("$label :")
        add(
            left,
            gridX = 0,
            insets = Padding(5),
            anchor = GridBagConstraints.WEST,
        )
        add(
            setting,
            gridX = 1,
            weightX = 1.0,
            fill = GridBagConstraints.HORIZONTAL,
            insets = Padding(5),
            anchor = GridBagConstraints.WEST
        )
    }
}
