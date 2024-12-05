import {getFirestore} from "firebase-admin/firestore";
import {HttpsError, onCall} from "firebase-functions/v2/https";

/**
 * Fetch the notification tokens for a specific user.
 * @param {string} userId The user ID to fetch tokens for.
 * @return {Promise<string[]>} A list of notification tokens.
 */
async function getUserNotificationTokens(userId: string): Promise<string[]> {
  const fs = getFirestore();
  console.log(`Fetching tokens for user ${userId}`);
  const userDoc = await fs.collection("userlist").doc(userId).get();

  if (!userDoc.exists) {
    throw new HttpsError("not-found", "User not found");
  }

  const data = userDoc.data();
  console.log(`Data: ${data}`);
  return data?.notificationTokens || [];
}

/**
 * Cloud function to retrieve notification tokens for a user.
 */
export const fetchNotificationTokens = onCall(
    {region: "europe-west9"},
    async (req) => {
        const userId = req.data?.userId;

        if (!userId) {
            console.error("Request missing userId parameter.");
            throw new HttpsError("invalid-argument", "Missing userId");
        }

        console.log(`Fetching notification tokens for userId: ${userId}`);  // Log userId

        try {
            const tokens = await getUserNotificationTokens(userId);
            console.log(`Fetched ${tokens.length} tokens for userId: ${userId}`);
            return {tokens};
        } catch (err) {
            console.error(`Error fetching tokens for userId ${userId}:`, err);
            throw new HttpsError("internal", "Error fetching tokens");
        }
    }
);
