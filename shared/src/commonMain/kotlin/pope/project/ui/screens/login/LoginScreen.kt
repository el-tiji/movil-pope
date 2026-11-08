package pope.project.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import pope.project.domain.repository.AuthRepository
import pope.project.ui.components.FormField
import pope.project.ui.components.PopeButton
import pope.project.ui.components.PopeCard
import pope.project.ui.components.PopeTextField
import pope.project.ui.icons.PopeIcons
import pope.project.ui.theme.PopeTheme

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun login() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.login(current.email, current.password)
            _state.update {
                it.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::login,
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    val c = PopeTheme.colors
    Box(Modifier.fillMaxSize().background(c.bg)) {
        // Navy hero behind the status bar, like the app header.
        Box(Modifier.fillMaxWidth().height(300.dp).background(c.navy))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(c.navyMid),
                contentAlignment = Alignment.Center,
            ) {
                Icon(PopeIcons.Sparkle, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Pope Cleaning", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                "PORTAL DEL PERSONAL",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                letterSpacing = 0.1.em,
            )
            Spacer(Modifier.height(40.dp))

            PopeCard(modifier = Modifier.widthIn(max = 440.dp), spacing = 16.dp) {
                Text("Iniciar sesión", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.fg)
                Text("Accede para ver tu turno de hoy.", fontSize = 14.sp, color = c.fgMid)
                FormField("Correo electrónico") {
                    PopeTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        placeholder = "tu@popecleaning.com.au",
                        keyboardType = KeyboardType.Email,
                        isError = state.error != null,
                    )
                }
                FormField("Contraseña") {
                    PopeTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        placeholder = "••••••••",
                        isPassword = true,
                        isError = state.error != null,
                    )
                }
                if (state.error != null) {
                    Text(state.error, color = c.danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                PopeButton(
                    text = "Entrar",
                    onClick = onLogin,
                    icon = PopeIcons.LogIn,
                    enabled = state.canSubmit,
                    loading = state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Demo: cualquier correo válido y contraseña.",
                    fontSize = 12.sp,
                    color = c.fgDim,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
