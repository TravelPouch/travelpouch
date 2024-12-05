import {HttpsError, onCall, onRequest} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import {initializeApp} from "firebase-admin/app";

import {
  getAccessToken, getNewMessagesId, parseBodyGmail, parseBodyStore,
  processNewMessagesId, updateVars,
} from "./gmail.js";
import {storeFile} from "./storage.js";
import {generateThumbnailForDocument} from "./thumbnailing.js";

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
