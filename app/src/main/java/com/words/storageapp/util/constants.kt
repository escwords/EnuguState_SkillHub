package com.words.storageapp.util

import com.google.android.gms.common.internal.AccountType
import java.util.ArrayList


class CurrentLocation(
    val longitude: Double?,
    val latitude: Double?,
    val address: ArrayList<String>? = null
)

const val SKILLS_JSON_DATA = "skills.json"
const val USERID = "userID"
const val AccountType = "AccountType"
const val CLIENT = "Client"
const val LABOURER = "Labourer"

object Constants {
    const val SUCCESS_RESULT = 0

    const val FAILURE_RESULT = 1

    private const val PACKAGE_NAME = "com.words.storageapp.util.utilities"

    const val RECEIVER = "INTENT_RECEIVER"

    const val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"
    const val RESULT_LOCALITY = "$PACKAGE_NAME.LOCALITY"

    const val LOCATION_DATA_EXTRA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"

    const val ADDRESS_KEY = "address_key"
    const val ADDRESS_REQUESTED = "address_available"
}