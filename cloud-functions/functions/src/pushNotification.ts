import {getFirestore} from "firebase-admin/firestore";
import * as admin from "firebase-admin";
import {logger} from "firebase-functions";

/**
 * Fetches notification tokens for a specific user.
 * @param {string} userId The Firebase UID of the user.
 * @return {Promise<string[]>} The list of notification tokens.
 */
export async function fetchNotificationTokens(userId: string): Promise<string[]> {
  const firestore = getFirestore();
  const userDoc = await firestore.collection("userslist").doc(userId).get();

  if (!userDoc.exists) {
    throw new Error(`User with ID ${userId} not found`);
  }

  const userData = userDoc.data();
  return userData?.notificationTokens || [];
}

/**
 * Sends a notification to a list of FCM tokens.
 * @param {string[]} tokens List of FCM tokens to send the notification to.
 * @param {object} payload The notification payload.
 * @return {Promise<admin.messaging.BatchResponse>} The response from FCM.
 */
export async function sendPushNotification(tokens: string[], payload: object): Promise<admin.messaging.BatchResponse> {
  if (tokens.length === 0) {
    logger.warn("No tokens provided for sending the notification");
    return Promise.reject(new Error("No tokens available"));
  }

  return admin.messaging().sendMulticast({
    tokens,
    notification: payload as admin.messaging.NotificationMessagePayload,
  });
}
