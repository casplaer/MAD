package com.example.calculator

import PassKeyManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.fragment.app.FragmentActivity
import com.example.calculator.services.BiometricHelper


class PassKeyActivity : FragmentActivity() {

    private lateinit var passKeyManager: PassKeyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        passKeyManager = PassKeyManager(this)

        setContent {
            PassKeyScreen(
                onPassKeyVerified = { navigateToMainActivity() },
                passKeyManager = passKeyManager
            )
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun PassKeyScreen(
    onPassKeyVerified: () -> Unit,
    passKeyManager: PassKeyManager
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
        ?: throw IllegalStateException("PassKeyScreen must be hosted in a FragmentActivity")

    val biometricHelper = remember { BiometricHelper(activity) }

    var passKey by remember { mutableStateOf("") }
    var isPassKeySet by remember { mutableStateOf(passKeyManager.isPassKeyInitialized()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isPassKeySet) "Enter Pass Key" else "Create Pass Key",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = passKey,
            onValueChange = { passKey = it },
            label = { Text("Pass Key") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (passKey.length < 4) {
                    Toast.makeText(context, "Pass Key must be at least 4 digits", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (isPassKeySet) {
                    if (passKeyManager.validatePasskey(passKey)) {
                        Toast.makeText(context, "Pass Key Verified", Toast.LENGTH_SHORT).show()
                        onPassKeyVerified()
                    } else {
                        Toast.makeText(context, "Incorrect Pass Key", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    passKeyManager.generateNewPassKey(passKey)
                    Toast.makeText(context, "Pass Key Created", Toast.LENGTH_SHORT).show()
                    isPassKeySet = true
                    passKey = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isPassKeySet) "Enter Pass Key" else "Create Pass Key")
        }

        if (isPassKeySet && biometricHelper.isBiometricAvailable()) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    biometricHelper.authenticate(
                        onSuccess = {
                            passKeyManager.resetPassKey()
                            isPassKeySet = false
                            passKey = ""
                            Toast.makeText(context, "Pass Key Reset", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Toast.makeText(context, "Biometric Authentication Failed", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("Reset via Fingerprint")
            }
        }
    }
}

