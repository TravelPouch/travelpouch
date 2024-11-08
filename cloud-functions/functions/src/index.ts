import { onCall, onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import { initializeApp } from "firebase-admin/app";

import { getAccessToken, getNewMessagesId, parseBodyGmail, parseBodyStore, processNewMessagesId, updateVars } from "./gmail.js";
import { storeFile } from "./storage.js";
import { convertPdf } from "./thumbnailing.js";

initializeApp();

/**
 * handle requests from pub/sub and get the documents
 */
export const gmailDocuments = onRequest(
  { region: "europe-west9" },
  async (req, res) => {
    logger.debug(req.headers);
    try {
      const newHistoryId = parseBodyGmail(req.body);
      const historyId = (await updateVars({ historyId: newHistoryId }))
        .historyId;
      const accessToken = await getAccessToken();
      const newMessagesId = await getNewMessagesId(
        historyId,
        accessToken);
      await processNewMessagesId(newMessagesId, accessToken)
    } catch (e) {
      logger.error("some error occured", e);
    }

    res.json({ result: "Ok" });
  }
);

export const storeDocument = onCall(
  { region: "europe-west9" },
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
      return { message: "Ohhhhhh nooo", code: 500 };
    }

    return { message: "Oooooookayyy", code: 200 };
  }
);

export const test = onRequest(
  { region: "europe-west9" },
  async (req, res) => {
    logger.debug(req.headers);

    convertPdf("chartes.pdf").then(() => {
      res.json({ result: "Ok" });
    }).catch((err: string) => {
      res.json({ result: err });
    })
  })


