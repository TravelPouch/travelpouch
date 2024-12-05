import * as functions from "firebase-functions";
import { getFirestore } from "firebase-admin/firestore";
import * as admin from "firebase-admin";

// Initialize Firebase Admin if not already done
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = getFirestore();

/**
 * Fetches the notification tokens for a given user ID from Firestore.
 * @param {string} userId - The Firestore user document ID.
 * @returns {Promise<string[]>} - A promise resolving to a list of notification tokens.
 */
export const getNotificationTokens = functions.https.onCall(async (data, context) => {
  // Check for proper authentication
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Request not authenticated.");
  }

  const userId = data.userId;
  if (!userId) {
    throw new functions.https.HttpsError("invalid-argument", "The function must be called with a userId.");
  }

  try {
    // Fetch the user's document from Firestore
    const userDoc = await db.collection("userslist").doc(userId).get();

    if (!userDoc.exists) {
      throw new functions.https.HttpsError("not-found", "User document does not exist.");
    }

    const userData = userDoc.data();
    const tokens = userData?.notificationToken || [];

    if (!Array.isArray(tokens)) {
      throw new functions.https.HttpsError("internal", "Invalid token format in Firestore.");
    }

    return { tokens };
  } catch (error) {
    functions.logger.error("Error fetching notification tokens:", error);
    throw new functions.https.HttpsError("internal", "An error occurred while fetching tokens.");
  }
});
