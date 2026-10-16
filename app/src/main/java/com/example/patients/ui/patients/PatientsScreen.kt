package com.example.patients.ui.patients

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.R
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.ui.theme.StatusActiveBg
import com.example.patients.ui.theme.StatusActiveText
import com.example.patients.ui.theme.StatusInactiveBg
import com.example.patients.ui.theme.StatusInactiveText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    viewModel: PatientViewModel = viewModel(factory = PatientViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var patientToEdit by remember { mutableStateOf<Patient?>(null) }
    var patientToDelete by remember { mutableStateOf<Patient?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cabinet Médical",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPatients() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nouveau", fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            when (val state = uiState) {
                is PatientsUiState.Loading ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Chargement des patients...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        )
                    }

                is PatientsUiState.Error ->
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadPatients() },
                        modifier = Modifier.align(Alignment.Center),
                    )

                is PatientsUiState.Success ->
                    PatientListContent(
                        patients = state.patients,
                        onEditRequest = { patientToEdit = it },
                        onDeleteRequest = { patientToDelete = it },
                    )
            }
        }
    }

    if (showAddDialog) {
        AddPatientDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { request ->
                viewModel.addPatient(request)
                showAddDialog = false
            },
        )
    }

    patientToEdit?.let { patient ->
        EditPatientDialog(
            patient = patient,
            onDismiss = { patientToEdit = null },
            onConfirm = { request ->
                viewModel.updatePatient(patient.id, request)
                patientToEdit = null
            },
        )
    }

    patientToDelete?.let { patient ->
        AlertDialog(
            onDismissRequest = { patientToDelete = null },
            title = { Text("Confirmer la suppression") },
            text = { Text("Voulez-vous vraiment supprimer le patient ${patient.prenom} ${patient.nom} ?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePatient(patient)
                        patientToDelete = null
                    },
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { patientToDelete = null }) {
                    Text("Annuler")
                }
            },
        )
    }
}

@Composable
fun CabinetHeaderCard(
    totalCount: Int,
    activeCount: Int,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_medical_header),
                contentDescription = "Bannière Cabinet Médical",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.65f),
                                Color.Black.copy(alpha = 0.20f),
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Cabinet de Consultation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Gestion & Suivi Médical des Patients",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Total : ",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                            )
                            Text(
                                text = "$totalCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Actifs : ",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                            )
                            Text(
                                text = "$activeCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientListContent(
    patients: List<Patient>,
    onEditRequest: (Patient) -> Unit,
    onDeleteRequest: (Patient) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("Tous") }

    val activeCount = remember(patients) {
        patients.count { it.statut.equals("Actif", ignoreCase = true) }
    }

    val filteredPatients = remember(patients, searchQuery, selectedFilter) {
        patients.filter { patient ->
            val matchesQuery = patient.nom.contains(searchQuery, ignoreCase = true) ||
                patient.prenom.contains(searchQuery, ignoreCase = true) ||
                (patient.medecin?.contains(searchQuery, ignoreCase = true) ?: false)

            val matchesFilter = when (selectedFilter) {
                "Actif" -> patient.statut.equals("Actif", ignoreCase = true)
                "Inactif" -> !patient.statut.equals("Actif", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête illustré du Cabinet Médical
        CabinetHeaderCard(
            totalCount = patients.size,
            activeCount = activeCount,
        )

        // Barre de recherche
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("Rechercher un patient ou un médecin...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
        )

        // Filtres par statut
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            val filters = listOf("Tous", "Actif", "Inactif")
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                )
            }
        }

        if (filteredPatients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Aucun patient ne correspond à la recherche" else "Aucun patient disponible dans le cabinet",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filteredPatients, key = { it.id }) { patient ->
                    PatientCardItem(
                        patient = patient,
                        onEditRequest = { onEditRequest(patient) },
                        onDeleteRequest = { onDeleteRequest(patient) },
                    )
                }
            }
        }
    }
}

@Composable
fun PatientCardItem(
    patient: Patient,
    onEditRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditRequest() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val initials = "${patient.prenom.take(1)}${patient.nom.take(1)}".uppercase()
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "${patient.prenom} ${patient.nom}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val isActive = patient.statut.equals("Actif", ignoreCase = true)
                    Surface(
                        color = if (isActive) StatusActiveBg else StatusInactiveBg,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = patient.statut,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) StatusActiveText else StatusInactiveText,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val details = listOfNotNull(
                    patient.age?.let { "$it ans" },
                    patient.medecin?.let { "Dr. $it" },
                    patient.telephone,
                )

                if (details.isNotEmpty()) {
                    Text(
                        text = details.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row {
                IconButton(onClick = onEditRequest) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onDeleteRequest) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier.padding(24.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Une erreur est survenue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Réessayer")
            }
        }
    }
}

@Composable
fun AddPatientDialog(onDismiss: () -> Unit, onConfirm: (PatientRequest) -> Unit) {
    var nom by rememberSaveable { mutableStateOf("") }
    var prenom by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var telephone by rememberSaveable { mutableStateOf("") }
    var medecin by rememberSaveable { mutableStateOf("") }
    val formulaireValide = nom.isNotBlank() && prenom.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Nouveau Patient",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = prenom,
                    onValueChange = { prenom = it },
                    label = { Text("Prénom *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Âge") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Téléphone") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = medecin,
                    onValueChange = { medecin = it },
                    label = { Text("Médecin traitant") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = formulaireValide,
                onClick = {
                    onConfirm(
                        PatientRequest(
                            nom = nom.trim(),
                            prenom = prenom.trim(),
                            age = age.toIntOrNull(),
                            telephone = telephone.trim().ifBlank { null },
                            medecin = medecin.trim().ifBlank { null },
                        ),
                    )
                },
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
    )
}

@Composable
fun EditPatientDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onConfirm: (PatientRequest) -> Unit,
) {
    var nom by rememberSaveable { mutableStateOf(patient.nom) }
    var prenom by rememberSaveable { mutableStateOf(patient.prenom) }
    var age by rememberSaveable { mutableStateOf(patient.age?.toString() ?: "") }
    var telephone by rememberSaveable { mutableStateOf(patient.telephone ?: "") }
    var medecin by rememberSaveable { mutableStateOf(patient.medecin ?: "") }
    val formulaireValide = nom.isNotBlank() && prenom.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Modifier le Patient",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = prenom,
                    onValueChange = { prenom = it },
                    label = { Text("Prénom *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Âge") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Téléphone") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = medecin,
                    onValueChange = { medecin = it },
                    label = { Text("Médecin traitant") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = formulaireValide,
                onClick = {
                    onConfirm(
                        PatientRequest(
                            nom = nom.trim(),
                            prenom = prenom.trim(),
                            age = age.toIntOrNull(),
                            telephone = telephone.trim().ifBlank { null },
                            medecin = medecin.trim().ifBlank { null },
                        ),
                    )
                },
            ) {
                Text("Sauvegarder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
    )
}
