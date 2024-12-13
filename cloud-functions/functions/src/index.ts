import {HttpsError, onCall, onRequest} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import {initializeApp} from "firebase-admin/app";

import {
  getAccessToken, getNewMessagesId, parseBodyGmail, parseBodyStore,
  processNewMessagesId, updateVars,
} from "./gmail.js";
import {storeFile} from "./storage.js";
import {generateThumbnailForDocument} from "./thumbnailing.js";

import {fetchNotificationTokens, sendPushNotification} from "./pushNotification.js";

initializeApp();

/**
 * handle requests from pub/sub and get the documents
 */
export const gmailDocuments = onRequest({region: "europe-west9"}, async (req, res) => {
  logger.debug(req.headers);
  try {
    const newHistoryId = parseBodyGmail(req.body);
    const historyId = (await updateVars({historyId: newHistoryId}))
      .historyId;
    const accessToken = await getAccessToken();
    const newMessagesId = await getNewMessagesId(
      historyId,
      accessToken);
    await processNewMessagesId(newMessagesId, accessToken);
  } catch (e) {
    logger.error("some error occured", e);
  }

  res.json({result: "Ok"});
}
);

export const storeDocument = onCall(
  {region: "europe-west9"},
  async (req) => {
    // if (!req?.auth) {
    //   return { message: "Authentication Required!", code: 401 };
    // }
    try {
      logger.debug(req.data);
      const body = parseBodyStore(req.data);
      await storeFile(body.content, body.fileFormat,
        body.title, body.travelId, body.fileSize);
    } catch (e) {
      logger.error("some error occured", e);
      return {success: false, code: 500};
    }

    return {success: true, code: 200};
  }
);

export const generateThumbnailCall = onCall(
  {region: "europe-west9", memory: "1GiB"},
  async (req) => {
    if (!req.data.travelId || !req.data.documentId || !req.data.width) {
      throw new HttpsError("invalid-argument", "Missing parameters");
    }
    try {
      await generateThumbnailForDocument(req.data.travelId, req.data.documentId, req.data.width);
    } catch (err) {
      throw new HttpsError("internal", "Internal error while generating thumbnail");
    }
    return {success: true};
  });

export const generateThumbnailHttp = onRequest(
  {region: "europe-west9", memory: "1GiB"},
  async (req, res) => {
    if (!req.body.travelId || !req.body.documentId || !req.body.width) {
      res.status(400).json({success: false, message: "Missing parameters"});
      return;
    }
    try {
      await generateThumbnailForDocument(req.body.travelId, req.body.documentId, req.body.width);
    } catch (err) {
      res.status(500).json({success: false, message: err});
      return;
    }
    res.json({success: true});
  });


export const sendNotification = onCall(
  {region: "europe-west9"},
  async (req) => {
    if (!req.data.userId || !req.data.message) {
      throw new HttpsError("invalid-argument", "Missing parameters: userId or message");
    }
    try {
      const userId = req.data.userId;
      const message = req.data.message;

      const tokens = await fetchNotificationTokens(userId);
      if (tokens.length === 0) {
        logger.warn(`No notification tokens found for user: ${userId}`);
        return {success: false, message: "No tokens found"};
      }

      await sendPushNotification(tokens, message);
      return {success: true, message: "Notification sent successfully"};
    } catch (err) {
      logger.error("Error sending notification", err);
      throw new HttpsError("internal", "Error sending notification");
    }
  });

export const sendNotificationHttp = onRequest(
  {region: "europe-west9"},
  async (req, res) => {
    if (!req.body.userId || !req.body.message) {
      res.status(400).json({success: false, message: "Missing parameters"});
      return;
    }
    try {
      const userId = req.body.userId;
      const message = req.body.message;

      const tokens = await fetchNotificationTokens(userId);
      if (tokens.length === 0) {
        logger.warn(`No notification tokens found for user: ${userId}`);
        res.status(400).json({success: false, message: "No tokens found"});
        return;
      }

      await sendPushNotification(tokens, message);
    } catch (err) {
      logger.error("Error sending notification", err);
      res.status(500).json({error: "internal", message: "Error sending notification"});
      return;
    }
    res.json({success: true});
  });
