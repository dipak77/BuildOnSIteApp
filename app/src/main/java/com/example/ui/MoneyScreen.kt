package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.text.SimpleDateFormat
import java.util.*

private data class SummaryUiModel(
    val label: String,
    val value: String,
    val accent: Color,
    val icon: ImageVector
)

// ─────────────────────────────────────────────────────────────────────────────
// MoneyScreen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MoneyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    val dark = viewModel.darkThemeEnabled
    val currentProject by viewModel.activeProject.collectAsState()
    val allProjects by viewModel.projects.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()

    val selectedTxForDetails = viewModel.sharedSelectedTxDetails
    var showDeleteConfirmForTx by remember { mutableStateOf<Transaction?>(null) }

    val query = viewModel.transactionSearchQuery
    val typeFilter = viewModel.transactionTypeFilter
    val catFilter = viewModel.transactionCategoryFilter

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Local sorting state
    var sortOrder by remember { mutableStateOf("Latest First") }

    val projectTransactions = remember(allTransactions, currentProject) {
        val projId = currentProject?.id ?: return@remember emptyList()
        allTransactions.filter { it.projectId == projId }
    }

    val sortedTransactions = remember(projectTransactions, sortOrder) {
        if (sortOrder == "Latest First") {
            projectTransactions.sortedByDescending { it.date }
        } else {
            projectTransactions.sortedBy { it.date }
        }
    }

    val totalIn = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money In" }.sumOf { it.amount }
    }
    val totalOut = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money Out" }.sumOf { it.amount }
    }
    val balance = totalIn - totalOut

    val filteredTransactions = remember(sortedTransactions, query, typeFilter, catFilter) {
        sortedTransactions.filter { tx ->
            val matchesType = typeFilter == "All" || tx.type == typeFilter
            val matchesCategory = catFilter == "All" || tx.category == catFilter
            val matchesQuery = query.isBlank() ||
                tx.description.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true) ||
                (tx.partyName?.contains(query, ignoreCase = true) == true) ||
                tx.reference.contains(query, ignoreCase = true) ||
                tx.paymentMethod.contains(query, ignoreCase = true)

            matchesType && matchesCategory && matchesQuery
        }
    }

    val groupedByDate = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.date }
    }
    val sortedDates = remember(groupedByDate, sortOrder) {
        if (sortOrder == "Latest First") {
            groupedByDate.keys.sortedDescending()
        } else {
            groupedByDate.keys.sorted()
        }
    }

    val categories = remember { listOf("All") + COST_CODES }

    val summaryCards = remember(totalIn, totalOut, balance, filteredTransactions.size, dark) {
        listOf(
            SummaryUiModel(
                label = "MONEY IN",
                value = currencyFormatter.format(totalIn),
                accent = if (dark) NeonGreen else Color(0xFF047857),
                icon = Icons.Default.ArrowUpward
            ),
            SummaryUiModel(
                label = "MONEY OUT",
                value = currencyFormatter.format(totalOut),
                accent = if (dark) NeonPink else Color(0xFFBE123C),
                icon = Icons.Default.ArrowDownward
            ),
            SummaryUiModel(
                label = "BALANCE",
                value = currencyFormatter.format(balance),
                accent = if (balance >= 0) {
                    if (dark) NeonGreen else Color(0xFF047857)
                } else {
                    if (dark) NeonPink else Color(0xFFBE123C)
                },
                icon = Icons.Default.AccountBalanceWallet
            ),
            SummaryUiModel(
                label = "TRANSACTIONS",
                value = filteredTransactions.size.toString(),
                accent = if (dark) NeonCyan else Color(0xFF0284C7),
                icon = Icons.Default.List
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Top App Bar matching reference mockup
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = if (dark) Color.White else Color.Black,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onMenuClick() }
                    )
                    var expanded by remember { mutableStateOf(false) }
                    Column {
                        Text(
                            text = "Money",
                            color = if (dark) Color.White else Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentProject != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable { expanded = true }
                            ) {
                                Text(
                                    text = currentProject!!.name,
                                    color = if (dark) TextSecondary else TextSecondaryLight,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Project",
                                    tint = if (dark) TextSecondary else TextSecondaryLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(if (dark) Color(0xFF1E293B) else Color.White)
                            ) {
                                allProjects.forEach { proj ->
                                    val isSelected = proj.id == currentProject?.id
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = proj.name,
                                                color = if (isSelected) {
                                                    if (dark) NeonGreen else Color(0xFF10B981)
                                                } else {
                                                    if (dark) TextPrimary else TextPrimaryLight
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            viewModel.selectedProjectId = proj.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val context = LocalContext.current
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Download CSV",
                        tint = if (dark) Color.White else Color.Black,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { viewModel.exportTransactionsCSV(context) }
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (dark) Color.White else Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (dark) Color.White else Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (dark) NeonGreen else Color(0xFF10B981))
                            .clickable {
                                viewModel.transactionTypePreset = "Money In"
                                viewModel.showTransactionDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Transactions / Summary / Budget Tabs matching reference mockup
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                listOf("TRANSACTIONS", "SUMMARY", "BUDGET").forEachIndexed { index, tab ->
                    val selected = index == 0 // TRANSACTIONS is active
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { /* Tab switch logic placeholder */ }
                    ) {
                        Text(
                            text = tab,
                            color = if (selected) {
                                if (dark) NeonGreen else Color(0xFF10B981)
                            } else {
                                if (dark) TextSecondary else TextSecondaryLight
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(2.5.dp)
                                    .background(if (dark) NeonGreen else Color(0xFF10B981))
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Summary Cards Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(summaryCards, key = { it.label }) { stat ->
                    SummaryCard(
                        modifier = Modifier.width(150.dp),
                        dark = dark,
                        label = stat.label,
                        value = stat.value,
                        accentColor = stat.accent,
                        icon = stat.icon
                    )
                }
            }
        }

        // Search Bar Block
        item {
            GlassTextField(
                value = query,
                onValueChange = { viewModel.transactionSearchQuery = it },
                label = "Search transactions...",
                darkTheme = dark,
                placeholder = "Description, party, ref, method…",
                icon = Icons.Default.Search,
                focusedStroke = NeonCyan
            )
        }

        // 3 Dropdown Filters in a Row matching reference mockup
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dropdown 1: All Types
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdown(
                        options = listOf("All Types", "Money In", "Money Out"),
                        selectedOption = if (typeFilter == "All") "All Types" else typeFilter,
                        onOptionSelected = { option ->
                            viewModel.transactionTypeFilter = if (option == "All Types") "All" else option
                        },
                        dark = dark
                    )
                }

                // Dropdown 2: All Categories
                Box(modifier = Modifier.weight(1.5f)) {
                    FilterDropdown(
                        options = listOf("All Categories") + COST_CODES,
                        selectedOption = if (catFilter == "All") "All Categories" else catFilter,
                        onOptionSelected = { option ->
                            viewModel.transactionCategoryFilter = if (option == "All Categories") "All" else option
                        },
                        dark = dark
                    )
                }

                // Dropdown 3: Date / Sort Order
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdown(
                        options = listOf("Date", "Latest First", "Oldest First"),
                        selectedOption = if (sortOrder == "Date") "Date" else sortOrder,
                        onOptionSelected = { option ->
                            sortOrder = if (option == "Date") "Latest First" else option
                        },
                        dark = dark
                    )
                }
            }
        }

        // Grouped Date Header & Grouped Transactions Container matching reference mockup
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dark) Color(0xFF111827) else Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (dark) Color(0xFF243449) else Color(0xFFE2E8F0)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (dark) Color(0x1A38BDF8) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "Empty",
                                tint = if (dark) NeonCyan else Color(0xFF0284C7),
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Text(
                            text = "No matching cash records found",
                            color = if (dark) TextPrimary else TextPrimaryLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try adjusting search or clearing filters.",
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 12.sp
                        )

                        GlassButton(
                            onClick = {
                                viewModel.transactionSearchQuery = ""
                                viewModel.transactionTypeFilter = "All"
                                viewModel.transactionCategoryFilter = "All"
                                sortOrder = "Latest First"
                            },
                            darkTheme = dark,
                            glowColor = if (dark) NeonCyan else Color(0xFF0284C7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Filters", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        } else {
            sortedDates.forEach { date ->
                val transactionsForDate = groupedByDate[date].orEmpty()

                // Date Group Header
                item {
                    TransactionDateHeader(
                        date = date,
                        count = transactionsForDate.size,
                        dark = dark
                    )
                }

                // Grouped Cards Container for transactions on this date
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (dark) Color(0xFF0F172A) else Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (dark) Color(0xFF243449) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column {
                            transactionsForDate.forEachIndexed { index, tx ->
                                TransactionRowItem(
                                    tx = tx,
                                    dark = dark,
                                    currencyFormatter = currencyFormatter,
                                    onView = { viewModel.sharedSelectedTxDetails = tx },
                                    onDelete = { showDeleteConfirmForTx = tx }
                                )

                                if (index < transactionsForDate.size - 1) {
                                    HorizontalDivider(
                                        color = if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Caught Up illustration card
        item {
            CaughtUpCard(dark = dark)
        }

        // Bottom action buttons: EXPORT and REPORTS matching reference mockup
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val context = LocalContext.current
                // Export CSV button
                GlassButton(
                    onClick = { viewModel.exportTransactionsCSV(context) },
                    darkTheme = dark,
                    glowColor = if (dark) NeonGreen else Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export",
                            tint = if (dark) Color.White else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "EXPORT",
                            color = if (dark) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Reports button
                GlassButton(
                    onClick = {
                        viewModel.currentScreen = AppScreen.Dashboard
                    },
                    darkTheme = dark,
                    glowColor = if (dark) NeonGreen else Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Reports",
                            tint = if (dark) Color.White else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "REPORTS",
                            color = if (dark) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Detail dialog
    if (selectedTxForDetails != null) {
        val tx = selectedTxForDetails
        val isMoneyIn = tx.type == "Money In"
        val tintColor =
            if (isMoneyIn) (if (dark) NeonGreen else Color(0xFF047857))
            else (if (dark) NeonPink else Color(0xFFBE123C))

        val formattedFull = formatIndianRupeesWithLakhCr(tx.amount)

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(tintColor.copy(alpha = 0.12f))
                        .border(
                            BorderStroke(1.dp, tintColor.copy(alpha = 0.35f)),
                            RoundedCornerShape(14.dp)
                        )
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
                            text = if (isMoneyIn) "+$formattedFull" else "-$formattedFull",
                            color = tintColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailItemRow(label = "Date of Payment:", value = tx.date, dark = dark, tintColor = tintColor)
                    DetailItemRow(label = "Category:", value = tx.category, dark = dark, tintColor = tintColor, isHighlight = true)
                    DetailItemRow(label = "Payment Method:", value = tx.paymentMethod, dark = dark, tintColor = tintColor)
                    DetailItemRow(label = "Reference / Bill No:", value = tx.reference, dark = dark, tintColor = tintColor)
                    DetailItemRow(label = "Mapped Party / Payee:", value = tx.partyName ?: "No mapped party", dark = dark, tintColor = tintColor)
                    DetailItemRow(label = "Project Associated:", value = currentProject?.name ?: "Main Site", dark = dark, tintColor = tintColor)

                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
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
                                .clip(RoundedCornerShape(10.dp))
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

    // Delete confirmation dialog
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
                    "Are you sure you want to delete this transaction? This action cannot be undone.",
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

// ─────────────────────────────────────────────────────────────────────────────
// Filter Dropdown Component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FilterDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    dark: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (dark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                .border(
                    1.dp,
                    if (dark) Color(0xFF334155) else Color(0xFFCBD5E1),
                    RoundedCornerShape(10.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedOption,
                color = if (dark) TextPrimary else TextPrimaryLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(if (dark) Color(0xFF1E293B) else Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (dark) TextPrimary else TextPrimaryLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date section header matching reference mockup
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TransactionDateHeader(
    date: String,
    count: Int,
    dark: Boolean
) {
    val label = prettyDateLabel(date)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = if (dark) NeonGreen else Color(0xFF10B981),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = if (dark) Color.White else Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = if (count == 1) "1 Transaction" else "$count Transactions",
            color = if (dark) TextSecondary else TextSecondaryLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Transaction Row Item inside date card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TransactionRowItem(
    tx: Transaction,
    dark: Boolean,
    currencyFormatter: NumberFormat,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val isIn = tx.type == "Money In"

    val accentColor =
        if (isIn) (if (dark) NeonGreen else Color(0xFF047857))
        else (if (dark) NeonPink else Color(0xFFBE123C))

    val textPrimary = if (dark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val textSecondary = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var showMenu by remember { mutableStateOf(false) }

    val mockTime = remember(tx.id) {
        val hour = 9 + (tx.id % 10)
        val minute = (tx.id * 17) % 60
        val ampm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        String.format(Locale.US, "%02d:%02d %s", displayHour, minute, ampm)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onView)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Icon (solid color background with white arrow inside)
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isIn) Color(0xFF10B981) else Color(0xFFEF4444)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIn) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Title and description details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = tx.description.ifBlank { "No description provided" },
                color = textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = tx.category,
                color = textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            val refStr = if (tx.reference.isNotBlank()) " • Ref: ${tx.reference}" else ""
            Text(
                text = "${tx.paymentMethod}$refStr",
                color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right side: Amount and Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${if (isIn) "+" else "-"}${currencyFormatter.format(tx.amount)}",
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = mockTime,
                    color = textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Details",
                    tint = if (dark) Color(0xFF475569) else Color(0xFFCBD5E1),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { showMenu = true }
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(if (dark) Color(0xFF0F172A) else Color.White)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.RemoveRedEye,
                                    null,
                                    tint = if (dark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("View Details", color = if (dark) TextPrimary else TextPrimaryLight)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onView()
                        }
                    )

                    HorizontalDivider(color = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0))

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    null,
                                    tint = Color(0xFFEF4444),
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

// ─────────────────────────────────────────────────────────────────────────────
// Summary card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    dark: Boolean,
    label: String,
    value: String,
    accentColor: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (dark) Color(0xFF111827) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = if (dark) 0.22f else 0.16f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (dark) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(accentColor, CircleShape)
                )
            }

            Text(
                text = label,
                color = accentColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.7.sp
            )

            Text(
                text = value,
                color = if (dark) TextPrimary else TextPrimaryLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Caught Up Card Component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CaughtUpCard(dark: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (dark) Color(0xFF0F172A) else Color.White
        ),
        border = BorderStroke(1.dp, if (dark) Color(0xFF243449) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (dark) Color(0x1A0284C7) else Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = if (dark) NeonCyan else Color(0xFF0284C7),
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "No more transactions",
                    color = if (dark) Color.White else Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "You're all caught up!",
                    color = if (dark) TextSecondary else TextSecondaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date formatter helper
// ─────────────────────────────────────────────────────────────────────────────
private fun prettyDateLabel(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val parsed = parser.parse(raw)
        if (parsed != null) formatter.format(parsed) else raw
    } catch (_: Exception) {
        raw
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail Item Row Component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailItemRow(
    label: String,
    value: String,
    dark: Boolean,
    tintColor: Color,
    isHighlight: Boolean = false
) {
    Column {
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
                color = if (isHighlight) tintColor else if (dark) TextPrimary else TextPrimaryLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalDivider(color = if (dark) GlassBorderDark else GlassBorderLight)
    }
}