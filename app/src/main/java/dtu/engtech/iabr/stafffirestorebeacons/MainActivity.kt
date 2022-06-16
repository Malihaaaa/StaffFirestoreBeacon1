package dtu.engtech.iabr.stafffirestorebeacons

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.compose.foundation.layout.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import com.estimote.proximity_sdk.api.*
import com.google.firebase.firestore.FirebaseFirestore
import dtu.engtech.iabr.stafffirestorebeacons.core.CloudCredentials.APP_ID
import dtu.engtech.iabr.stafffirestorebeacons.core.CloudCredentials.APP_TOKEN
import dtu.engtech.iabr.stafffirestorebeacons.core.FirestoreBeaconConstants
import dtu.engtech.iabr.stafffirestorebeacons.core.Lokation
import dtu.engtech.iabr.stafffirestorebeacons.model.StaffMember
import dtu.engtech.iabr.stafffirestorebeacons.ui.StaffScreen
import dtu.engtech.iabr.stafffirestorebeacons.ui.StaffViewModel
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.compose.ui.platform.ContextAmbient


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color.Companion.White

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField

import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.text.style.TextAlign
import dtu.engtech.iabr.stafffirestorebeacons.ui.theme.*


class MainActivity : ComponentActivity() {

    private lateinit var proximityObserver: ProximityObserver
    private var proximityObservationHandler: ProximityObserver.Handler? = null
    private val cloudCredentials = EstimoteCloudCredentials(
        APP_ID,
        APP_TOKEN
    )

    private val staffViewModel by viewModels<StaffViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {


            StaffFirestoreBeaconsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background

                ) {
                    //TO DO change hard coded
                    var location = "UNKNOWN"
                    if(staffViewModel.staffRepository.staff.isNotEmpty()){
                        var location = staffViewModel.staffRepository.staff[0].lokation

                    }
                    NavDemo(location)


                }

            }

        }


        // Requirements check
        RequirementsWizardFactory.createEstimoteRequirementsWizard().fulfillRequirements(
            this,
            onRequirementsFulfilled = { startProximityObservation() },
            onRequirementsMissing = displayToastAboutMissingRequirements,
            onError = displayToastAboutError
        )

        staffViewModel.staffRepository.addListener()
        //testFirebaseSetStaff("V2.02")
        //testFirebaseGetStaff("501")
        //testFirebaseGet()

    }

    private fun startProximityObservation() {
        Log.d(FirestoreBeaconConstants.BEACONLOGTAG, "StartObserving")
        proximityObserver = ProximityObserverBuilder(applicationContext, cloudCredentials)
            .onError(displayToastAboutError)
            .withTelemetryReportingDisabled()
            .withAnalyticsReportingDisabled()
            .withEstimoteSecureMonitoringDisabled()
            .withBalancedPowerMode()
            .build()

        val proximityZones = ArrayList<ProximityZone>()
        proximityZones.add(zoneBuild(Lokation.TAG504))
        proximityZones.add(zoneBuild(Lokation.TAG505))
        proximityZones.add(zoneBuild(Lokation.TAG506))

        proximityObservationHandler = proximityObserver.startObserving(proximityZones)
    }

    private fun zoneBuild(tag: String): ProximityZone {
        return ProximityZoneBuilder()
            .forTag(tag)
            .inNearRange()
            .onEnter {
                Log.d(FirestoreBeaconConstants.BEACONLOGTAG, "Enter: ${it.tag}")
                staffViewModel.getStaff(it.tag)
                testFirebaseSetStaff(it.tag)
                //  staffViewModel.setStaff(it.tag)
            }
            .onExit {
                Log.d(FirestoreBeaconConstants.BEACONLOGTAG, "Exit: ${it.tag}")
            }
            .onContextChange {
                Log.d(FirestoreBeaconConstants.BEACONLOGTAG, "Change: ${it}")
                // zoneEventViewModel.updateZoneContexts(it)
            }
            .build()
    }

    // Lambda functions for displaying errors when checking requirements
    private val displayToastAboutMissingRequirements: (List<Requirement>) -> Unit = {
        Toast.makeText(
            this,
            "Unable to start proximity observation. Requirements not fulfilled: ${it.size}",
            Toast.LENGTH_SHORT
        ).show()
    }
    private val displayToastAboutError: (Throwable) -> Unit = {
        Toast.makeText(
            this,
            "Error while trying to start proximity observation: ${it.message}",
            Toast.LENGTH_SHORT
        ).show()
        Log.d(FirestoreBeaconConstants.FIREBASELOGTAG, "${it.message}")
    }

    private fun testFirebaseGetStaff(staffID: String) {
        val docRef = FirebaseFirestore.getInstance().collection(FirestoreBeaconConstants.STAFF)
        docRef.whereEqualTo(FirestoreBeaconConstants.LOKATION, staffID)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(
                        FirestoreBeaconConstants.FIREBASELOGTAG,
                        "Number of documents => ${documents.size()}"
                    )
                    Log.d(
                        FirestoreBeaconConstants.FIREBASELOGTAG,
                        "${document.id} => ${document.data}"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w(
                    FirestoreBeaconConstants.FIREBASELOGTAG,
                    "Error getting documents: ",
                    exception
                )
            }
    }

    private fun testFirebaseSetStaff(lokation: String) {
        //update
        FirebaseFirestore.getInstance().collection("staff").document("S4dSlUesks0z3Q7NlqB8")
            .update(FirestoreBeaconConstants.LOKATION, lokation)
            .addOnSuccessListener {
                Log.d(
                    FirestoreBeaconConstants.FIREBASELOGTAG,
                    "DocumentSnapshot successfully updated!"
                )
            }
            .addOnFailureListener { e ->
                Log.w(
                    FirestoreBeaconConstants.FIREBASELOGTAG,
                    "Error updating document",
                    e
                )
            }
    }


    private fun testFirebaseGet() {
        val docRef = FirebaseFirestore.getInstance().collection(FirestoreBeaconConstants.STAFF)
        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(
                        FirestoreBeaconConstants.FIREBASELOGTAG,
                        "Number of documents => ${documents.size()}"
                    )
                    Log.d(
                        FirestoreBeaconConstants.FIREBASELOGTAG,
                        "${document.id} => ${document.data}"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w(
                    FirestoreBeaconConstants.FIREBASELOGTAG,
                    "Error getting documents: ",
                    exception
                )
            }
    }

}


@Composable
fun NavDemo(location: String){

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "SignInScreen",
    )
    {composable("SignInScreen") {
        SignInScreen(navController = navController)}

        composable("MyButton") {
            MyButton(navController = navController, location = location)
        }
        composable("RødAlarm/{staffLocation}")
        {
            RødAlarm(navController = navController, it.arguments?.getString("staffLocation")?:"Test location")


        }
        composable("modtager")
        {
            modtager(navController = navController)
        }
        composable("modtagerAccept")
        {
            modtagerAccept(navController = navController)
        }
        composable("modtagerAfvis")
        {
            modtagerAfvis(navController = navController)
        }

    }}



@Composable
fun MyButton(navController: NavController, location: String) {

Column(

    Modifier
        .fillMaxWidth()
        .background(dtu.engtech.iabr.stafffirestorebeacons.ui.theme.Gray)
        .absolutePadding(10.dp, 50.dp, 10.dp, 0.dp), horizontalAlignment = Alignment.CenterHorizontally){

    Text(text = "Tryk på knap ud fra niveau af akutsituation", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Hvid)



    Spacer(modifier = Modifier.height(40.dp))
    Box() {
        Button(
            onClick = { navController.navigate("modtager")},
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(backgroundColor = androidx.compose.ui.graphics.Color.Red),
            modifier = Modifier
                .size(150.dp)

        )
        {
            Text(
                text = "Nødknap 1", fontSize = 20.sp, color = Hvid)
            Modifier.padding(12.dp)

        }
    }
    Spacer(modifier = Modifier.height(25.dp))
    Box() {
        Button(
            onClick = { /*TODO*/ },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(backgroundColor = Orange500),
            modifier = Modifier
                .size(150.dp)


        )
        {
            Text(
                text = "Nødknap 2", fontSize = 20.sp, color = Hvid )
            Modifier.padding(12.dp)

        }
    }
    Spacer(modifier = Modifier.height(25.dp))
    Box() {
        Button(
            onClick = { /*TODO*/ },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(backgroundColor = Gul),
            modifier = Modifier
                .size(150.dp)
        ) {
            Text(
                text = "Nødknap 3", fontSize = 20.sp, color = Hvid)
            Modifier.padding(40.dp)

        }
    }

}


}


@Composable
fun RødAlarm(
    navController: NavController,
    //TO DO ÆNDRE HARD CODED
    staffLocation: String = "V2.02",
    modifier: Modifier = Modifier
) {

    Box(
        modifier = Modifier
            .size(180.dp, 300.dp)


    )

    Spacer(modifier = Modifier.height(4.dp))
    Text(
       text = "ALARM: ${staffLocation}" ?: "",
        modifier = Modifier.padding(all = 4.dp),
        style = MaterialTheme.typography.body2
    )

}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SignInScreen(navController: NavController) {
    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var isPasswordVisible by remember {
        mutableStateOf(false)
    }
    val isFormValid by derivedStateOf {
        username.isNotBlank() && password.length >= 7
    }

    Scaffold(backgroundColor = darkgrey) {
 Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
            Card(
                Modifier
                    .weight(2f)
                    .padding(8.dp),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Text(text = "InstantSOS", fontWeight = FontWeight.Bold, fontSize = 32.sp)
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = username,
                            onValueChange = { username = it },
                            label = { Text(text = "Username") },
                            singleLine = true,
                            trailingIcon = {
                                if (username.isNotBlank())
                                    IconButton(onClick = { username = "" }) {
                                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "")
                                    }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(text = "Password") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                           // trailingIcon = {
                                //IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                              //  }
                           // }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {navController.navigate("MyButton")},
                           // enabled = isFormValid,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = "Log In")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = {}) {
                                Text(text = "Sign Up")
                            }
                            //TextButton(onClick = { }) {
                               // Text(text = "Forgot Password?", color =Color.GRAY)
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun modtager(navController: NavController) {
    Column(
        Modifier
            .fillMaxWidth()
            .absolutePadding(10.dp, 50.dp, 10.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(60.dp))
        Box(Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick = { navController.navigate("modtagerAfvis") },
                colors = ButtonDefaults.buttonColors(backgroundColor = Red),
                modifier = Modifier
                    .height(150.dp)
                    .width(300.dp)
                    .border(BorderStroke(5.dp, Black))

            )
            {
                Text(
                    text = "Afvis anmodning",
                    fontSize = 28.sp,
                    color = Hvid,
                    textAlign = TextAlign.Center
                )
                Modifier.padding(12.dp)

            }
        }

        Spacer(modifier = Modifier.height(35.dp))
        Box() {
            /*Button(
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(backgroundColor = Graa),
                modifier = Modifier
                    .height(90.dp)
                    .width(300.dp)
            )
            {
             */
            Text(
                text = "Katrine T. - Stue 306", fontSize = 25.sp, color = dtu.engtech.iabr.stafffirestorebeacons.ui.theme.Black
            )
            Modifier
                .padding(12.dp)
                .height(90.dp)
                .width(300.dp)
                .border(BorderStroke(5.dp, Black))


        }


        Spacer(modifier = Modifier.height(35.dp))
        Box() {
            Button(
                onClick = { navController.navigate("modtagerAccept") },
                colors = ButtonDefaults.buttonColors(backgroundColor = Green),
                modifier = Modifier
                    .height(150.dp)
                    .width(300.dp)
                    .border(BorderStroke(5.dp, Black))

            )
            {
                Text(
                    text = "Accepter anmodning",
                    fontSize = 28.sp,
                    color = Hvid,
                    textAlign = TextAlign.Center
                )
                Modifier.padding(12.dp)

            }
        }
    }

}

@Composable
fun modtagerAccept(navController: NavController) {

    Column(
        Modifier
            .background(Green)
            .fillMaxSize()
            .padding(8.dp)
    ) {

    }

    Column(
        Modifier
            .fillMaxWidth()
            .absolutePadding(10.dp, 50.dp, 10.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(170.dp))
        Box(Modifier.align(Alignment.CenterHorizontally) .border(BorderStroke(11.dp, Hvid))) {
            Button(
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(backgroundColor = Graa),
                modifier = Modifier
                    .height(150.dp)
                    .width(300.dp)

            )
            {
                Text(
                    text = "Anmodning accepteret", fontSize = 30.sp, color = darkgrey, textAlign = TextAlign.Center
                )
                Modifier
                    .padding(12.dp)


            }
        }

    }
}

@Composable
fun modtagerAfvis(navController: NavController) {

    Column(
        Modifier
            .background(Red)
            .fillMaxSize()
            .padding(8.dp)
    ) {

    }

    Column(
        Modifier
            .fillMaxWidth()
            .absolutePadding(10.dp, 50.dp, 10.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(170.dp))
        Box(Modifier.align(Alignment.CenterHorizontally) .border(BorderStroke(11.dp, Hvid))) {
            Button(
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(backgroundColor = Graa),
                modifier = Modifier
                    .height(150.dp)
                    .width(300.dp)

            )
            {
                Text(
                    text = "Anmodning afvist", fontSize = 30.sp, color = darkgrey, textAlign = TextAlign.Center
                )
                Modifier
                    .padding(12.dp)


            }
        }

    }
}






























































































































































































