/*permite a los usuarios iniciar sesión utilizando correo electrónico y contraseña,
o mediante inicio de sesión con Google. Incluye validaciones en tiempo real, manejo
de estados (carga, error, solicitud de número telefónico), gestión de temas oscuro/claro,
 control de teclado, y navegación entre pantallas. Utiliza un ViewModel (LoginViewModel)
 para manejar la lógica de autenticación, implementa un diálogo para solicitar número telefónico
 si es necesario, y proporciona opciones como recuperación de contraseña y registro de nuevos usuarios,
 todo con un diseño responsive y adaptable a diferentes temas.*/


package com.example.lumvida.ui.login.ui // Define el paquete donde se encuentra esta clase.

import android.app.Activity // Importa la clase Activity.
import androidx.activity.compose.rememberLauncherForActivityResult // Importa el método para manejar resultados de actividades.
import androidx.activity.result.contract.ActivityResultContracts // Importa contratos para resultados de actividades.
import androidx.compose.foundation.isSystemInDarkTheme // Importa función para comprobar si el sistema está en tema oscuro.
import androidx.compose.foundation.layout.* // Importa funciones para el diseño de disposición de elementos.
import androidx.compose.foundation.text.KeyboardActions // Importa acciones de teclado.
import androidx.compose.foundation.text.KeyboardOptions // Importa opciones de teclado.
import androidx.compose.material.icons.Icons // Importa iconos de Material Design.
import androidx.compose.material.icons.filled.Visibility // Importa el icono de visibilidad.
import androidx.compose.material.icons.filled.VisibilityOff // Importa el icono de visibilidad desactivada.
import androidx.compose.material3.* // Importa componentes de Material Design 3.
import androidx.compose.runtime.* // Importa funciones y tipos de Compose relacionados con el estado.
import androidx.compose.ui.Alignment // Importa alineaciones para el diseño.
import androidx.compose.ui.ExperimentalComposeUiApi // Importa la anotación para usar API experimentales de Compose.
import androidx.compose.ui.Modifier // Importa la clase Modifier para modificar elementos.
import androidx.compose.ui.focus.FocusRequester // Importa la clase para solicitar enfoque en campos.
import androidx.compose.ui.focus.focusRequester // Importa el modificador para solicitar enfoque.
import androidx.compose.ui.graphics.Color // Importa la clase Color para manejar colores.
import androidx.compose.ui.platform.LocalContext // Importa el contexto local.
import androidx.compose.ui.platform.LocalSoftwareKeyboardController // Importa el controlador del teclado.
import androidx.compose.ui.text.input.ImeAction // Importa acciones IME para el teclado.
import androidx.compose.ui.text.input.KeyboardType // Importa tipos de teclado.
import androidx.compose.ui.text.input.PasswordVisualTransformation // Importa transformación visual para contraseñas.
import androidx.compose.ui.text.input.VisualTransformation // Importa transformación visual para textos.
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp // Importa la clase para manejar unidades de densidad.
import androidx.lifecycle.viewmodel.compose.viewModel // Importa el método para obtener ViewModels en Compose.
import androidx.navigation.NavController // Importa el controlador de navegación.
import com.example.lumvida.ui.Auth.ui.AuthViewModel // Importa el ViewModel de autenticación.
import com.example.lumvida.ui.theme.* // Importa temas personalizados.
import com.google.android.gms.auth.api.signin.GoogleSignIn // Importa la clase para iniciar sesión con Google.
import com.google.android.gms.auth.api.signin.GoogleSignInOptions // Importa opciones de inicio de sesión de Google.
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes // Importa códigos de estado de inicio de sesión.
import com.google.android.gms.common.api.ApiException // Importa la clase para manejar excepciones de API.
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class) // Anota la función para usar API experimentales.
@Composable // Anota la función como composable.
fun LoginScreen(
    navController: NavController, // Controlador de navegación para navegar entre pantallas.
    authViewModel: AuthViewModel, // ViewModel para manejar la autenticación.
    loginViewModel: LoginViewModel = viewModel( // Obtiene el LoginViewModel, usando un Factory para la inyección.
        factory = LoginViewModel.Factory(authViewModel)
    ),
    isDarkTheme: Boolean = isSystemInDarkTheme() // Determina si se usa un tema oscuro.
) {
    // Estado para el correo del usuario.
    var email by remember { mutableStateOf("") }
    // Estado para la contraseña del usuario.
    var password by remember { mutableStateOf("") }
    // Estado para mostrar u ocultar la contraseña.
    var passwordVisible by remember { mutableStateOf(false) }

    var showPhoneDialog by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(null) }

    // Estado de inicio de sesión desde el LoginViewModel.
    val loginState by loginViewModel.loginState.collectAsState()
    // Contexto de la actividad actual.
    val context = LocalContext.current
    // Mensaje de error que se mostrará al usuario, inicializado como nulo.
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Controlador del teclado para manejar la entrada del usuario.
    val keyboardController = LocalSoftwareKeyboardController.current
    // Solicitud de enfoque para el campo de contraseña.
    val passwordFocusRequester = remember { FocusRequester() }

    // Configuración de opciones para el inicio de sesión con Google.
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(com.example.lumvida.R.string.default_web_client_id)) // Solicita el ID de token.
        .requestEmail() // Solicita el correo electrónico del usuario.
        .build() // Construye las opciones de inicio de sesión.
    // Crea un cliente de inicio de sesión de Google.
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Configura el launcher para manejar el resultado del inicio de sesión con Google.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult() // Define el contrato para resultados de actividad.
    ) { result -> // Maneja el resultado de la actividad.
        if (result.resultCode == Activity.RESULT_OK) { // Si el resultado es OK.
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data) // Obtiene la cuenta firmada.
            try {
                val account = task.getResult(ApiException::class.java) // Intenta obtener el resultado.
                if (account?.email == null) { // Verifica si se obtuvo el correo electrónico.
                    errorMessage = "No se pudo obtener el correo electrónico de Google" // Mensaje de error.
                } else {
                    // Si se obtuvo la cuenta, intenta iniciar sesión con ella.
                    account.let { loginViewModel.loginWithGoogle(it) }
                }
            } catch (e: ApiException) { // Manejo de excepciones.
                // Asigna mensajes de error según el código de estado.
                errorMessage = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Inicio de sesión cancelado" // Inicio de sesión cancelado.
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Error de red. Verifica tu conexión" // Error de red.
                    else -> "Error en el inicio de sesión con Google: ${e.message}" // Otros errores.
                }
            }
        }
    }

    // Efecto para manejar el estado de inicio de sesión.
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.NeedsPhoneNumber -> {
                currentUser = (loginState as LoginState.NeedsPhoneNumber).user
                showPhoneDialog = true
            }
            is LoginState.Error -> { // Si hay un error en el inicio de sesión.
                errorMessage = (loginState as LoginState.Error).message // Obtiene el mensaje de error.
            }
            else -> {} // No hace nada si no hay cambios.
        }
    }

    if (showPhoneDialog && currentUser != null) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "Número de teléfono requerido",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            text = {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { newValue ->
                        // Solo permite números y limita a 10 dígitos
                        if (newValue.length <= 10) {
                            phoneNumber = newValue.filter { it.isDigit() }
                        }
                    },
                    label = {
                        Text(
                            "Teléfono (10 dígitos)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) TextPrimary else PrimaryDark
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Secondary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        loginViewModel.submitPhoneNumber(currentUser!!, phoneNumber)
                        showPhoneDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        "Guardar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }

    // Contenedor de fondo que se adapta al tema.
    BackgroundContainer(isDarkTheme = isDarkTheme) {
        // Columna para organizar los elementos verticalmente.
        Column(
            modifier = Modifier
                .fillMaxSize() // Ocupa todo el tamaño disponible.
                .padding(32.dp), // Añade un relleno.
            horizontalAlignment = Alignment.CenterHorizontally, // Alinea horizontalmente al centro.
            verticalArrangement = Arrangement.Center // Alinea verticalmente al centro.
        ) {
            // Título de la pantalla de inicio de sesión.
            Text(
                text = "Iniciar Sesión", // Texto del título.
                style = MaterialTheme.typography.displaySmall, // Estilo del texto.
                color = if (isDarkTheme) TextPrimary else PrimaryDark, // Color del texto según el tema.
                modifier = Modifier.padding(bottom = 32.dp) // Añade un relleno inferior.
            )

            // Campo para ingresar el correo electrónico.
            OutlinedTextField(
                value = email, // Valor actual del campo.
                onValueChange = { email = it.filter { c -> c != ' ' } }, // Actualiza el valor y elimina espacios.
                label = { Text( // Etiqueta del campo.
                    "Correo electrónico",
                    style = MaterialTheme.typography.bodyMedium, // Estilo del texto.
                    color = if (isDarkTheme) TextPrimary else PrimaryDark // Color según el tema.
                ) },
                keyboardOptions = KeyboardOptions( // Opciones del teclado.
                    keyboardType = KeyboardType.Email, // Tipo de teclado: correo electrónico.
                    imeAction = ImeAction.Next // Acción del teclado: siguiente.
                ),
                keyboardActions = KeyboardActions( // Acciones del teclado.
                    onNext = { passwordFocusRequester.requestFocus() } // Solicita enfoque en el campo de contraseña.
                ),
                singleLine = true, // Solo una línea de entrada.
                colors = OutlinedTextFieldDefaults.colors( // Colores del campo.
                    focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark, // Color del texto enfocado.
                    unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark, // Color del texto no enfocado.
                    focusedBorderColor = Primary, // Color del borde enfocado.
                    unfocusedBorderColor = Secondary // Color del borde no enfocado.
                ),
                modifier = Modifier
                    .fillMaxWidth() // Ocupa todo el ancho disponible.
                    .padding(bottom = 16.dp) // Añade un relleno inferior.
            )

            // Campo para ingresar la contraseña.
            OutlinedTextField(
                value = password, // Valor actual del campo.
                onValueChange = { password = it.filter { c -> c != ' ' } }, // Actualiza el valor y elimina espacios.
                label = { Text("Contraseña",  style = MaterialTheme.typography.bodyMedium, color = if (isDarkTheme) TextPrimary else PrimaryDark) }, // Etiqueta del campo.
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), // Muestra u oculta la contraseña.
                keyboardOptions = KeyboardOptions( // Opciones del teclado.
                    keyboardType = KeyboardType.Password, // Tipo de teclado: contraseña.
                    imeAction = ImeAction.Done // Acción del teclado: hecho.
                ),
                keyboardActions = KeyboardActions( // Acciones del teclado.
                    onDone = {
                        keyboardController?.hide() // Oculta el teclado.
                        loginViewModel.login(email, password) // Inicia sesión con las credenciales ingresadas.
                    }
                ),
                singleLine = true, // Solo una línea de entrada.
                colors = OutlinedTextFieldDefaults.colors( // Colores del campo.
                    focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark, // Color del texto enfocado.
                    unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark, // Color del texto no enfocado.
                    focusedBorderColor = Primary, // Color del borde enfocado.
                    unfocusedBorderColor = Secondary // Color del borde no enfocado.
                ),
                modifier = Modifier
                    .fillMaxWidth() // Ocupa todo el ancho disponible.
                    .focusRequester(passwordFocusRequester), // Solicita enfoque en este campo.
                trailingIcon = { // Icono al final del campo.
                    IconButton(onClick = { passwordVisible = !passwordVisible }) { // Cambia la visibilidad de la contraseña al hacer clic.
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, // Muestra el icono adecuado.
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña", // Descripción del icono.
                            tint = if (isDarkTheme) TextPrimary else PrimaryDark // Color del icono según el tema.
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp)) // Espaciador vertical.

            // Botón para iniciar sesión.
            Button(
                onClick = { loginViewModel.login(email, password) }, // Acción al hacer clic: iniciar sesión.
                modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho disponible.
                colors = ButtonDefaults.buttonColors( // Colores del botón.
                    containerColor = Primary, // Color de fondo del botón.
                    contentColor = TextPrimary // Color del contenido del botón.
                )
            ) {
                Text( // Texto dentro del botón.
                    "Iniciar sesión",
                    style = MaterialTheme.typography.labelLarge // Estilo del texto.
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Espaciador vertical.

            // Botón para iniciar sesión con Google.
            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) }, // Acción al hacer clic: inicia el proceso de inicio de sesión de Google.
                modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho disponible.
                colors = ButtonDefaults.outlinedButtonColors( // Colores del botón.
                    contentColor = if (isDarkTheme) TextPrimary else PrimaryDark // Color del contenido según el tema.
                )
            ) {
                Text("Iniciar sesión con Google") // Texto del botón.
            }

            Spacer(modifier = Modifier.height(16.dp)) // Espaciador vertical.

            // Botón "¿Olvidaste tu contraseña?"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = {
                        // Aseguramos que la navegación funcione
                        navController.navigate("recuperar_contrasena") {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                }
            }

            // Botón "¿No tienes una cuenta?"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = {
                        // Aseguramos que la navegación funcione
                        navController.navigate("crear_cuenta") {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text(
                        text = "¿No tienes una cuenta? Regístrate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                }
            }

            // Mostrar mensaje de error si existe
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Indicador de carga
            if (loginState is LoginState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp),
                    color = Primary
                )
            }

        }
    }
}
