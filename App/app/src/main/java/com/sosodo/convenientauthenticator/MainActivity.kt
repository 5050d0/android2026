package com.sosodo.convenientauthenticator

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sosodo.convenientauthenticator.ui.theme.ConvenientAuthenticatorTheme

class MainActivity : ComponentActivity() {
    val tag = "MainActivityLifecycle"


    private val selectedEntry = mutableStateOf<Entry?>(null)
    private val linkError = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(tag, "onCreate: savedInstanceState=${savedInstanceState}")
        enableEdgeToEdge()

        if (savedInstanceState != null) {
            restoreScreenState(savedInstanceState)
        } else {
            handleLink(intent)
        }

        setContent {
            ConvenientAuthenticatorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        val entry = selectedEntry.value
                        val error = linkError.value
                        when {
                            error != null -> {
                                Text(text = error)
                            }

                            entry != null -> {
                                TotpBlock(entry = entry, shareEntry = ::shareEntry)
                            }

                            else -> {
                                Greeting()
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        Log.i(tag, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i(tag, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(tag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(tag, "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(tag, "onRestart")
    }

    override fun onDestroy() {
        Log.i(tag, "onDestroy")
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(tag, "onNewIntent")
        setIntent(intent)
        handleLink(intent)
    }

    private fun handleLink(intent: Intent) {
        if (intent.action != Intent.ACTION_VIEW) return

        val link = intent.dataString ?: return

        val entry = parseOtpAuthLink(link)
        selectedEntry.value = entry
        linkError.value = if (entry == null) {
            "Не удалось открыть ссылку"
        } else {
            null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.i(tag, "onSaveInstanceState")
        val entry = selectedEntry.value

        if (entry != null) {
            outState.putString("entry_link", generateOtpAuthLink(entry))
            outState.putString("entry_uuid", entry.uuid.toString())
        }

        outState.putString("link_error", linkError.value)

        super.onSaveInstanceState(outState)
    }

    private fun restoreScreenState(state: Bundle) {
        Log.i(tag, "restoreScreenState")
        linkError.value = state.getString("link_error")

        val link = state.getString("entry_link") ?: return

        val entry = parseOtpAuthLink(link)
        if (entry == null) {
            selectedEntry.value = null
            linkError.value = "Не удалось восстановить запись"
            return
        }

        val uuid = state.getString("entry_uuid")
        if (uuid != null) {
            entry.uuid = java.util.UUID.fromString(uuid)
        }

        selectedEntry.value = entry
        linkError.value = null
    }

    private fun shareEntry(entry: Entry) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, generateOtpAuthLink(entry))

        startActivity(
            Intent.createChooser(shareIntent, "Экспортировать запись")
        )
    }
}


@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(
        text = "Hello, user!",
        modifier = modifier
    )
}


@Composable
fun TotpBlock(entry: Entry, modifier: Modifier = Modifier, shareEntry: (Entry) -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = entry.issuer,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = entry.username,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = entry.getCode().toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Button(onClick = { shareEntry(entry) }) {
                Text("Поделиться")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TotpBlockPreview() {
    ConvenientAuthenticatorTheme {
        TotpBlock(
            Entry(
                "secret",
                "issuer",
                "user",
                period = 30,
                hashAlgorithm = HashAlgorithm.SHA1,
                digits = 6
            ),
            shareEntry = {}
        )
    }
}
