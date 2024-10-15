/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {onRequest} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";

import {initializeApp} from "firebase-admin/app";
// import {getFirestore} from "firebase-admin/firestore";

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// export const helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

initializeApp();

interface Body {
  data: string,
  messageId: number,
  publishTime: Date
}

interface DataHistoryId {
  historyId: number,
  emailAddress: string
}

export const gmailDocuments = onRequest(
  {region: "europe-west9"},
  async (req, res) => {
    const body: Body = req.body;
    logger.debug(body);
    if (body?.data !== undefined) {
      const content: DataHistoryId = JSON.parse(atob(body.data));
      logger.debug(content);
      if (content?.historyId !== undefined) {
        logger.debug(content.historyId);
      }
    }
    // const writeResult = await getFirestore()
    //   .collection("message")
    //   .add({original: original});
    // res.json({result: `Message with id ${writeResult.id} added.`});
    res.json({result: "Ok"});
  });