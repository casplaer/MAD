package com.example.calculator

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.calculator.ui.theme.CalculatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class SignInActivity : ComponentActivity() {
    private lateinit var credentialManager: CredentialManager

    private lateinit var resultMessage: MutableState<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        credentialManager = CredentialManager.create(context = this)

        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        SignInScreen()
                    }
                }
            }
        }
    }

    @Composable
    fun SignInScreen() {
        var isLoginFocus by rememberSaveable { mutableStateOf(false) }
        var usernameSignIn by rememberSaveable { mutableStateOf("") }
        var passwordSignIn by rememberSaveable { mutableStateOf("") }
        val isSignInWithPassKeyProgress = rememberSaveable { mutableStateOf(false) }
        val isSignInWithPasswordProgress = rememberSaveable { mutableStateOf(false) }

        val usernameLogIn = rememberSaveable { mutableStateOf("") }
        val passwordLogIn = rememberSaveable { mutableStateOf("") }
        val isLogInProgress = rememberSaveable { mutableStateOf(false) }

        resultMessage = rememberSaveable { mutableStateOf("") }

        LaunchedEffect(resultMessage.value) {
            delay(3000)
            resultMessage.value = ""
        }

        if (isLogInProgress.value || isSignInWithPasswordProgress.value || isSignInWithPassKeyProgress.value) {
            resultMessage.value = ""
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                shape = RoundedCornerShape(14.dp)
            ) {
                val modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                Column(
                    modifier = Modifier.padding(4.dp, if (isLoginFocus) 0.dp else 4.dp, 4.dp, 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(visible = !isLoginFocus) {
                        Column {
                            TextField(
                                modifier = modifier,
                                shape = RoundedCornerShape(8.dp),
                                value = usernameSignIn,
                                placeholder = {
                                    Text("Username")
                                },
                                onValueChange = {
                                    usernameSignIn = it
                                })
                            TextField(
                                modifier = modifier,
                                shape = RoundedCornerShape(8.dp),
                                value = passwordSignIn,
                                placeholder = {
                                    Text("Password")
                                },
                                onValueChange = {
                                    passwordSignIn = it
                                })
                            Button(
                                modifier = modifier,
                                enabled = !isSignInWithPassKeyProgress.value,
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    lifecycleScope.launch {
                                        if (usernameSignIn.isEmpty()) {
                                            resultMessage.value = "Username cannot be empty"
                                            return@launch
                                        }
                                        createPassKey(usernameSignIn, isSignInWithPassKeyProgress)
                                    }
                                }) {
                                if (isSignInWithPassKeyProgress.value)
                                    CircularProgressIndicator(
                                        Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                else
                                    Text(text = "SignIn With PassKey")
                            }
                        }
                    }
                    Button(
                        modifier = modifier,
                        enabled = !isSignInWithPasswordProgress.value,
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            if (isLoginFocus) {
                                isLoginFocus = false
                            } else {
                                lifecycleScope.launch {
                                    if (usernameSignIn.isEmpty() && passwordSignIn.isEmpty()) {
                                        resultMessage.value =
                                            "Username and Password cannot be empty"
                                        return@launch
                                    }
                                    if (usernameSignIn.isEmpty()) {
                                        resultMessage.value = "Username cannot be empty"
                                        return@launch
                                    }
                                    if (passwordSignIn.isEmpty()) {
                                        resultMessage.value = "Password cannot be empty"
                                        return@launch
                                    }
                                    signInPassword(
                                        usernameSignIn,
                                        passwordSignIn,
                                        isSignInWithPasswordProgress
                                    )
                                }
                            }
                        }) {
                        if (isSignInWithPasswordProgress.value)
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        else
                            if (isLoginFocus)
                                Text(text = "SignIn")
                            else
                                Text(text = "SignIn With Password")
                    }
                }
            }
            ElevatedCard(
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp, if (isLoginFocus) 4.dp else 0.dp, 4.dp, 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                    AnimatedVisibility(visible = isLoginFocus) {
                        Column {
                            TextField(
                                modifier = modifier,
                                shape = RoundedCornerShape(8.dp),
                                value = usernameLogIn.value,
                                placeholder = {
                                    Text("Username")
                                },
                                onValueChange = {
                                    usernameLogIn.value = it
                                }
                            )
                            TextField(
                                modifier = modifier,
                                shape = RoundedCornerShape(8.dp),
                                value = passwordLogIn.value,
                                placeholder = {
                                    Text("Password")
                                },
                                onValueChange = {
                                    passwordLogIn.value = it
                                }
                            )
                        }
                    }
                    Button(
                        modifier = modifier,
                        enabled = !isLogInProgress.value,
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            if (isLoginFocus)
                                lifecycleScope.launch {
                                    signInPassKey(isLogInProgress, usernameLogIn, passwordLogIn)
                                }
                            else
                                isLoginFocus = true

                        }) {
                        if (isLogInProgress.value)
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        else
                            Text(text = "Login")
                    }
                }
            }

            AnimatedVisibility(visible = resultMessage.value.isNotEmpty()) {
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Result", fontWeight = FontWeight.Bold)
                        Text(text = resultMessage.value)
                    }
                }
            }
        }
    }

    private suspend fun signInPassword(
        username: String,
        password: String,
        signInWithPasswordProgress: MutableState<Boolean>
    ) {
        signInWithPasswordProgress.value = true
        try {
            val response = credentialManager.createCredential(
                context = this,
                request = CreatePasswordRequest(id = username, password = password)
            )
            resultMessage.value = response.data.toString()
            Log.d("MainActivity", "response: $response")
            signInWithPasswordProgress.value = false
        } catch (e: Exception) {
            e.printStackTrace()
            resultMessage.value = e.localizedMessage ?: ""
            signInWithPasswordProgress.value = false
        }
    }

    private suspend fun createPassKey(username: String, isSignInProgress: MutableState<Boolean>) {
        isSignInProgress.value = true
        val request = CreatePublicKeyCredentialRequest(getRegistrationRequest(username))
        try {
            val response = credentialManager.createCredential(
                context = this,
                request = request
            ) as CreatePublicKeyCredentialResponse
            resultMessage.value = response.registrationResponseJson
            isSignInProgress.value = false
        } catch (e: CreateCredentialException) {
            e.printStackTrace()
            resultMessage.value = e.localizedMessage ?: ""
            isSignInProgress.value = false
        }
    }

    private suspend fun signInPassKey(
        isLogInProgress: MutableState<Boolean>,
        usernameLogIn: MutableState<String>,
        passwordLogIn: MutableState<String>
    ) {
        isLogInProgress.value = true

        try {
            val response = credentialManager.getCredential(
                context = this,
                request = GetCredentialRequest(
                    listOf(
                        GetPublicKeyCredentialOption(getLoginRequest()),
                        GetPasswordOption()
                    )
                )
            )
            Log.d(
                "MainActivity",
                "signInPassKey: ${handleLoginResponse(response, usernameLogIn, passwordLogIn)}"
            )
            isLogInProgress.value = false
        } catch (e: Exception) {
            e.printStackTrace()
            resultMessage.value = e.localizedMessage ?: ""
            isLogInProgress.value = false
        }
    }

    private fun handleLoginResponse(
        response: GetCredentialResponse,
        usernameLogIn: MutableState<String>,
        passwordLogIn: MutableState<String>
    ): String {
        if (response.credential is PublicKeyCredential) {
            val cred = response.credential as PublicKeyCredential
            resultMessage.value = cred.authenticationResponseJson
            return "Passkey: ${cred.authenticationResponseJson}"
        } else if (response.credential is PasswordCredential) {
            val cred = response.credential as PasswordCredential
            usernameLogIn.value = cred.id
            passwordLogIn.value = cred.password
            resultMessage.value = "UserId:${cred.id} Password: ${cred.password}"
            return "Got Password - User:${cred.id} Password: ${cred.password}"
        }
        return ""
    }

    private fun getRegistrationRequest(username: String): String {
        return assets.open("registrationRequest.json")
            .bufferedReader().use { it.readText() }
            .replace("<userId>", getEncodedUserId(username))
            .replace("<userName>", username.lowercase())
            .replace("<userDisplayName>", username)
            .replace("<challenge>", getEncodedChallenge())
            .replace("<relyingPartyId>", "confab.glitch.me")
    }

    private fun getLoginRequest(): String {
        return assets.open("loginRequest.json")
            .bufferedReader().use { it.readText() }
            .replace("<challenge>", getEncodedChallenge())
            .replace("<relyingPartyId>", "confab.glitch.me")
    }

    private fun getEncodedUserId(username: String): String {
        return Base64.encodeToString(username.toByteArray(), Base64.NO_WRAP)
    }

    private fun getEncodedChallenge(): String {
        val challenge = Random(100).nextBytes(16)
        return Base64.encodeToString(challenge, Base64.NO_WRAP)
    }
}