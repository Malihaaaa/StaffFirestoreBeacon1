package dtu.engtech.iabr.stafffirestorebeacons.ui

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dtu.engtech.iabr.stafffirestorebeacons.core.FirestoreBeaconConstants
import dtu.engtech.iabr.stafffirestorebeacons.model.StaffMember

@Composable
fun StaffCardList(

    list: List<StaffMember>,
    modifier: Modifier = Modifier
) {
    Log.d(FirestoreBeaconConstants.FIREBASELOGTAG, "StaffCardList size: ${list.size}")
    LazyColumn(
        modifier = modifier
    ) {
        items(
            items = list
        ) { staffMember ->
            StaffCard(
                staffMember = staffMember
            )
        }
    }
}