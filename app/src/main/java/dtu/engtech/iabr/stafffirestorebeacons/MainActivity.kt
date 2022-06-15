package dtu.engtech.iabr.stafffirestorebeacons

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import dtu.engtech.iabr.stafffirestorebeacons.ui.theme.StaffFirestoreBeaconsTheme
import com.estimote.proximity_sdk.api.*
import com.google.firebase.firestore.FirebaseFirestore
import dtu.engtech.iabr.stafffirestorebeacons.core.CloudCredentials.APP_ID
import dtu.engtech.iabr.stafffirestorebeacons.core.CloudCredentials.APP_TOKEN
import dtu.engtech.iabr.stafffirestorebeacons.core.FirestoreBeaconConstants
import dtu.engtech.iabr.stafffirestorebeacons.core.Lokation
import dtu.engtech.iabr.stafffirestorebeacons.model.StaffMember
import dtu.engtech.iabr.stafffirestorebeacons.ui.StaffScreen
import dtu.engtech.iabr.stafffirestorebeacons.ui.StaffViewModel

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
                    StaffScreen(staffViewModel)
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
                Log.d(FirestoreBeaconConstants.FIREBASELOGTAG, "${document.id} => ${document.data}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w(FirestoreBeaconConstants.FIREBASELOGTAG, "Error getting documents: ", exception)
        }
}

}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StaffFirestoreBeaconsTheme {
    }
}