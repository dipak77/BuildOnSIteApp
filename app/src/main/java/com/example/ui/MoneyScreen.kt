package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun MoneyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dark = viewModel.darkThemeEnabled
    val currentProject by viewModel.activeProject.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()

    val selectedTxForDetails = viewModel.sharedSelectedTxDetails
    var showDeleteConfirmForTx by remember { mutableStateOf<Transaction?>(null) }

    // Filters & search state
    val query = viewModel.transactionSearchQuery
    val typeFilter = viewModel.transactionTypeFilter
    val catFilter = viewModel.transactionCategoryFilter

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Aggregate lists filtered for current selected project
    val projectTransactions = remember(allTransactions, currentProject) {
        val projId = currentProject?.id
        if (projId == null) emptyList()
        else allTransactions.filter { it.projectId == projId }
    }

    // Calculations of unfiltered numbers
    val totalIn = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money In" }.sumOf { it.amount }
    }
    val totalOut = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money Out" }.sumOf { it.amount }
    }
    val balance = totalIn - totalOut

    // Filter results dynamically!
    val filteredTransactions = remember(projectTransactions, query, typeFilter, catFilter) {
        projectTransactions.filter { tx ->
            val matchesType = (typeFilter == "All") || (tx.type == typeFilter)
            val matchesCategory = (catFilter == "All") || (tx.category == catFilter)
            val matchesQuery = (query.isBlank()) || 
                    tx.description.contains(query, ignoreCase = true) || 
                    tx.category.contains(query, ignoreCase = true) ||
                    (tx.partyName?.contains(query, ignoreCase = true) == true) ||
                    tx.reference.contains(query, ignoreCase = true) ||
                    tx.paymentMethod.contains(query, ignoreCase = true)
            matchesType && matchesCategory && matchesQuery
        }
    }

    // List of categories for category filter chips
    val categories = listOf("All") + COST_CODES

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Page title & CSV Export
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cash Flows",
                        color = if (dark) NeonCyan else Color(0xFF0284C7),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Transactions ledger for ${currentProject?.name ?: "None"}",
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val context = LocalContext.current
                IconButton(
                    onClick = { viewModel.exportTransactionsCSV(context) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Export CSV",
                        tint = if (dark) NeonCyan else Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Stats grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cash-In Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp,
                    borderColor = if (dark) GlassBorderDark else null
                ) {
                    Text("MONEY IN", color = if (dark) NeonGreen else Color(0xFF047857), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        currencyFormatter.format(totalIn),
                        color = if (dark) TextPrimary else TextPrimaryLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Cash-Out Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp,
                    borderColor = if (dark) GlassBorderDark else null
                ) {
                    Text("MONEY OUT", color = if (dark) NeonPink else Color(0xFFBE123C), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        currencyFormatter.format(totalOut),
                        color = if (dark) TextPrimary else TextPrimaryLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Net balance Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp,
                    borderColor = if (dark) GlassBorderNeonCyan else null
                ) {
                    Text("NET BALANCE", color = if (dark) NeonCyan else Color(0xFF0284C7), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        currencyFormatter.format(balance),
                        color = if (balance >= 0) (if (dark) NeonGreen else Color(0xFF047857)) else (if (dark) NeonPink else Color(0xFFBE123C)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Search Bar Block
        item {
            GlassTextField(
                value = query,
                onValueChange = { viewModel.transactionSearchQuery = it },
                label = "Search ledger...",
                darkTheme = dark,
                placeholder = "Type description or cement/steel...",
                icon = Icons.Default.Search,
                focusedStroke = NeonCyan
            )
        }

        // Filter Type Tabs: All, Money In, Money Out
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Money In", "Money Out").forEach { tab ->
                    GlassChip(
                        text = tab,
                        selected = typeFilter == tab,
                        onClick = { viewModel.transactionTypeFilter = tab },
                        darkTheme = dark,
                        activeColor = if (tab == "Money In") NeonGreen else if (tab == "Money Out") NeonPink else NeonCyan
                    )
                }
            }
        }

        // Horizontal scrolling category chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Simple wraps or columns
                Column {
                    Text(
                        text = "Categories",
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            GlassChip(
                                text = cat,
                                selected = catFilter == cat,
                                onClick = { viewModel.transactionCategoryFilter = cat },
                                darkTheme = dark,
                                activeColor = NeonPurple
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.drop(3).forEach { cat ->
                            GlassChip(
                                text = cat,
                                selected = catFilter == cat,
                                onClick = { viewModel.transactionCategoryFilter = cat },
                                darkTheme = dark,
                                activeColor = NeonPurple
                            )
                        }
                    }
                }
            }
        }

        // Transaction list or Empty view
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty",
                            tint = if (dark) TextMuted else TextSecondaryLight,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching cash records found",
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                val isIn = tx.type == "Money In"
                val dateParts = tx.date.split("-")
                val months = listOf("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC")
                val dayStr = dateParts.getOrNull(2) ?: "27"
                val monIndex = (dateParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                val monStr = months.getOrElse(monIndex) { "MAY" }
                val yearStr = dateParts.getOrNull(0) ?: "2024"

                val themeGreen = if (dark) NeonGreen else Color(0xFF047857)
                val themePink = if (dark) NeonPink else Color(0xFFBE123C)
                val accentColor = if (isIn) themeGreen else themePink

                val cardBg = if (dark) Color(0xFF1E293B) else Color.White
                val cardBorder = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)
                val textPrimary = if (dark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                val textSecondary = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)

                val cardAccentBorder = accentColor.copy(alpha = 0.35f)

                var showMenu by remember { mutableStateOf(false) }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = dark,
                    borderColor = cardAccentBorder,
                    padding = 12.dp,
                    onClick = { viewModel.sharedSelectedTxDetails = tx }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Left Date Badge (green/red themed)
                        val dateBadgeBg = if (isIn) {
                            if (dark) Color(0x2810B981) else Color(0xFFE6F4EA)
                        } else {
                            if (dark) Color(0x28EF4444) else Color(0xFFFCE8E6)
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 54.dp, height = 66.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(dateBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayStr,
                                    fontSize = 18.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = monStr,
                                    fontSize = 10.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = yearStr,
                                    fontSize = 9.sp,
                                    color = accentColor.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 2. Arrow Indicator circle
                        val arrowBg = if (isIn) {
                            if (dark) Color(0x1F10B981) else Color(0xFFE6F4EA)
                        } else {
                            if (dark) Color(0x1FEF4444) else Color(0xFFFCE8E6)
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(36.dp)
                                .background(arrowBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 3. Middle Content Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = tx.description.ifBlank { "No description provided" },
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!tx.partyName.isNullOrEmpty()) {
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val partyBadgeBg = if (isIn) {
                                        if (dark) Color(0x2210B981) else Color(0xFFE6F4EA)
                                    } else {
                                        if (dark) Color(0x22EF4444) else Color(0xFFFCE8E6)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(partyBadgeBg)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = tx.partyName,
                                            color = accentColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${tx.category}  •  ${tx.date}",
                                color = textSecondary,
                                fontSize = 11.sp
                            )

                            val refStr = if (tx.reference.isNotEmpty()) "  •  Ref: ${tx.reference}" else ""
                            Text(
                                text = "${tx.paymentMethod}$refStr",
                                color = textSecondary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            val isBank = tx.paymentMethod.contains("Bank", ignoreCase = true) || tx.paymentMethod.contains("Transfer", ignoreCase = true)
                            val pillBg = if (isBank) {
                                if (dark) Color(0x1A1A73E8) else Color(0xFFE8F0FE)
                            } else {
                                if (dark) Color(0x1AB91C1C) else Color(0xFFFDE8E8)
                            }
                            val pillTextCol = if (isBank) {
                                if (dark) Color(0xFF90CDF4) else Color(0xFF1A73E8)
                            } else {
                                if (dark) Color(0xFFF87171) else Color(0xFFC5221F)
                            }
                            val pillIcon = if (isBank) Icons.Default.AccountBalance else Icons.Default.Payments
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(pillBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = pillIcon,
                                    contentDescription = null,
                                    tint = pillTextCol,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = tx.paymentMethod,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = pillTextCol
                                )
                            }
                        }

                        // 4. Right Content Column
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.align(Alignment.Top)
                        ) {
                            Text(
                                text = "${if (isIn) "+" else "-"}${currencyFormatter.format(tx.amount)}",
                                color = accentColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { viewModel.sharedSelectedTxDetails = tx }
                                        .padding(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RemoveRedEye,
                                        contentDescription = "View",
                                        tint = textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "View",
                                        fontSize = 8.sp,
                                        color = textSecondary
                                    )
                                }

                                Box {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { showMenu = true }
                                            .padding(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More",
                                            tint = textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "More",
                                            fontSize = 8.sp,
                                            color = textSecondary
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        modifier = Modifier.background(if (dark) Color(0xFF1E293B) else Color.White)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(Icons.Default.DeleteOutline, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                    Text("Delete Record", color = Color.Red)
                                                }
                                            },
                                            onClick = {
                                                showMenu = false
                                                showDeleteConfirmForTx = tx
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedTxForDetails != null) {
        val tx = selectedTxForDetails!!
        val isMoneyIn = tx.type == "Money In"
        val tintColor = if (isMoneyIn) (if (dark) NeonGreen else Color(0xFF047857)) else (if (dark) NeonPink else Color(0xFFBE123C))
        val formattedFullAmount = formatIndianRupeesWithLakhCr(tx.amount)

        GlassModalDialog(
            visible = true,
            onDismiss = { viewModel.sharedSelectedTxDetails = null },
            title = "Receipt / Transaction Details",
            darkTheme = dark,
            glowColor = tintColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Amount Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(tintColor.copy(alpha = 0.12f))
                        .border(BorderStroke(1.dp, tintColor.copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                        .padding(vertical = 16.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = tx.type.uppercase(),
                            color = tintColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isMoneyIn) "+$formattedFullAmount" else "-$formattedFullAmount",
                            color = tintColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Grid of Details
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Item Detail Row Helper
                    @Composable
                    fun DetailItem(label: String, value: String, isValueHighlight: Boolean = false) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = value.ifBlank { "N/A" },
                                color = if (isValueHighlight) tintColor else if (dark) TextPrimary else TextPrimaryLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        HorizontalDivider(color = if (dark) GlassBorderDark else GlassBorderLight)
                    }

                    DetailItem(label = "Date of Payment:", value = tx.date)
                    DetailItem(label = "Category:", value = tx.category, isValueHighlight = true)
                    DetailItem(label = "Payment Method:", value = tx.paymentMethod)
                    DetailItem(label = "Reference / Bill No:", value = tx.reference)
                    DetailItem(label = "Mapped Party / Payee:", value = tx.partyName ?: "No mapped party")
                    DetailItem(label = "Project Associated:", value = currentProject?.name ?: "Main Site")
                    
                    // Full Description Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Description / Memo:",
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (dark) Color(0x1F293780) else Color(0x12000000))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = tx.description.ifBlank { "No description details provided for this transaction." },
                                color = if (dark) TextPrimary else TextPrimaryLight,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    GlassButton(
                        onClick = { viewModel.sharedSelectedTxDetails = null },
                        darkTheme = dark,
                        glowColor = tintColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE DETAILS", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }

    if (showDeleteConfirmForTx != null) {
        val tx = showDeleteConfirmForTx!!
        GlassModalDialog(
            visible = true,
            onDismiss = { showDeleteConfirmForTx = null },
            title = "⚠ Confirm Deletion",
            darkTheme = dark,
            glowColor = NeonPink
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Are you sure you want to delete this transaction record? This action cannot be undone.",
                    color = if (dark) TextSecondary else TextSecondaryLight,
                    fontSize = 13.sp
                )
                Text(
                    "${tx.type}: ${currencyFormatter.format(tx.amount)}\n${tx.description}",
                    color = if (dark) TextPrimary else TextPrimaryLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassButton(
                        onClick = { showDeleteConfirmForTx = null },
                        darkTheme = dark,
                        outlineMode = true,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    GlassButton(
                        onClick = {
                            viewModel.deleteTransaction(tx)
                            showDeleteConfirmForTx = null
                        },
                        darkTheme = dark,
                        glowColor = NeonPink,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
