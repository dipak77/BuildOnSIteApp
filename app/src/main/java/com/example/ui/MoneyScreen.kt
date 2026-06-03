package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

// ─────────────────────────────────────────────────────────────────────────────
// MoneyScreen
// ─────────────────────────────────────────────────────────────────────────────
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
    val query      = viewModel.transactionSearchQuery
    val typeFilter = viewModel.transactionTypeFilter
    val catFilter  = viewModel.transactionCategoryFilter

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Project-scoped transactions
    val projectTransactions = remember(allTransactions, currentProject) {
        val projId = currentProject?.id
        if (projId == null) emptyList()
        else allTransactions.filter { it.projectId == projId }
    }

    // Summary numbers (unfiltered)
    val totalIn  = remember(projectTransactions) { projectTransactions.filter { it.type == "Money In"  }.sumOf { it.amount } }
    val totalOut = remember(projectTransactions) { projectTransactions.filter { it.type == "Money Out" }.sumOf { it.amount } }
    val balance  = totalIn - totalOut

    // Dynamically filtered list
    val filteredTransactions = remember(projectTransactions, query, typeFilter, catFilter) {
        projectTransactions.filter { tx ->
            val matchesType     = (typeFilter == "All") || (tx.type     == typeFilter)
            val matchesCategory = (catFilter  == "All") || (tx.category == catFilter)
            val matchesQuery    = query.isBlank() ||
                tx.description.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true) ||
                (tx.partyName?.contains(query, ignoreCase = true) == true) ||
                tx.reference.contains(query, ignoreCase = true) ||
                tx.paymentMethod.contains(query, ignoreCase = true)
            matchesType && matchesCategory && matchesQuery
        }
    }

    val categories = listOf("All") + COST_CODES

    // ── Main scrollable content ───────────────────────────────────────────────
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Page header ───────────────────────────────────────────────────────
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
                        text = "Ledger · ${currentProject?.name ?: "No project selected"}",
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

        // ── Enhanced stats grid ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statGreen  = if (dark) NeonGreen  else Color(0xFF047857)
                val statPink   = if (dark) NeonPink   else Color(0xFFBE123C)
                val balColor   = if (balance >= 0) statGreen else statPink

                // Money In
                StatCard(
                    modifier    = Modifier.weight(1f),
                    dark        = dark,
                    label       = "MONEY IN",
                    value       = currencyFormatter.format(totalIn),
                    accentColor = statGreen,
                    borderColor = if (dark) GlassBorderDark else null
                )
                // Money Out
                StatCard(
                    modifier    = Modifier.weight(1f),
                    dark        = dark,
                    label       = "MONEY OUT",
                    value       = currencyFormatter.format(totalOut),
                    accentColor = statPink,
                    borderColor = if (dark) GlassBorderDark else null
                )
                // Net Balance
                StatCard(
                    modifier    = Modifier.weight(1f),
                    dark        = dark,
                    label       = "BALANCE",
                    value       = currencyFormatter.format(balance),
                    accentColor = balColor,
                    borderColor = if (dark) GlassBorderNeonCyan else null,
                    valueColor  = balColor
                )
            }
        }

        // ── Search bar ────────────────────────────────────────────────────────
        item {
            GlassTextField(
                value         = query,
                onValueChange = { viewModel.transactionSearchQuery = it },
                label         = "Search ledger...",
                darkTheme     = dark,
                placeholder   = "Description, party, ref, method…",
                icon          = Icons.Default.Search,
                focusedStroke = NeonCyan
            )
        }

        // ── Type filter tabs ──────────────────────────────────────────────────
        item {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Money In", "Money Out").forEach { tab ->
                    GlassChip(
                        text        = tab,
                        selected    = typeFilter == tab,
                        onClick     = { viewModel.transactionTypeFilter = tab },
                        darkTheme   = dark,
                        activeColor = when (tab) {
                            "Money In"  -> NeonGreen
                            "Money Out" -> NeonPink
                            else        -> NeonCyan
                        }
                    )
                }
            }
        }

        // ── Category chips – HORIZONTAL SCROLL (LazyRow) ─────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text       = "CATEGORY",
                    color      = if (dark) TextSecondary else TextSecondaryLight,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding        = PaddingValues(end = 16.dp)
                ) {
                    items(categories) { cat ->
                        GlassChip(
                            text        = cat,
                            selected    = catFilter == cat,
                            onClick     = { viewModel.transactionCategoryFilter = cat },
                            darkTheme   = dark,
                            activeColor = NeonPurple
                        )
                    }
                }
            }
        }

        // ── Transaction count + sort label ────────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "All Transactions (${filteredTransactions.size})",
                    color      = if (dark) TextPrimary else TextPrimaryLight,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector     = Icons.Default.SwapVert,
                        contentDescription = null,
                        tint            = if (dark) TextSecondary else TextSecondaryLight,
                        modifier        = Modifier.size(14.dp)
                    )
                    Text(
                        text   = "Date (Latest)",
                        color  = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // ── Empty state ───────────────────────────────────────────────────────
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector     = Icons.Default.Inbox,
                            contentDescription = "Empty",
                            tint            = if (dark) TextMuted else TextSecondaryLight,
                            modifier        = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text     = "No matching cash records found",
                            color    = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text     = "Try clearing your filters",
                            color    = if (dark) TextMuted else TextSecondaryLight,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            // ── Transaction cards ─────────────────────────────────────────────
            items(filteredTransactions, key = { it.id }) { tx ->
                EnhancedTransactionCard(
                    tx                = tx,
                    dark              = dark,
                    currencyFormatter = currencyFormatter,
                    onView            = { viewModel.sharedSelectedTxDetails = tx },
                    onDelete          = { showDeleteConfirmForTx = tx }
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Transaction detail modal (unchanged logic, kept same UI)
    // ─────────────────────────────────────────────────────────────────────────
    if (selectedTxForDetails != null) {
        val tx              = selectedTxForDetails!!
        val isMoneyIn       = tx.type == "Money In"
        val tintColor       = if (isMoneyIn) (if (dark) NeonGreen else Color(0xFF047857)) else (if (dark) NeonPink else Color(0xFFBE123C))
        val formattedFull   = formatIndianRupeesWithLakhCr(tx.amount)

        GlassModalDialog(
            visible    = true,
            onDismiss  = { viewModel.sharedSelectedTxDetails = null },
            title      = "Receipt / Transaction Details",
            darkTheme  = dark,
            glowColor  = tintColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Amount badge
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
                        Text(tx.type.uppercase(), color = tintColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text       = if (isMoneyIn) "+$formattedFull" else "-$formattedFull",
                            color      = tintColor,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Detail rows
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    @Composable
                    fun DetailItem(label: String, value: String, isHighlight: Boolean = false) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text       = label,
                                color      = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text       = value.ifBlank { "N/A" },
                                color      = if (isHighlight) tintColor else if (dark) TextPrimary else TextPrimaryLight,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(start = 16.dp),
                                maxLines   = 3,
                                overflow   = TextOverflow.Ellipsis
                            )
                        }
                        HorizontalDivider(color = if (dark) GlassBorderDark else GlassBorderLight)
                    }

                    val currentProject2 by viewModel.activeProject.collectAsState()
                    DetailItem(label = "Date of Payment:",   value = tx.date)
                    DetailItem(label = "Category:",          value = tx.category,              isHighlight = true)
                    DetailItem(label = "Payment Method:",    value = tx.paymentMethod)
                    DetailItem(label = "Reference / Bill No:", value = tx.reference)
                    DetailItem(label = "Mapped Party / Payee:", value = tx.partyName ?: "No mapped party")
                    DetailItem(label = "Project Associated:", value = currentProject2?.name ?: "Main Site")

                    // Full description card
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(
                            text       = "Description / Memo:",
                            color      = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(bottom = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (dark) Color(0x1F293780) else Color(0x12000000))
                                .padding(10.dp)
                        ) {
                            Text(
                                text       = tx.description.ifBlank { "No description details provided for this transaction." },
                                color      = if (dark) TextPrimary else TextPrimaryLight,
                                fontSize   = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                GlassButton(
                    onClick    = { viewModel.sharedSelectedTxDetails = null },
                    darkTheme  = dark,
                    glowColor  = tintColor,
                    modifier   = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE DETAILS", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    if (showDeleteConfirmForTx != null) {
        val tx = showDeleteConfirmForTx!!
        GlassModalDialog(
            visible   = true,
            onDismiss = { showDeleteConfirmForTx = null },
            title     = "⚠ Confirm Deletion",
            darkTheme = dark,
            glowColor = NeonPink
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Are you sure you want to delete this transaction? This action cannot be undone.",
                    color    = if (dark) TextSecondary else TextSecondaryLight,
                    fontSize = 13.sp
                )
                Text(
                    "${tx.type}: ${currencyFormatter.format(tx.amount)}\n${tx.description}",
                    color      = if (dark) TextPrimary else TextPrimaryLight,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassButton(
                        onClick      = { showDeleteConfirmForTx = null },
                        darkTheme    = dark,
                        outlineMode  = true,
                        modifier     = Modifier.weight(1f)
                    ) { Text("Cancel", fontWeight = FontWeight.Bold) }

                    GlassButton(
                        onClick   = {
                            viewModel.deleteTransaction(tx)
                            showDeleteConfirmForTx = null
                        },
                        darkTheme  = dark,
                        glowColor  = NeonPink,
                        modifier   = Modifier.weight(1f)
                    ) { Text("Delete", fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EnhancedTransactionCard
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EnhancedTransactionCard(
    tx: Transaction,
    dark: Boolean,
    currencyFormatter: NumberFormat,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val isIn = tx.type == "Money In"

    // Date parsing
    val dateParts = tx.date.split("-")
    val months    = listOf("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC")
    val dayStr    = dateParts.getOrNull(2) ?: "—"
    val monIndex  = (dateParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
    val monStr    = months.getOrElse(monIndex) { "—" }
    val yearStr   = dateParts.getOrNull(0) ?: "—"

    // Colors
    val accentColor  = if (isIn) (if (dark) NeonGreen else Color(0xFF047857)) else (if (dark) NeonPink else Color(0xFFBE123C))
    val cardBg       = if (dark) Color(0xFF1E293B)  else Color.White
    val cardBorder   = if (dark) Color(0xFF334155)  else Color(0xFFE2E8F0)
    val textPrimary  = if (dark) Color(0xFFF1F5F9)  else Color(0xFF1E293B)
    val textSecondary= if (dark) Color(0xFF94A3B8)  else Color(0xFF64748B)
    val dividerColor = if (dark) Color(0xFF2D3F55)  else Color(0xFFF1F5F9)

    // Payment method styling
    val isBank      = tx.paymentMethod.contains("Bank", ignoreCase = true) ||
                      tx.paymentMethod.contains("Transfer", ignoreCase = true)
    val pillBg      = if (isBank) (if (dark) Color(0x1A1A73E8) else Color(0xFFE8F0FE))
                      else        (if (dark) Color(0x1AB91C1C) else Color(0xFFFDE8E8))
    val pillTextCol = if (isBank) (if (dark) Color(0xFF90CDF4) else Color(0xFF1A73E8))
                      else        (if (dark) Color(0xFFF87171) else Color(0xFFC5221F))
    val pillIcon    = if (isBank) Icons.Default.AccountBalance else Icons.Default.Payments

    var showMenu by remember { mutableStateOf(false) }

    // Card container — uses IntrinsicSize.Min so the accent stripe stretches full height
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {

            // ── Left accent stripe (color-coded, 4 dp) ────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            // ── Card content ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onView)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // Row 1 – compact date (left) + amount & type badge (right)
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    // Date inline
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(accentColor, CircleShape)
                        )
                        Text(
                            text       = "$dayStr $monStr $yearStr",
                            color      = textSecondary,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Amount + type badge
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text       = "${if (isIn) "+" else "-"}${currencyFormatter.format(tx.amount)}",
                            color      = accentColor,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        // Type badge (arrow + label)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector     = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint            = accentColor,
                                modifier        = Modifier.size(9.dp)
                            )
                            Text(
                                text       = if (isIn) "Money In" else "Money Out",
                                color      = accentColor,
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(9.dp))

                // Row 2 – description (bold title)
                Text(
                    text       = tx.description.ifBlank { "No description provided" },
                    color      = textPrimary,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Row 3 – category chip + party badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Category chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (dark) Color(0x1A3B82F6) else Color(0xFFEFF6FF))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text       = tx.category,
                            color      = if (dark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Party badge (conditional)
                    if (!tx.partyName.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(accentColor.copy(alpha = 0.10f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text       = tx.partyName,
                                color      = accentColor,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Divider
                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                Spacer(modifier = Modifier.height(10.dp))

                // Row 4 – payment pill + ref text (left) | View + More actions (right)
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Payment + ref
                    Row(
                        modifier              = Modifier.weight(1f),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(pillBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(pillIcon, null, tint = pillTextCol, modifier = Modifier.size(11.dp))
                            Text(tx.paymentMethod, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = pillTextCol)
                        }
                        if (tx.reference.isNotEmpty()) {
                            Text(
                                text     = "# ${tx.reference}",
                                color    = textSecondary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // View pill button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (dark) Color(0x1A3B82F6) else Color(0xFFEFF6FF))
                                .border(1.dp, if (dark) Color(0x333B82F6) else Color(0xFFBFDBFE), RoundedCornerShape(7.dp))
                                .clickable(onClick = onView)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector     = Icons.Default.RemoveRedEye,
                                    contentDescription = "View",
                                    tint            = if (dark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                    modifier        = Modifier.size(12.dp)
                                )
                                Text(
                                    text       = "View",
                                    fontSize   = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = if (dark) Color(0xFF93C5FD) else Color(0xFF2563EB)
                                )
                            }
                        }

                        // More (⋮) icon button with dropdown
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (dark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                    .border(1.dp, cardBorder, RoundedCornerShape(7.dp))
                                    .clickable { showMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector     = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint            = textSecondary,
                                    modifier        = Modifier.size(13.dp)
                                )
                            }

                            DropdownMenu(
                                expanded        = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier        = Modifier.background(if (dark) Color(0xFF1E293B) else Color.White)
                            ) {
                                // View details option
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.RemoveRedEye, null,
                                                tint     = if (dark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                "View Details",
                                                color = if (dark) TextPrimary else TextPrimaryLight
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onView()
                                    }
                                )
                                HorizontalDivider(color = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                // Delete option
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline, null,
                                                tint     = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text("Delete Record", color = Color(0xFFEF4444))
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
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

// ─────────────────────────────────────────────────────────────────────────────
// StatCard — compact summary tile used in the 3-column grid
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatCard(
    modifier: Modifier     = Modifier,
    dark: Boolean,
    label: String,
    value: String,
    accentColor: Color,
    borderColor: Color?    = null,
    valueColor: Color?     = null
) {
    GlassCard(
        modifier    = modifier,
        darkTheme   = dark,
        padding     = 10.dp,
        borderColor = borderColor
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(accentColor, CircleShape)
                )
                Text(
                    text          = label,
                    color         = accentColor,
                    fontSize      = 8.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text       = value,
                color      = valueColor ?: (if (dark) TextPrimary else TextPrimaryLight),
                fontSize   = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
    }
}