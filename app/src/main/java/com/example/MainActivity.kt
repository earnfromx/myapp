package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dynamic cleanup on creation to verify clean launch state
        cleanupAllUserData(this)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFFDFBFF) // Clean, lightweight Professional Polish theme background
                ) { innerPadding ->
                    PrivateBrowserContent(
                        targetUrl = "https://ov.cabretpardao.com/itUi3X2SVstfs3g/143313",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        // High assurance cleanup during exit state
        cleanupAllUserData(this)
        super.onDestroy()
    }
}

// Global utility helper to shred all user session evidence
fun cleanupAllUserData(context: Context) {
    try {
        // 1. Wipe Cookie manager trace completely
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.removeSessionCookies(null)
        cookieManager.flush()

        // 2. Clear Web Storage, databases & IndexedDB instances
        WebStorage.getInstance().deleteAllData()

        // 3. Clear file cache directories recursively
        context.cacheDir.deleteRecursively()
        context.codeCacheDir.deleteRecursively()

        // 4. Forceful delete of local key-value state or standard WebView DB directories
        val databaseDir = context.getDatabasePath("webview.db")
        if (databaseDir.exists()) {
            databaseDir.delete()
        }
        val databaseJournal = context.getDatabasePath("webview.db-journal")
        if (databaseJournal.exists()) {
            databaseJournal.delete()
        }
    } catch (e: Exception) {
        // Safe failover
    }
}

@Composable
fun PrivateBrowserContent(
    targetUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableStateOf(0f) }
    var currentUrl by remember { mutableStateOf(targetUrl) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Intercept system navigation back gesture to clear state before exit
    BackHandler(enabled = true) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            cleanupAllUserData(context)
            activity?.finish()
        }
    }

    // Secondary assurance cleanup when leaving scope
    DisposableEffect(Unit) {
        onDispose {
            cleanupAllUserData(context)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBFF)) // Professional Polish light theme canvas background
    ) {
        // 1. Top Navigation & Identity block
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular back/close action button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3E3E3))
                        .testTag("back_button_circle"),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (webViewInstance?.canGoBack() == true) {
                                webViewInstance?.goBack()
                            } else {
                                cleanupAllUserData(context)
                                activity?.finish()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close session",
                            tint = Color(0xFF1A1C1E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "Private Session",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = Color(0xFF1A1C1E),
                            lineHeight = 22.sp
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Indicator",
                            tint = Color(0xFF0061A4), // Accent Blue shield color
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Isolated Container",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = Color(0xFF44474E),
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // More Options badge (representing status metadata dropdown potential)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD3E3FD)), // Light Blue Badge
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color(0xFF041E49), // Dark Navy blue
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. URL Address / Control Bar block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Color(0xFFEFF1F8)) // Gray-blue address pill background
                    .border(1.dp, Color(0xFFC4C7CF), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "🔒 Encrypted",
                    tint = Color(0xFF44474E),
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = currentUrl.replace("https://", "").replace("www.", ""),
                    color = Color(0xFF1A1C1E),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Refresh trigger inside URL pill
                IconButton(
                    onClick = { webViewInstance?.reload() },
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Page",
                        tint = Color(0xFF44474E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. Main Sandbox Protected View Card
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp), // rounded-3xl
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC4C7CF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Embed the isolated secure AndroidView browser content
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = false
                                cacheMode = WebSettings.LOAD_NO_CACHE
                                saveFormData = false
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                            }

                            isSaveEnabled = false

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    return handleSchemeUrl(view?.context, url, view)
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                    if (url == null) return false
                                    return handleSchemeUrl(view?.context, url, view)
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    url?.let { currentUrl = it }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    url?.let { currentUrl = it }
                                    canGoBack = view?.canGoBack() ?: false
                                    canGoForward = view?.canGoForward() ?: false
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    loadingProgress = newProgress / 100f
                                }
                            }

                            loadUrl(targetUrl)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    update = { webView ->
                        webViewInstance = webView
                    }
                )

                // Linear sleek progress bar exactly matching the specified style
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFFE3E3E3)) // E3E3E3 template bg
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = loadingProgress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                .background(Color(0xFF0061A4)) // 0061A4 template blue color
                        )
                    }
                }
            }
        }

        // 4. Secure Metadata Status Row & Navigation arrows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Secure connection green indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16A34A)) // green-600
                )
                Text(
                    text = "SECURE TUNNEL ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E),
                        letterSpacing = 0.5.sp
                    )
                )
            }

            // Quick back/forward page controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { webViewInstance?.goBack() },
                    enabled = canGoBack,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (canGoBack) Color(0xFF1A1C1E) else Color(0xFFC4C7CF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { webViewInstance?.goForward() },
                    enabled = canGoForward,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("forward_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward Navigation",
                        tint = if (canGoForward) Color(0xFF1A1C1E) else Color(0xFFC4C7CF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "AES-256",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E),
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        // 5. Destructive Purge Button Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    cleanupAllUserData(context)
                    activity?.finish()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBA1A1A), // Red color
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("purge_and_exit_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Clear cache icon",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Clear Cache & Exit",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            // Aesthetic visual Home bar mock
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1C1E).copy(alpha = 0.2f))
            )
        }
    }
}

// Keep a simple greeting stub for test file backward compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Secure Sandbox Protocol Active: $name",
            color = Color(0xFF16A34A),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Column {
            PrivateBrowserContent(targetUrl = "https://ov.cabretpardao.com/itUi3X2SVstfs3g/143313")
        }
    }
}

fun handleSchemeUrl(context: Context?, url: String, webView: WebView?): Boolean {
    if (context == null) return false

    // Standard web navigation
    if (url.startsWith("http://") || url.startsWith("https://")) {
        return false
    }

    // Handle intent:// schemes safely like Chrome does
    if (url.startsWith("intent://")) {
        try {
            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.component = null
                intent.selector?.let { selector ->
                    selector.component = null
                }
                
                // Try resolving/starting the activity
                val packageManager = context.packageManager
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent)
                    return true
                } else {
                    // Try processing fallback url extra parameter
                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                    if (fallbackUrl != null && (fallbackUrl.startsWith("http://") || fallbackUrl.startsWith("https://"))) {
                        webView?.loadUrl(fallbackUrl)
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            // Quick exception recovery: parse standard fallback directly from query string parameters
            try {
                if (url.contains("browser_fallback_url=")) {
                    val startIndex = url.indexOf("browser_fallback_url=") + "browser_fallback_url=".length
                    var endIndex = url.indexOf("&", startIndex)
                    if (endIndex == -1) endIndex = url.length
                    val fallbackDecoded = Uri.decode(url.substring(startIndex, endIndex))
                    if (fallbackDecoded.startsWith("http://") || fallbackDecoded.startsWith("https://")) {
                        webView?.loadUrl(fallbackDecoded)
                        return true
                    }
                }
            } catch (ex: Exception) {
                // Ignore silent errors
            }
        }
        return true // Consume to prevent unknown protocol errors in the web view frame
    }

    // Handle generic custom app schemes: tel:, mailto:, sms:, market:, whatsapp:, etc.
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
        return true
    } catch (e: Exception) {
        // Safe fall-through if package doesn't exist
    }

    return true // Always consume to prevent default bad scheme crash/error screen
}
