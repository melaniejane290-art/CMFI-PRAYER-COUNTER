package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.AppLanguage
import com.example.model.AuthMode
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SoftGold
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.PrayerCounterViewModel

/**
 * Material 3 Authentication Dialog supporting Google Sign-In, Email/Password login,
 * Registration, and Password Reset in English & French.
 */
@Composable
fun AuthDialog(
    viewModel: PrayerCounterViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val state by viewModel.uiState.collectAsState()
    val authUiState by viewModel.authUiState.collectAsState()
    val authUser by viewModel.authUser.collectAsState()

    val isFr = state.language == AppLanguage.FRENCH

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    // If user signs in successfully, safely dismiss dialog
    LaunchedEffect(authUser) {
        if (authUser != null && !authUiState.isLoading && authUiState.errorMessage == null) {
            keyboardController?.hide()
            focusManager.clearFocus()
            onDismissRequest()
        }
    }

    Dialog(
        onDismissRequest = {
            keyboardController?.hide()
            focusManager.clearFocus()
            viewModel.clearAuthMessages()
            onDismissRequest()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            border = BorderStroke(1.5.dp, SoftGold.copy(alpha = 0.5f)),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("auth_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Logo & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MidnightBlue,
                            border = BorderStroke(1.dp, SoftGold),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.praying_hands_logo_1787928549167),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isFr) "Compte de Prière" else "Prayer Account",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DeepNavy
                                )
                            )
                            Text(
                                text = if (isFr) "Sauvegarde & Synchronisation Cloud" else "Cloud Backup & Devotion Sync",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.clearAuthMessages()
                            onDismissRequest()
                        },
                        modifier = Modifier.testTag("btn_close_auth_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Tabs (Sign In / Sign Up)
                if (authUiState.currentAuthMode != AuthMode.FORGOT_PASSWORD) {
                    TabRow(
                        selectedTabIndex = if (authUiState.currentAuthMode == AuthMode.SIGN_IN) 0 else 1,
                        containerColor = OffWhite,
                        contentColor = DeepNavy,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(
                                    tabPositions[if (authUiState.currentAuthMode == AuthMode.SIGN_IN) 0 else 1]
                                ),
                                color = RoyalNavy,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = authUiState.currentAuthMode == AuthMode.SIGN_IN,
                            onClick = { viewModel.setAuthMode(AuthMode.SIGN_IN) },
                            text = {
                                Text(
                                    text = if (isFr) "Se Connecter" else "Sign In",
                                    fontWeight = if (authUiState.currentAuthMode == AuthMode.SIGN_IN) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("tab_auth_signin")
                        )
                        Tab(
                            selected = authUiState.currentAuthMode == AuthMode.SIGN_UP,
                            onClick = { viewModel.setAuthMode(AuthMode.SIGN_UP) },
                            text = {
                                Text(
                                    text = if (isFr) "Créer un Compte" else "Create Account",
                                    fontWeight = if (authUiState.currentAuthMode == AuthMode.SIGN_UP) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("tab_auth_signup")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Error Banner
                if (!authUiState.errorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("auth_error_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = authUiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Success Banner
                if (!authUiState.successMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AccentGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("auth_success_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = authUiState.successMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DeepNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Google Sign In Button (Prominent)
                if (authUiState.currentAuthMode != AuthMode.FORGOT_PASSWORD) {
                    OutlinedButton(
                        onClick = {
                            viewModel.signInWithGoogle(context)
                        },
                        enabled = !authUiState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = PureWhite,
                            contentColor = DeepNavy
                        ),
                        border = BorderStroke(1.5.dp, SoftGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_google_signin")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MidnightBlue,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = SoftGold,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isFr) "Continuer avec Google" else "Continue with Google",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Or with email divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = SoftGold.copy(alpha = 0.4f)
                        )
                        Text(
                            text = if (isFr) "ou avec votre email" else "or with email",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = SoftGold.copy(alpha = 0.4f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Form Fields
                when (authUiState.currentAuthMode) {
                    AuthMode.SIGN_IN -> {
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(if (isFr) "Adresse Email" else "Email Address") },
                            placeholder = { Text("prayer@example.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = RoyalNavy)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = authTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_signin_email")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (isFr) "Mot de passe" else "Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalNavy)
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (authUiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (authUiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.signInWithEmail(email, password)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = authTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_signin_password")
                        )

                        // Forgot Password Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.setAuthMode(AuthMode.FORGOT_PASSWORD) },
                                modifier = Modifier.testTag("btn_goto_forgot_password")
                            ) {
                                Text(
                                    text = if (isFr) "Mot de passe oublié ?" else "Forgot Password?",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = RoyalNavy,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Submit Sign In Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.signInWithEmail(email, password)
                            },
                            enabled = !authUiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepNavy,
                                contentColor = PureWhite
                            ),
                            border = BorderStroke(1.dp, SoftGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_signin")
                        ) {
                            if (authUiState.isLoading) {
                                CircularProgressIndicator(
                                    color = SoftGold,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = if (isFr) "Se Connecter" else "Sign In",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    AuthMode.SIGN_UP -> {
                        // Display Name
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text(if (isFr) "Nom complet ou prénom" else "Full Name / Display Name") },
                            placeholder = { Text("Melanie Jane") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = RoyalNavy)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = authTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_signup_name")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(if (isFr) "Adresse Email" else "Email Address") },
                            placeholder = { Text("prayer@example.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = RoyalNavy)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = authTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_signup_email")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (isFr) "Mot de passe (min. 6 car.)" else "Password (min. 6 chars)") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalNavy)
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (authUiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (authUiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = authTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_signup_password")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Confirm Password Field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(if (isFr) "Confirmer le mot de passe" else "Confirm Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalNavy)
                            },
                            visualTransformation = if (authUiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.signUpWithEmail(email, password, confirmPassword, displayName)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = authTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_signup_confirm_password")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Sign Up Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.signUpWithEmail(email, password, confirmPassword, displayName)
                            },
                            enabled = !authUiState.isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepNavy,
                                contentColor = PureWhite
                            ),
                            border = BorderStroke(1.dp, SoftGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_signup")
                        ) {
                            if (authUiState.isLoading) {
                                CircularProgressIndicator(
                                    color = SoftGold,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = if (isFr) "Créer mon Compte" else "Create Account",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    AuthMode.FORGOT_PASSWORD -> {
                        Text(
                            text = if (isFr) "Réinitialisation du mot de passe" else "Reset Your Password",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isFr) {
                                "Entrez l'adresse email associée à votre compte pour recevoir un lien de réinitialisation sécurisé."
                            } else {
                                "Enter the email address registered with your prayer account to receive a secure password reset link."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reset Email Input
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(if (isFr) "Adresse Email" else "Email Address") },
                            placeholder = { Text("prayer@example.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = RoyalNavy)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.sendPasswordReset(email)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = authTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_forgot_email")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.sendPasswordReset(email)
                            },
                            enabled = !authUiState.isLoading && email.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepNavy,
                                contentColor = PureWhite
                            ),
                            border = BorderStroke(1.dp, SoftGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_forgot_password")
                        ) {
                            if (authUiState.isLoading) {
                                CircularProgressIndicator(
                                    color = SoftGold,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = if (isFr) "Envoyer le lien" else "Send Reset Link",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = { viewModel.setAuthMode(AuthMode.SIGN_IN) },
                            modifier = Modifier.testTag("btn_back_to_signin")
                        ) {
                            Text(
                                text = if (isFr) "Retour à la connexion" else "Back to Sign In",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = RoyalNavy,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = OffWhite,
    unfocusedContainerColor = OffWhite,
    focusedBorderColor = RoyalNavy,
    unfocusedBorderColor = SoftGold.copy(alpha = 0.5f),
    focusedTextColor = DeepNavy,
    unfocusedTextColor = DeepNavy
)
