package ru.familybudget.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Op(val title: String, val amount: Double, val income: Boolean, val account: String)
enum class Tab(val title: String) { HOME("Главная"), OPS("Операции"), BUDGET("Бюджет"), ANALYTICS("Аналитика"), MORE("Ещё") }
private val TBankYellow = Color(0xFFFFDD2D)
private val ChartGray = Color(0xFF8A8F98)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    var dark by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(Tab.HOME) }
    var add by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(false) }
    val ops = remember { mutableStateListOf<Op>() }
    val colors = if (dark) darkColorScheme(primary = TBankYellow, background = Color(0xFF111111), surface = Color(0xFF1B1B1B))
    else lightColorScheme(primary = Color(0xFF111111), background = Color(0xFFF6F6F6), surface = Color.White)

    MaterialTheme(colorScheme = colors) {
        Scaffold(
            topBar = {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Семейный бюджет", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("8.1 PRO • smart finance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { settings = true }) { Icon(Icons.Rounded.Settings, contentDescription = "Настройки") }
                    }
                }
            },
            bottomBar = {
                NavigationBar { Tab.values().forEach { t ->
                    NavigationBarItem(selected = t == tab, onClick = { tab = t }, icon = { Icon(icon(t), contentDescription = t.title) }, label = { Text(t.title, fontSize = 10.sp) })
                } }
            },
            floatingActionButton = {
                if (tab == Tab.HOME || tab == Tab.OPS) FloatingActionButton(onClick = { add = true }, containerColor = TBankYellow, contentColor = Color.Black) { Icon(Icons.Rounded.Add, contentDescription = "Добавить") }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    Tab.HOME -> Home(ops)
                    Tab.OPS -> OpsPage(ops)
                    Tab.BUDGET -> Budget()
                    Tab.ANALYTICS -> Analytics(ops)
                    Tab.MORE -> More { settings = true }
                }
            }
        }
        if (add) Add(close = { add = false }, save = { ops.add(it); add = false })
        if (settings) Settings(dark, { dark = it }) { settings = false }
    }
}

fun icon(t: Tab) = when (t) {
    Tab.HOME -> Icons.Rounded.Home
    Tab.OPS -> Icons.Rounded.Payments
    Tab.BUDGET -> Icons.Rounded.Savings
    Tab.ANALYTICS -> Icons.Rounded.Analytics
    Tab.MORE -> Icons.Rounded.MoreHoriz
}

@Composable
fun Page(content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp), content = content)

@Composable
fun Home(ops: List<Op>) {
    val income = ops.filter { it.income }.sumOf { it.amount }
    val expense = ops.filter { !it.income }.sumOf { it.amount }
    Page {
        Text("ОБЗОР", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = TBankYellow)) {
            Column(Modifier.padding(22.dp)) {
                Text("ОБЩИЙ БАЛАНС", fontSize = 12.sp, color = Color.Black.copy(alpha = .6f))
                Text("%.0f ₽".format(income - expense), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Доходы %.0f ₽".format(income), color = Color.Black); Text("Расходы %.0f ₽".format(expense), color = Color.Black) }
            }
        }
        Spacer(Modifier.height(14.dp)); Text("СЧЕТА", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        listOf("Яндекс", "ОЗОН", "Сбербанк", "Т-Банк", "Кредитка").forEach { Account(it) }
        Spacer(Modifier.height(8.dp)); Text("ПОСЛЕДНИЕ ОПЕРАЦИИ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (ops.isEmpty()) Empty("Пока нет операций", "Нажмите +, чтобы добавить первую") else ops.takeLast(4).reversed().forEach { RowOp(it) }
    }
}

@Composable
fun Account(name: String) = Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), RoundedCornerShape(17.dp)) {
    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.CreditCard, contentDescription = null); Spacer(Modifier.width(10.dp)); Text(name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Text("0 ₽", fontWeight = FontWeight.Bold) }
}

@Composable
fun OpsPage(ops: List<Op>) = Page { Text("ОПЕРАЦИИ", fontSize = 24.sp, fontWeight = FontWeight.Bold); if (ops.isEmpty()) Empty("Операций пока нет", "Добавьте доход или расход") else LazyColumn { items(ops.reversed()) { RowOp(it) } } }

@Composable
fun RowOp(o: Op) = Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
    Icon(if (o.income) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward, contentDescription = null); Spacer(Modifier.width(10.dp))
    Column(Modifier.weight(1f)) { Text(o.title, fontWeight = FontWeight.SemiBold); Text(o.account, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    Text((if (o.income) "+" else "−") + "%.0f ₽".format(o.amount), fontWeight = FontWeight.Bold)
}

@Composable
fun Budget() = Page {
    Text("БЮДЖЕТ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Text("ПЛАНИРУЙТЕ РАСХОДЫ И КОНТРОЛИРУЙТЕ ЛИМИТЫ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    listOf("ПРОДУКТЫ" to .42f, "ТРАНСПОРТ" to .27f, "ЖИЛЬЁ" to .70f, "РАЗВЛЕЧЕНИЯ" to .18f).forEach { (name, progress) ->
        Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), RoundedCornerShape(18.dp)) { Column(Modifier.padding(15.dp)) { Text(name, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = ChartGray) } }
    }
}

@Composable
fun Analytics(ops: List<Op>) = Page {
    Text("АНАЛИТИКА", fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp))
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp)) { Column(Modifier.padding(18.dp)) {
        Text("РАСХОДЫ ПО МЕСЯЦАМ", fontWeight = FontWeight.Bold); Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth().height(140.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) { listOf(.3f, .5f, .4f, .7f, .55f, .8f).forEach { value -> Box(Modifier.width(28.dp).height((120 * value).dp).background(ChartGray, RoundedCornerShape(7.dp))) } }
        HorizontalDivider(Modifier.padding(vertical = 12.dp)); Text("ВСЕГО РАСХОДОВ: %.0f ₽".format(ops.filter { !it.income }.sumOf { it.amount }), fontWeight = FontWeight.Bold)
    } }
}

@Composable
fun More(open: () -> Unit) = Page {
    Text("ЕЩЁ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    listOf("Регулярные платежи", "Финансовые цели", "Долги и кредиты", "Календарь", "Отчёты", "Экспорт данных").forEach { Card(Modifier.fillMaxWidth().padding(4.dp), RoundedCornerShape(17.dp)) { Text(it, Modifier.padding(16.dp)) } }
    Button(onClick = open, modifier = Modifier.fillMaxWidth()) { Text("НАСТРОЙКИ") }
}

@Composable
fun Empty(title: String, subtitle: String) = Column(Modifier.fillMaxWidth().padding(top = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Rounded.Payments, contentDescription = null, Modifier.size(46.dp)); Text(title, fontWeight = FontWeight.Bold); Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }

@Composable
fun Add(close: () -> Unit, save: (Op) -> Unit) {
    var name by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var income by remember { mutableStateOf(false) }; var account by remember { mutableStateOf("Т-Банк") }
    AlertDialog(onDismissRequest = close, title = { Text("НОВАЯ ОПЕРАЦИЯ") }, text = {
        Column {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") })
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Сумма") })
            Row { FilterChip(selected = income, onClick = { income = true }, label = { Text("Доход") }); Spacer(Modifier.width(8.dp)); FilterChip(selected = !income, onClick = { income = false }, label = { Text("Расход") }) }
            Spacer(Modifier.height(8.dp)); Text("СЧЁТ"); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("Яндекс", "ОЗОН", "Сбербанк", "Т-Банк", "Кредитка").forEach { AssistChip(onClick = { account = it }, label = { Text(it) }) } }
        }
    }, confirmButton = { Button(onClick = { val value = amount.replace(',', '.').toDoubleOrNull() ?: 0.0; if (name.isNotBlank() && value > 0) save(Op(name, value, income, account)) }) { Text("СОХРАНИТЬ") } }, dismissButton = { TextButton(onClick = close) { Text("ОТМЕНА") } })
}

@Composable
fun Settings(dark: Boolean, setDark: (Boolean) -> Unit, close: () -> Unit) = AlertDialog(onDismissRequest = close, title = { Text("НАСТРОЙКИ") }, text = { Row(verticalAlignment = Alignment.CenterVertically) { Text("Тёмная тема", Modifier.weight(1f)); Switch(checked = dark, onCheckedChange = setDark) } }, confirmButton = { TextButton(onClick = close) { Text("ГОТОВО") } })
