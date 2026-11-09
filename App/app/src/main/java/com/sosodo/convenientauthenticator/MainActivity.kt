package com.sosodo.convenientauthenticator

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                AuthenticatorScreen {}
            }
//            ConvenientAuthenticatorTheme {
//                Scaffold(
//                    modifier = Modifier.fillMaxSize()
//                ) { innerPadding ->
//                    Column(
//                        modifier = Modifier.padding(innerPadding)
//                    ) {
//                        val entry = selectedEntry.value
//                        val error = linkError.value
//                        when {
//                            error != null -> {
//                                Text(text = error)
//                            }
//
//                            entry != null -> {
//                                TotpBlock(entry = entry, shareEntry = ::shareEntry)
//                            }
//
//                            else -> {
//                                Greeting()
//                            }
//                        }
//                    }
//                }
//            }
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.issuer.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = entry.issuer,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        maxLines = 1,
                    )
                    Text(
                        text = entry.username,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = entry.getCode().toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 57.sp,
                    lineHeight = 58.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    ),
                )
            }
        }
    }
}


@Preview(name = "TOTP - Light", showBackground = true)
@Preview(
    name = "TOTP - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun TotpBlockPreview() {
    ConvenientAuthenticatorTheme(dynamicColor = false) {
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


@Composable
fun AuthenticatorScreen(
    onAdd: () -> Unit = {},
    onMenu: () -> Unit = {},
) {

    val entries = listOf(
        Entry(issuer = "Google", username = "example@gmail.com"),
        Entry(issuer = "Yandex", username = "example2@ya.ru"),
        Entry(issuer = "Github", username = "example@gmail.com"),
        Entry(issuer = "Google", username = "example@gmail.com"),
        Entry(issuer = "Yandex", username = "example2@ya.ru"),
        Entry(issuer = "Github", username = "example@gmail.com"),
        Entry(issuer = "Google", username = "example@gmail.com"),
        Entry(issuer = "Yandex", username = "example2@ya.ru"),
        Entry(issuer = "Github", username = "example@gmail.com"),
        Entry(issuer = "Google", username = "example@gmail.com"),
        Entry(issuer = "Yandex", username = "example2@ya.ru"),
        Entry(issuer = "Github", username = "example@gmail.com"),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.statusBars.only(
                                WindowInsetsSides.Top +
                                        WindowInsetsSides.Horizontal
                            )
                        )
                        .height(64.dp)
                        .padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Convenient Authenticator",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    IconButton(onClick = onAdd) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_entry),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(onClick = onMenu) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    ) { insets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(entries, key = { it.uuid }) { entry ->
                TotpBlock(entry, shareEntry = {})
            }
        }
    }
}

@Preview(name = "Authenticator - Light", showBackground = true)
@Preview(
    name = "Authenticator - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun AuthenticatorScreenPreview() {
    ConvenientAuthenticatorTheme(dynamicColor = false) {
        AuthenticatorScreen()
    }
}
