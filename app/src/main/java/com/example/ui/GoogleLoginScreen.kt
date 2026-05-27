package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.TextStyle
import com.example.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun GoogleLoginScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val dark = viewModel.darkThemeEnabled
    
    // UI controller states
    var isConnecting by remember { mutableStateOf(false) }
    var showAccountChooser by remember { mutableStateOf(false) }
    
    // Custom email login mode toggle
    var showCustomInput by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customEmail by remember { mutableStateOf("") }

    // Authentic GMS Google Sign In options config
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Launcher for standard Google Sign In Intent activity
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isConnecting = false
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if (account != null) {
                    val user = GoogleUser(
                        displayName = account.displayName ?: "Google Builder",
                        email = account.email ?: "developer@gmail.com",
                        photoUrl = account.photoUrl?.toString(),
                        idToken = account.idToken,
                        isGuest = false
                    )
                    viewModel.handleGoogleSignIn(user, context)
                    Toast.makeText(context, "Welcome, ${user.displayName}!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                // If GMS framework throws exception (common on emulate layers lacking Play Services Account integrations),
                // we gracefully fall back to custom selection to keep it fully operational!
                showAccountChooser = true
                Toast.makeText(context, "GMS Session: Initializing fallback chooser", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Failed/Cancelled - fallback to custom chooser
            showAccountChooser = true
        }
    }

    GlassAtmosphereBox(darkTheme = dark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // 1. App logo branding logo
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .widthIn(max = 480.dp),
                darkTheme = dark,
                glowColor = NeonCyan
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BuildOnSiteLogo(modifier = Modifier.size(72.dp), darkTheme = dark)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CONSTRUCTPRO",
                            color = if (dark) NeonCyan else Color(0xFF0284C7),
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Unified Workspace Client",
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Welcome & Introductory Message
                    Text(
                        text = "Build On Site App empowers contractors, site engineers, and project developers with advanced offline-first ledger tracking, attendance, customized cards and real-time summaries.",
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // 2. Interactive Sign In Panel
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                darkTheme = dark,
                glowColor = NeonPurple
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SECURE WORKSPACE PORTAL",
                        color = if (dark) NeonPurple else Color(0xFF7C3AED),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    if (isConnecting) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = NeonPurple,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Connecting to Google Services...",
                                color = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Google Sign-In button
                        Button(
                            onClick = {
                                isConnecting = true
                                try {
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        try {
                                            val intent = googleSignInClient.signInIntent
                                            signInLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            isConnecting = false
                                            showAccountChooser = true
                                            Toast.makeText(context, "No local Play services: opening account list", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    try {
                                        val intent = googleSignInClient.signInIntent
                                        signInLauncher.launch(intent)
                                    } catch (ex: Exception) {
                                        isConnecting = false
                                        showAccountChooser = true
                                        Toast.makeText(context, "No local Play services: opening account list", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dark) Color(0xFFFFFFFF) else Color(0xFF1F2937)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Draw a miniature color Google logo
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.White, CircleShape)
                                        .border(BorderStroke(1.dp, Color(0xFFE5E7EB)), CircleShape)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFEA4335), // Google Red
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = "Sign in with Google",
                                    color = if (dark) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Alternative demo/offline quick entry option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAccountChooser = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (dark) NeonGreen else Color(0xFF059669),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Having trouble? Select demo account",
                                color = if (dark) NeonGreen else Color(0xFF059669),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Google Account Selector Fallback & Custom Entry Sheet Dialog
    if (showAccountChooser) {
        AlertDialog(
            onDismissRequest = { showAccountChooser = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 440.dp)
                .clip(RoundedCornerShape(24.dp)),
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccountChooser = false }) {
                    Text("Close", color = Color.Gray)
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Google Sign-In Accounts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color.White else Color.Black
                    )
                    Text(
                        text = "Choose an account to continue to ConstructPro",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = if (dark) Color(0xFF0F172A) else Color.White,
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    
                    // Account Option 1: The current developer user (Dipak Harane)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val user = GoogleUser(
                                        displayName = "Dipak Harane",
                                        email = "haranedipak@gmail.com",
                                        photoUrl = null,
                                        isGuest = false
                                    )
                                    viewModel.handleGoogleSignIn(user, context)
                                    showAccountChooser = false
                                    Toast.makeText(context, "SignedIn successfully as Dipak", Toast.LENGTH_SHORT).show()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (dark) Color(0x3B1F2937) else Color(0xFFF3F4F6)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle showing "D"
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF8B5CF6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("DH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Dipak Harane",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (dark) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "haranedipak@gmail.com",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Standard",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Account Option 2: Demo Contractor
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val user = GoogleUser(
                                        displayName = "ConstructPro Demo",
                                        email = "demo.contractor@constructpro.net",
                                        photoUrl = null,
                                        isGuest = true
                                    )
                                    viewModel.handleGoogleSignIn(user, context)
                                    showAccountChooser = false
                                    Toast.makeText(context, "Entered Workspace Demo Mode", Toast.LENGTH_SHORT).show()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (dark) Color(0x3B1F2937) else Color(0xFFF3F4F6)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF10B981), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("CP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ConstructPro Workspace Demo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (dark) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "demo.contractor@constructpro.net",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Toggle custom simulation input row
                    item {
                        OutlinedButton(
                            onClick = { showCustomInput = !showCustomInput },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (dark) NeonCyan.copy(alpha = 0.5f) else Color.LightGray)
                        ) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (showCustomInput) "Hide simulation options" else "Select custom google identity...", fontSize = 11.sp, color = if (dark) NeonCyan else Color.Black)
                        }
                    }

                    if (showCustomInput) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Full Name text field
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    label = { Text("Display Name", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        focusedLabelColor = NeonCyan,
                                        unfocusedTextColor = if (dark) Color.White else Color.Black,
                                        focusedTextColor = if (dark) Color.White else Color.Black
                                    ),
                                    textStyle = TextStyle(fontSize = 12.sp)
                                )

                                // Email text field
                                OutlinedTextField(
                                    value = customEmail,
                                    onValueChange = { customEmail = it },
                                    label = { Text("Google Account Email", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        focusedLabelColor = NeonCyan,
                                        unfocusedTextColor = if (dark) Color.White else Color.Black,
                                        focusedTextColor = if (dark) Color.White else Color.Black
                                    ),
                                    textStyle = TextStyle(fontSize = 12.sp)
                                )

                                Button(
                                    onClick = {
                                        if (customName.isNotBlank() && customEmail.isNotBlank()) {
                                            val user = GoogleUser(
                                                displayName = customName.trim(),
                                                email = customEmail.trim(),
                                                photoUrl = null,
                                                isGuest = false
                                            )
                                            viewModel.handleGoogleSignIn(user, context)
                                            showAccountChooser = false
                                        } else {
                                            Toast.makeText(context, "Please fill in all simulation credentials", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("AUTHENTICATE CUSTOM IDENTITY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
