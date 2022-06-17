package dtu.engtech.iabr.stafffirestorebeacons.model

import android.util.Log
import androidx.compose.animation.core.snap
import androidx.compose.runtime.toMutableStateList
import com.google.firebase.firestore.FirebaseFirestore
import dtu.engtech.iabr.stafffirestorebeacons.core.FirestoreBeaconConstants
import dtu.engtech.iabr.stafffirestorebeacons.core.FirestoreBeaconConstants.FIREBASELOGTAG

class StaffRepositoryFirestore : StaffRepository {
    override var staff = mutableListOf<StaffMember>().toMutableStateList()

    override fun getStaffMember(staffID: String) {
        val docRef = FirebaseFirestore.getInstance().collection(FirestoreBeaconConstants.STAFF)
        docRef.whereEqualTo(FirestoreBeaconConstants.LOKATION, staffID)
            .get()
            .addOnSuccessListener { documents ->
                staff = documents.toObjects(StaffMember::class.java).toMutableStateList()
                logCatStaff("Lokation opdateret")
                for (document in documents) {
                    Log.d(
                        FirestoreBeaconConstants.FIREBASELOGTAG,
                        "${document.id} => ${document.data}"
                    )
                }
            }
    }

    override fun addListener() {
        FirebaseFirestore.getInstance().collection(FirestoreBeaconConstants.STAFF)
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    Log.w(FirestoreBeaconConstants.FIREBASELOGTAG, "Listen failed.", e)
                    //return@addSnapshotListener
                }

                if (snapshot != null) {
                    staff = snapshot.toObjects(StaffMember::class.java).toMutableStateList()
                    logCatStaff("Lokation fra databasen")

                } else {
                    Log.d(FirestoreBeaconConstants.FIREBASELOGTAG, "Current data: null")
                }
            }
    }


    private fun logCatStaff(comment: String) {
        for (staffMember in staff) {
            Log.d(FirestoreBeaconConstants.FIREBASELOGTAG, "$comment: $staffMember")
        }
    }
}
