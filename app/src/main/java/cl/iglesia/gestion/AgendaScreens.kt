package cl.iglesia.gestion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

// Inicio: "Mis pendientes" ------------------------------------------------------

@Composable
fun HomeScreen(
    member: Member,
    activities: List<ChurchActivity>,
    ministries: List<Ministry>,
    notifications: List<ChurchNotification>,
    onOpenItem: (ChurchActivity) -> Unit,
    onOpenAgenda: (personal: Boolean) -> Unit,
    onOpenMyAgenda: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val today = LocalDate.now()
    val mine = activities.filter { it.involves(member.id) }
    // Trabajo propio con fecha y sin terminar desde hoy, más las tareas vencidas.
    val pending = mine
        .filter { !it.isDone && (!it.date.isBefore(today) || it.isTask) }
        .sortedWith(compareBy(ChurchActivity::date, ChurchActivity::time))
    val overdueTasks = pending.count { it.isTask && it.isOverdue() }
    val pendingTasks = pending.count { it.isTask } - overdueTasks
    val upcomingPending = pending.filterNot { it.isTask && it.isOverdue() }
    val thisWeek = mine.count { !it.isDone && !it.date.isBefore(today) && it.date.isBefore(today.plusDays(7)) }
    val unread = notifications.filter { !it.read }
    val upcomingChurch = activities
        .filter { !it.isTask && !it.isDone && !it.date.isBefore(today) && !it.involves(member.id) }
        .take(3)
    val myMinistries = ministries.filter { it.id in member.ministryIds }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ListPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                eyebrow = today.longLabel(),
                title = "Hola, ${member.firstName}",
                subtitle = buildString {
                    append(member.role)
                    if (myMinistries.isNotEmpty()) append(" · ").append(myMinistries.joinToString { it.shortName })
                },
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryTile("Vencidas", overdueTasks, Icons.Outlined.TaskAlt, Modifier.weight(1f)) { onOpenAgenda(true) }
                    SummaryTile("Tareas pendientes", pendingTasks, Icons.Outlined.TaskAlt, Modifier.weight(1f)) { onOpenAgenda(true) }
                }
            }
        }
        item { WeekOverviewCard(thisWeek, unread.size, onOpenMyAgenda, onOpenNotifications) }
        if (overdueTasks > 0) {
            item { SectionHeader("Tareas vencidas", actionLabel = "Ver agenda", onAction = { onOpenAgenda(true) }) }
            items(pending.filter { it.isTask && it.isOverdue() }.take(6), key = { "overdue-" + it.id }) { item ->
                ActivityListCard(item, ministries, onClick = { onOpenItem(item) }, showDate = true)
            }
            if (overdueTasks > 6) {
                item {
                    TextButton(onClick = { onOpenAgenda(true) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver ${overdueTasks - 6} tareas vencidas más")
                    }
                }
            }
        }
        item { SectionHeader("Próximos pendientes", actionLabel = "Ver agenda", onAction = { onOpenAgenda(true) }) }
        if (upcomingPending.isEmpty()) {
            item {
                EmptyState(
                    if (overdueTasks > 0) "No tienes otros pendientes próximos" else "Estás al día",
                    if (overdueTasks > 0) "Las tareas vencidas aparecen en la sección anterior." else "No tienes tareas ni actividades pendientes con fecha.",
                    Icons.Outlined.EventAvailable,
                )
            }
        } else {
            items(upcomingPending.take(6), key = { "p-" + it.kind.name + it.id }) { item ->
                ActivityListCard(item, ministries, onClick = { onOpenItem(item) }, showDate = true)
            }
            if (upcomingPending.size > 6) {
                item {
                    TextButton(onClick = { onOpenAgenda(true) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver ${upcomingPending.size - 6} pendientes más")
                    }
                }
            }
        }
        if (unread.isNotEmpty()) {
            item { SectionHeader("Avisos sin leer", actionLabel = "Ver todos", onAction = onOpenNotifications) }
            items(unread.take(3), key = { "n-" + it.id }) { notification ->
                AppCard(onClick = onOpenNotifications, containerColor = GoldSurface, contentPadding = 14.dp) {
                    Text(notification.message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    notification.createdAt?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it.relativeLabel(), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
        if (upcomingChurch.isNotEmpty()) {
            item { SectionHeader("Próximo en la iglesia", actionLabel = "Agenda general", onAction = { onOpenAgenda(false) }) }
            items(upcomingChurch, key = { "u-" + it.kind.name + it.id }) { item ->
                ActivityListCard(item, ministries, onClick = { onOpenItem(item) }, showDate = true)
            }
        }
    }
}

@Composable
private fun WeekOverviewCard(
    activityCount: Int,
    unreadCount: Int,
    onOpenMyAgenda: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Brand, BrandLight)))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "ESTA SEMANA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.78f),
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    activityCount.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (activityCount == 1) "actividad próxima" else "actividades próximas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
            Box(
                Modifier.size(60.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
        val largeText = LocalDensity.current.fontScale >= 1.25f
        if (largeText) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OverviewAction("Abrir agenda", Icons.Outlined.CalendarMonth, Modifier.fillMaxWidth()) { onOpenMyAgenda() }
                OverviewAction("Avisos · $unreadCount", Icons.Outlined.NotificationsNone, Modifier.fillMaxWidth(), onOpenNotifications)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OverviewAction("Abrir agenda", Icons.Outlined.CalendarMonth) { onOpenMyAgenda() }
                OverviewAction("Avisos · $unreadCount", Icons.Outlined.NotificationsNone, onClick = onOpenNotifications)
            }
        }
    }
}

@Composable
private fun OverviewAction(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .semantics { contentDescription = label; role = Role.Button }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SummaryTile(
    label: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    AppCard(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = "$label: $value" },
        onClick = onClick,
        contentPadding = 12.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, size = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(value.toString(), style = MaterialTheme.typography.titleMedium, color = Brand)
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Trabajo propio sin terminar desde hoy, más las tareas vencidas. Inicio y la vista
 * "Pendientes" de la Agenda usan esta misma regla para que sus números coincidan.
 */
fun pendingFor(member: Member, activities: List<ChurchActivity>, today: LocalDate = LocalDate.now()): List<ChurchActivity> =
    activities
        .filter { it.involves(member.id) && !it.isDone && (!it.date.isBefore(today) || it.isTask) }
        .sortedWith(compareBy(ChurchActivity::date, ChurchActivity::time))

private enum class PendingGroup(val title: String) {
    Overdue("Vencidas"),
    Today("Hoy"),
    Soon("Próximos días"),
    Later("Más adelante"),
}

private fun ChurchActivity.pendingGroup(today: LocalDate): PendingGroup = when {
    date.isBefore(today) -> PendingGroup.Overdue
    date == today -> PendingGroup.Today
    date.isBefore(today.plusDays(7)) -> PendingGroup.Soon
    else -> PendingGroup.Later
}

// Tarjeta de elemento de agenda --------------------------------------------------

@Composable
fun ActivityListCard(
    item: ChurchActivity,
    ministries: List<Ministry>,
    onClick: () -> Unit,
    showDate: Boolean = false,
) {
    val ministry = ministries.find { it.id == item.ministryId }
    val accent = ministry?.color?.toColor() ?: Brand
    val overdue = item.isTask && item.isOverdue()
    AppCard(onClick = onClick, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.Top) {
            SymbolBadge(item.typeSymbol(ministry), accent, size = 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                val whenText = buildString {
                    if (showDate) append(item.date.relativeLabel()).append(" · ")
                    append(item.time)
                    if (!item.isTask && item.endTime.isNotBlank()) append("–").append(item.endTime)
                }
                Text(whenText, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (item.location.isNotBlank()) {
                    Text(item.location, style = MaterialTheme.typography.bodySmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                item.parentActivityTitle?.let {
                    Text("En: $it", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(item.typeLabel, Brand, BrandSurface)
                    if (!item.isMeeting) StatusPill(item.status)
                    if (overdue) Pill("Vencida", Danger, DangerSurface)
                }
            }
        }
    }
}

private fun ChurchActivity.typeSymbol(ministry: Ministry?): String = when {
    isTask -> "✓"
    isMeeting -> "◎"
    category == ActivityTypes.CULTO -> "✝"
    category == ActivityTypes.ENSAYO -> "♫"
    category == ActivityTypes.ASEO -> "✦"
    category == ActivityTypes.LEADERS_MEETING -> "◎"
    else -> ministry?.icon ?: "▣"
}

// Agenda -------------------------------------------------------------------------

private val TypeFilters = listOf("Todos") + ActivityTypes.all + listOf("Tarea", "Reunión")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    member: Member,
    permissions: Permissions,
    activities: List<ChurchActivity>,
    ministries: List<Ministry>,
    personal: Boolean,
    onPersonalChange: (Boolean) -> Unit,
    pendingView: Boolean,
    onPendingViewChange: (Boolean) -> Unit,
    onOpenItem: (ChurchActivity) -> Unit,
    onCreate: (FormRequest) -> Unit,
) {
    val today = LocalDate.now()
    // La lista de pendientes solo existe en la agenda personal.
    val showingPending = personal && pendingView
    val pendingGroups = if (showingPending) {
        pendingFor(member, activities, today).groupBy { it.pendingGroup(today) }
    } else {
        emptyMap()
    }
    var selectedDate by rememberSaveable { mutableStateOf(today) }
    var typeFilter by rememberSaveable { mutableStateOf("Todos") }
    var createMenu by remember { mutableStateOf(false) }
    val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val week = (0L..6L).map(weekStart::plusDays)

    val visible = activities.filter { item ->
        (!personal || item.involves(member.id)) &&
            (typeFilter == "Todos" || item.typeLabel == typeFilter)
    }
    val datesWithItems = visible.mapTo(mutableSetOf(), ChurchActivity::date)
    val dayItems = visible.filter { it.date == selectedDate }.sortedBy(ChurchActivity::time)

    Box(Modifier.fillMaxSize()) {
        // Deslizar a los lados cambia de semana (solo en la vista por día; las flechas siguen igual).
        val swipeWeeks = Modifier.pointerInput(showingPending) {
            if (showingPending) return@pointerInput
            var total = 0f
            detectHorizontalDragGestures(
                onDragStart = { total = 0f },
                onHorizontalDrag = { _, delta -> total += delta },
                onDragEnd = {
                    if (total <= -140f) selectedDate = selectedDate.plusWeeks(1)
                    else if (total >= 140f) selectedDate = selectedDate.minusWeeks(1)
                },
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().then(swipeWeeks),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ScreenHeader(
                        "Calendario",
                        "Agenda",
                        when {
                            showingPending -> "Lo que te falta por hacer, empezando por lo vencido."
                            personal -> "Lo que tienes asignado o donde participas."
                            else -> "Toda la actividad visible para ti."
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                    // Un solo selector en lugar de dos apilados: General, Mi agenda (por día) o Pendientes.
                    val modes = listOf("General", "Mi agenda", "Pendientes")
                    val selectedMode = when {
                        !personal -> 0
                        pendingView -> 2
                        else -> 1
                    }
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        modes.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = selectedMode == index,
                                onClick = {
                                    onPersonalChange(index != 0)
                                    if (index != 0) onPendingViewChange(index == 2)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                                colors = SegmentedButtonDefaults.colors(activeContainerColor = BrandContainer, activeContentColor = Brand),
                            ) { Text(label, maxLines = 1) }
                        }
                    }
                }
            }
            if (showingPending) {
                if (pendingGroups.isEmpty()) {
                    item {
                        EmptyState(
                            "Estás al día",
                            "No tienes tareas ni actividades pendientes.",
                            Icons.Outlined.EventAvailable,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                PendingGroup.entries.forEach { group ->
                    val groupItems = pendingGroups[group].orEmpty()
                    if (groupItems.isNotEmpty()) {
                        item(key = "g-" + group.name) {
                            Text(
                                "${group.title} (${groupItems.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (group == PendingGroup.Overdue) Danger else TextPrimary,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .semantics { heading() },
                            )
                        }
                        items(groupItems, key = { "pg-" + it.kind.name + it.id }) { item ->
                            Box(Modifier.padding(horizontal = 16.dp)) {
                                ActivityListCard(item, ministries, onClick = { onOpenItem(item) }, showDate = true)
                            }
                        }
                    }
                }
            } else {
            item {
                Column(Modifier.padding(horizontal = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedDate = selectedDate.minusWeeks(1) }) {
                            Icon(Icons.Outlined.ChevronLeft, contentDescription = "Semana anterior")
                        }
                        Text(
                            weekStart.monthLabel(),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        if (selectedDate != today) {
                            TextButton(onClick = { selectedDate = today }) { Text("Hoy") }
                        }
                        IconButton(onClick = { selectedDate = selectedDate.plusWeeks(1) }) {
                            Icon(Icons.Outlined.ChevronRight, contentDescription = "Semana siguiente")
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEach { date ->
                            DayCell(
                                date = date,
                                selected = date == selectedDate,
                                isToday = date == today,
                                hasItems = date in datesWithItems,
                                onClick = { selectedDate = date },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(TypeFilters) { filter ->
                        FilterChip(
                            selected = typeFilter == filter,
                            onClick = { typeFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandContainer,
                                selectedLabelColor = Brand,
                            ),
                        )
                    }
                }
            }
            item {
                Text(
                    selectedDate.longLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            if (dayItems.isEmpty()) {
                item {
                    EmptyState(
                        "Sin elementos este día",
                        when {
                            personal -> "No tienes asignaciones para esta fecha. Prueba otro día o cambia a la agenda general."
                            typeFilter != "Todos" -> "No hay elementos de tipo «$typeFilter» para esta fecha. Prueba otro filtro o día."
                            else -> "No hay actividades visibles para esta fecha. Prueba otro día o revisa la agenda personal."
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                items(dayItems, key = { it.kind.name + it.id }) { item ->
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        ActivityListCard(item, ministries, onClick = { onOpenItem(item) })
                    }
                }
            }
            }
        }

        if (permissions.canManageAnyMinistry) {
            Box(Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                ExtendedFloatingActionButton(
                    onClick = { createMenu = true },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Nuevo") },
                    containerColor = Brand,
                    contentColor = Color.White,
                )
                DropdownMenu(expanded = createMenu, onDismissRequest = { createMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Actividad") },
                        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                        onClick = {
                            createMenu = false
                            onCreate(FormRequest.Activity)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Tarea de una actividad") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Assignment, contentDescription = null) },
                        onClick = {
                            createMenu = false
                            onCreate(FormRequest.Task(null))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Convocatoria de reunión") },
                        leadingIcon = { Icon(Icons.Outlined.Groups, contentDescription = null) },
                        onClick = {
                            createMenu = false
                            onCreate(FormRequest.Meeting)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    isToday: Boolean,
    hasItems: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        selected -> Brand
        isToday -> BrandSurface
        else -> Color.White
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                this.selected = selected
                contentDescription = date.longLabel() + if (hasItems) ", con actividades" else ""
            }
            .heightIn(min = 64.dp)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            date.weekdayInitials(),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White.copy(alpha = 0.85f) else TextSecondary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) Color.White else TextPrimary,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.size(6.dp).clip(CircleShape)
                .background(if (hasItems) (if (selected) Gold else BrandLight) else Color.Transparent),
        )
    }
}

// Detalle --------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailSheet(
    item: ChurchActivity,
    member: Member,
    permissions: Permissions,
    activities: List<ChurchActivity>,
    ministries: List<Ministry>,
    members: List<Member>,
    projects: List<ChurchProject>,
    worshipSets: List<WorshipSet>,
    songs: List<WorshipSong>,
    meetingRecords: List<MeetingRecord>,
    submitting: Boolean,
    onDismiss: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenItem: (ChurchActivity) -> Unit,
    onAddTask: () -> Unit,
    onEditRepertoire: () -> Unit,
    onEditAssignees: () -> Unit,
    onChangeStatus: (String) -> Unit,
    onOpenFile: (RecordFile) -> Unit = {},
    onAddComment: (String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ministry = ministries.find { it.id == item.ministryId }
    val project = projects.find { it.id == item.projectId }
    fun nameOf(id: String?) = members.find { it.id == id }?.name
    val canManage = permissions.canManage(item)
    val canChangeStatus = when {
        item.isMeeting -> false
        item.isTask -> item.isAssignedToCurrentUser || canManage
        else -> canManage
    }
    val childTasks = if (item.kind == ItemKind.Activity) activities.filter { it.parentActivityId == item.id } else emptyList()
    val isCulto = item.kind == ItemKind.Activity && item.category == ActivityTypes.CULTO
    // En staging el repertorio no depende del culto: se muestra el vigente.
    val worshipSet = worshipSets.ministryRepertoire()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(item.typeLabel, Brand, BrandSurface)
                if (!item.isMeeting) StatusPill(item.status)
                if (item.isTask && item.isOverdue()) Pill("Vencida", Danger, DangerSurface)
            }
            Text(item.title, style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Column {
                DetailRow(
                    Icons.Outlined.Schedule,
                    when {
                        !item.isTask -> "${item.date.longLabel()} · ${item.time}" + if (item.endTime.isNotBlank()) "–${item.endTime}" else ""
                        item.dueAt != null -> "Fecha límite: ${item.date.longLabel()} · ${item.time}"
                        else -> "Sin fecha límite (actividad: ${item.date.shortLabel()})"
                    },
                )
                if (item.location.isNotBlank()) DetailRow(Icons.Outlined.Place, item.location)
                val classification = when (item.classification) {
                    Classification.Ministry -> "Ministerio: ${ministry?.name ?: "—"}"
                    Classification.Project -> "Proyecto: ${project?.name ?: "—"}"
                    Classification.Individual -> "Actividad individual"
                    Classification.General -> if (item.isMeeting) "Reunión general" else "General (toda la iglesia)"
                }
                DetailRow(
                    if (item.classification == Classification.Project) Icons.Outlined.FolderOpen else Icons.Outlined.Groups,
                    classification,
                )
            }
            if (item.description.isNotBlank()) {
                LabeledValue(if (item.isTask) "Instrucciones" else "Descripción", item.description)
            }
            if (item.isTask) {
                item.parentActivityTitle?.let { LabeledValue("Actividad", it) }
                item.priority?.let { LabeledValue("Prioridad", it.toPriorityLabel()) }
            }
            if (isCulto) {
                LabeledValue("Predicador", nameOf(item.preacherId) ?: "Sin asignar")
                LabeledValue("Director de alabanza", nameOf(item.worshipLeaderId) ?: "Sin asignar")
            }
            val people = item.assigneeIds.mapNotNull { id -> members.find { it.id == id } }
            val canEditAssignees = canManage && !item.isMeeting
            if (item.assigneeIds.isNotEmpty() || canEditAssignees) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            when {
                                item.isMeeting || item.category == ActivityTypes.ENSAYO -> "Invitados"
                                item.isTask -> "Responsable"
                                else -> "Responsables"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted,
                            modifier = Modifier.weight(1f),
                        )
                        if (canEditAssignees) {
                            TextButton(onClick = onEditAssignees, enabled = !submitting) {
                                Text(if (item.isTask) "Reasignar" else "Cambiar")
                            }
                        }
                    }
                    if (item.assigneeIds.isEmpty()) {
                        Text("Sin responsable asignado.", style = MaterialTheme.typography.bodySmall, color = Warning)
                    }
                    people.forEach { person ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(person.initials, size = 28.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(person.name + if (person.id == member.id) " (tú)" else "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    val hidden = item.assigneeIds.size - people.size
                    if (hidden > 0) {
                        Text("y $hidden persona(s) más", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }

            if (item.isTask) {
                HorizontalDivider(color = CardBorder)
                Text("Comentarios (${item.taskComments.size})", style = MaterialTheme.typography.titleSmall)
                if (item.taskComments.isEmpty()) {
                    Text("Aún no hay comentarios.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                item.taskComments.forEach { comment ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        val whenLabel = comment.createdAt?.dateTimeLabel()
                        Text(
                            listOfNotNull(nameOf(comment.authorId) ?: "Alguien", whenLabel).joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                        Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                var newComment by remember(item.id) { mutableStateOf("") }
                TextInput(newComment, { newComment = it }, "Escribe un comentario", singleLine = false, minLines = 2)
                OutlinedButton(
                    onClick = {
                        onAddComment(newComment)
                        newComment = ""
                    },
                    enabled = !submitting && StagingTasks.commentOrNull(newComment) != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Comentar")
                }
            }

            if (item.isMeeting) {
                RecordLinks(meetingRecords.filter { it.meetingId == item.id }, onOpenRecords)
            }

            if (canChangeStatus) {
                HorizontalDivider(color = CardBorder)
                FieldLabel("Cambiar estado")
                ChoiceChips(
                    options = WORK_STATUSES,
                    selected = item.status,
                    label = { statusStyle(it).label },
                    onSelect = { if (it != item.status && !submitting) onChangeStatus(it) },
                    enabled = { !submitting },
                )
                if (item.isTask && !canManage) {
                    Text("Como responsable, solo puedes cambiar el estado de tu tarea.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }

            // Cultos y ensayos muestran el repertorio vigente, con partituras para ensayar.
            if (isCulto || (item.kind == ItemKind.Activity && item.category == ActivityTypes.ENSAYO)) {
                HorizontalDivider(color = CardBorder)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LibraryMusic, contentDescription = null, tint = BrandLight)
                    Spacer(Modifier.width(8.dp))
                    Text("Repertorio", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    if (permissions.canManageWorship) {
                        TextButton(onClick = onEditRepertoire) { Text(if (worshipSet == null) "Crear" else "Editar") }
                    }
                }
                RepertoireList(worshipSet, songs, members, onOpenFile)
            }

            if (item.kind == ItemKind.Activity) {
                HorizontalDivider(color = CardBorder)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.TaskAlt, contentDescription = null, tint = BrandLight)
                    Spacer(Modifier.width(8.dp))
                    Text("Tareas (${childTasks.size})", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    if (canManage) {
                        TextButton(onClick = onAddTask) { Text("Agregar") }
                    }
                }
                if (childTasks.isEmpty()) {
                    Text(
                        if (item.status == "completed") "Sin tareas: la actividad cuenta como 100 % de avance." else "Sin tareas visibles para ti.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                } else {
                    val done = childTasks.count { it.status == "completed" }
                    Text("$done de ${childTasks.size} completadas", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    childTasks.forEach { task ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onOpenItem(task) }
                                .heightIn(min = 56.dp)
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(task.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(nameOf(task.assigneeIds.firstOrNull()) ?: "Responsable", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            StatusPill(task.status)
                        }
                    }
                }
            }
            if (item.isTask && item.parentActivityId != null) {
                activities.firstOrNull { it.id == item.parentActivityId }?.let { parent ->
                    OutlinedButton(onClick = { onOpenItem(parent) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver actividad: ${parent.title}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun RepertoireList(
    worshipSet: WorshipSet?,
    songs: List<WorshipSong>,
    members: List<Member>,
    onOpenFile: (RecordFile) -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    if (worshipSet == null || worshipSet.songs.isEmpty()) {
        Text("Aún no hay canciones en el repertorio.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        worshipSet.songs.forEach { entry ->
            val song = songs.find { it.id == entry.songId }
            Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${entry.position}.", style = MaterialTheme.typography.labelLarge, color = BrandLight, modifier = Modifier.width(28.dp))
                Column(Modifier.weight(1f)) {
                    Text(song?.title ?: "Canción", style = MaterialTheme.typography.bodyMedium)
                    val lead = members.find { it.id == entry.leadId }?.name
                    val details = listOfNotNull(song?.artist?.takeIf(String::isNotBlank), lead?.let { "Dirige: $it" })
                    if (details.isNotEmpty()) {
                        Text(details.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                Pill("Tono ${entry.key}", Brand, BrandSurface)
                if (song?.referenceUrl?.startsWith("https://") == true) {
                    TextButton(onClick = { runCatching { uriHandler.openUri(song.referenceUrl) } }) { Text("Versión") }
                }
            }
            // Partituras y letras para ensayar: visibles para todo el equipo.
            song?.files?.forEach { file ->
                Row(Modifier.padding(start = 28.dp)) {
                    FileRow(
                        name = file.name,
                        contentType = file.contentType,
                        size = file.sizeBytes,
                        onOpen = { onOpenFile(file) },
                        onRemove = null,
                    )
                }
            }
        }
        worshipSet.updatedAt?.let {
            Text("Última modificación: ${it.relativeLabel()}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}
