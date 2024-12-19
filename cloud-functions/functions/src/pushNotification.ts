// parts of this file was generated using Github Copilot or ChatGPT

import {getFirestore} from "firebase-admin/firestore";
import {getMessaging} from "firebase-admin/messaging";
import * as logger from "firebase-functions/logger";

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
 * Send push notification using Firebase Cloud Messaging (FCM) to multiple tokens.
 *
 * @param {string[]} tokens - Array of FCM tokens.
 * @param {string} message - Notification message payload.
 * @return {Promise<void>} - Resolves when the notification is sent.
 * @throws Will throw an error if sending notification fails.
 */
export async function sendPushNotification(tokens: string[], message: string): Promise<void> {
  if (tokens.length === 0) {
    logger.warn("No tokens provided for push notification.");
    return;
  }

  const messaging = getMessaging();

  // Construct the MulticastMessage payload
  const multicastMessage = {
    notification: {
      title: "TravelPouch",
      body: message,
    },
    tokens,
  };

  logger.debug("Multicast message: ", multicastMessage);

  try {
    // Send the multicast message
    const response = await messaging.sendEachForMulticast(multicastMessage);

    logger.debug(`Successfully sent notification: ${response.successCount} successful, ${response.failureCount} failed.`);

    // Handle failed tokens
    if (response.failureCount > 0) {
      const failedTokens: string[] = [];
      response.responses.forEach((res, index) => {
        if (!res.success) {
          logger.warn(`Failed to send notification to token: ${tokens[index]} because ${res.error?.message ?? "unknown reason"}`);
          failedTokens.push(tokens[index]);
        }
      });

      logger.warn("Failed tokens:", failedTokens);

      // Optionally: Remove invalid tokens from Firestore
      // Implement token cleanup logic if required
    }
  } catch (err) {
    logger.error("Error sending multicast notification", err);
    throw new Error("Error sending push notification");
  }
}
