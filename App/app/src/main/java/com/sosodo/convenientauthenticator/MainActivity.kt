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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sosodo.convenientauthenticator.ui.theme.ConvenientAuthenticatorTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private val AccountGroups = listOf("All", "Personal", "Work", "Other")

private val ExampleEntries = listOf(
    Entry(issuer = "Google", username = "example@gmail.com", group = "Personal"),
    Entry(issuer = "Yandex", username = "example2@ya.ru", group = "Other"),
    Entry(issuer = "Github", username = "example@gmail.com", group = "Work"),
    Entry(issuer = "Google", username = "example@gmail.com", group = "Personal"),
)

private val EntriesSaver = listSaver<List<Entry>, String>(
    save = { entries ->
        entries.flatMap { entry ->
            listOf(entry.uuid.toString(), entry.group, generateOtpAuthLink(entry))
        }
    },
    restore = { saved ->
        saved.chunked(3).map { (uuid, group, link) ->
            requireNotNull(parseOtpAuthLink(link)).apply {
                this.uuid = java.util.UUID.fromString(uuid)
                this.group = group
            }
        }
    },
)

enum class AuthenticatorScreenState {
    Loading,
    Content,
    Empty,
    Error,
}

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
                AuthenticatorScreenContainer()
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
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.issuer.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displaySmall,
                )
            }

            Spacer(Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = entry.issuer,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                    )
                    Text(
                        text = entry.username,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = entry.getCode().toString()
                        .padStart(entry.digits, '0')
                        .chunked(3)
                        .joinToString(" "),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayLarge,
                    maxLines = 1,
                    softWrap = false,
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
fun AuthenticatorScreenContainer() {
    var state by rememberSaveable {
        mutableStateOf(AuthenticatorScreenState.entries.random())
    }
    var selectedGroup by rememberSaveable { mutableStateOf("All") }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var entries by rememberSaveable(stateSaver = EntriesSaver) {
        mutableStateOf(ExampleEntries)
    }

    LaunchedEffect(state) {
        if (state == AuthenticatorScreenState.Loading) {
            delay(1.5.seconds)
            state = listOf(
                AuthenticatorScreenState.Content,
                AuthenticatorScreenState.Empty,
                AuthenticatorScreenState.Error,
            ).random()
        }
    }

    AuthenticatorScreen(
        state = state,
        entries = entries,
        selectedGroup = selectedGroup,
        onGroupSelected = { selectedGroup = it },
        onAdd = { showAddDialog = true },
        onRetry = { state = AuthenticatorScreenState.Loading },
    )

    if (showAddDialog) {
        AddEntryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { entry ->
                entry.group = if (selectedGroup == "All") "Other" else selectedGroup
                entries = listOf(entry) + entries
                state = AuthenticatorScreenState.Content
                showAddDialog = false
            },
        )
    }
}

@Composable
fun AuthenticatorScreen(
    state: AuthenticatorScreenState = AuthenticatorScreenState.Content,
    entries: List<Entry> = ExampleEntries,
    selectedGroup: String = "All",
    onGroupSelected: (String) -> Unit = {},
    onAdd: () -> Unit = {},
    onMenu: () -> Unit = {},
    onRetry: () -> Unit = {},
) {

    val filteredEntries = entries.filter { entry ->
        selectedGroup == "All" || entry.group == selectedGroup
    }
    val displayState = if (
        state == AuthenticatorScreenState.Content && filteredEntries.isEmpty()
    ) AuthenticatorScreenState.Empty else state

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        )
                    ),
            ) {
                AuthenticatorAppBar(onAdd = onAdd, onMenu = onMenu)

            }
        },
        bottomBar = {
            GroupSelector(
                selectedGroup = selectedGroup,
                onGroupSelected = onGroupSelected,
            )
        }
    ) { insets ->
        if (displayState != AuthenticatorScreenState.Content) {
            AuthenticatorStatusContent(
                state = displayState,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(insets)
                    .consumeWindowInsets(insets)
                    .padding(24.dp),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(filteredEntries, key = { it.uuid }) { entry ->
                TotpBlock(entry, shareEntry = {})
            }
        }
    }
}

@Composable
private fun AuthenticatorAppBar(
    onAdd: () -> Unit,
    onMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        IconButton(onClick = onAdd, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_entry),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GroupSelector(
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(4.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AccountGroups.forEach { group ->
                val selected = group == selectedGroup
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer
                        )
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onGroupSelected(group) },
                        )
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = group,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatorStatusContent(
    state: AuthenticatorScreenState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        when (state) {
            AuthenticatorScreenState.Loading -> {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.loading_entries),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }

            AuthenticatorScreenState.Empty -> {
                Text(
                    text = stringResource(R.string.no_entries),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(R.string.refresh))
                }
            }

            AuthenticatorScreenState.Error -> {
                Text(
                    text = stringResource(R.string.load_entries_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }

            AuthenticatorScreenState.Content -> Unit
        }
    }
}

@Preview(name = "Authenticator - Light", showBackground = true)
@Preview(
    name = "Authenticator - 150% text",
    showBackground = true,
    widthDp = 360,
    fontScale = 1.5f,
)
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

@Preview(name = "Authenticator - Work", showBackground = true)
@Composable
fun AuthenticatorWorkPreview() {
    ConvenientAuthenticatorTheme(dynamicColor = false) {
        AuthenticatorScreen(selectedGroup = "Work")
    }
}

@Preview(name = "Authenticator - Loading", showBackground = true)
@Composable
fun AuthenticatorLoadingPreview() {
    ConvenientAuthenticatorTheme(dynamicColor = false) {
        AuthenticatorScreen(state = AuthenticatorScreenState.Loading)
    }
}

@Preview(name = "Authenticator - Empty", showBackground = true)
@Composable
fun AuthenticatorEmptyPreview() {
    ConvenientAuthenticatorTheme(dynamicColor = false) {
        AuthenticatorScreen(state = AuthenticatorScreenState.Empty)
    }
}

@Preview(name = "Authenticator - Error", showBackground = true)
@Composable
fun AuthenticatorErrorPreview() {
    ConvenientAuthenticatorTheme(dynamicColor = false) {
        AuthenticatorScreen(state = AuthenticatorScreenState.Error)
    }
}
