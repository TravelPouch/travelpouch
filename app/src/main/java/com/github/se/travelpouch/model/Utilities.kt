/**
 * Function to check if a Firestore UID is valid. Firestore UID must be 20 characters long and
 * contain only alphanumeric characters.
 *
 * Follows the format specified by random uid generator of firestore
 * [source](https://github.com/firebase/firebase-android-sdk/blob/a024da69daa0a5264b133d9550d1cf068ec2b3ee/firebase-firestore/src/main/java/com/google/firebase/firestore/util/Util.java#L35).
 *
 * @param fsUid Firestore UID to check.
 * @return True if the UID is valid, false otherwise.
 */
fun isValidUid(fsUid: String): Boolean {
    return fsUid.isNotBlank() && fsUid.matches(Regex("^[a-zA-Z0-9]{20}$"))
}