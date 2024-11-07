"use strict";
/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.test = exports.storeDocument = exports.gmailDocuments = void 0;
const https_1 = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const app_1 = require("firebase-admin/app");
const params_1 = require("firebase-functions/params");
const firestore_1 = require("firebase-admin/firestore");
const storage_1 = require("firebase-admin/storage");
// Start writing functions
// https://firebase.google.com/docs/functions/typescript
// export const helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
const REFRESH_TOKEN = (0, params_1.defineSecret)("GMAIL_API_REFRESH_TOKEN");
const CLIENT_ID = (0, params_1.defineSecret)("GMAIL_API_CLIENT_ID");
const CLIENT_SECRET = (0, params_1.defineSecret)("GMAIL_API_CLIENT_SECRET");
const ACCEPTED_MIME_TYPES = [
    "application/pdf",
    "image/jpeg",
    "image/png",
];
(0, app_1.initializeApp)();
const ADDRESS_TO_ID = /^<?travelpouchswent\+(.*)@gmail.com>?$/;
/**
 * This error is used for parsing errors
 */
class ParseError extends Error {
}
/**
 * handle requests from pub/sub and get the documents
 */
exports.gmailDocuments = (0, https_1.onRequest)({ region: "europe-west9" }, async (req, res) => {
    var _a;
    logger.debug(req.headers);
    try {
        const newHistoryId = parseBodyGmail(req.body);
        const accessTokenPromise = getAccessToken();
        const historyId = (await updateVars({ historyId: newHistoryId }))
            .historyId;
        const accessToken = await accessTokenPromise;
        const newMessagesId = await getNewMessagesId(historyId, accessToken);
        logger.debug(newMessagesId);
        for (const newMessageId of newMessagesId) {
            const destAttachments = await getDestAttachment(newMessageId, accessToken);
            logger.debug(destAttachments);
            for (const destAttachment of destAttachments.attachments) {
                const contentBase64 = await getAttachment(newMessageId, destAttachment.id, accessToken);
                const travelId = (_a = ADDRESS_TO_ID.exec(destAttachments.destination)) === null || _a === void 0 ? void 0 : _a.at(1);
                if (travelId == null) {
                    return;
                }
                storeFile(contentBase64, destAttachment.format, destAttachment.title, travelId, destAttachment.size);
            }
        }
    }
    catch (e) {
        logger.error("some error occured", e);
    }
    res.json({ result: "Ok" });
});
exports.storeDocument = (0, https_1.onCall)({ region: "europe-west9" }, async (req) => {
    // if (!req?.auth) {
    //   return { message: "Authentication Required!", code: 401 };
    // }
    try {
        logger.debug(req.data);
        const body = parseBodyStore(req.data);
        await storeFile(body.content, body.fileFormat, body.title, body.travelId, body.fileSize);
    }
    catch (e) {
        logger.error("some error occured", e);
        return { message: "Ohhhhhh nooo", code: 500 };
    }
    return { message: "Oooooookayyy", code: 200 };
});
const mupdf = require("mupdf");
const Vips = require("wasm-vips");
async function convertPdfToJpeg() {
    const doc = mupdf.Document.openDocument(new Uint8Array([0, 0, 0, 0]), "application/pdf");
    logger.debug(doc.countPages());
    const vips = await Vips();
    const mask = vips.Image.newFromArray([
        -1, -1, -1,
        -1, 16, -1,
        -1, -1, -1
    ], 8.0);
    // Finally, write the result to a buffer
    const outBuffer = mask.writeToBuffer('.jpg');
    logger.debug(outBuffer.byteLength);
}
exports.test = (0, https_1.onRequest)({ region: "europe-west9" }, async (req, res) => {
    logger.debug(req.headers);
    convertPdfToJpeg().then(() => {
        res.json({ result: "Ok" });
    }).catch((err) => {
        res.json({ result: "Fuck" });
    });
});
/**
 * Store a file in the cloud
 * @param {string} contentBase64url
 *  The content of the file to sotre in base64url encoding
 * @param {string} format The mime type of the file
 * @param {string} title The name of the file
 * @param {string} travelId The id of the travel linked
 * @param {number} size The size of the file in bytes
 */
async function storeFile(contentBase64url, format, title, travelId, size) {
    const fs = (0, firestore_1.getFirestore)();
    const reference = fs.collection("travels")
        .doc(travelId);
    const document = await fs.collection("documents").add({
        addedAt: firestore_1.Timestamp.now(),
        fileFormat: format,
        fileSize: size,
        title,
        travelRef: reference,
        visibility: "ME",
    });
    const storage = (0, storage_1.getStorage)();
    const bucket = storage.bucket();
    const file = bucket.file(document.id);
    await file.save(Buffer.from(contentBase64url, "base64url"));
    logger.debug("Uploaded the", format, title, "of size", size, "with id", document.id);
}
/**
 * Get the old version of the vars and replace it by the newVars
 * @param {MailProcessingVars} newVars the new version of the vars
 * @return {Promise<MailProcessingVars>} the old version of the vars
 */
async function updateVars(newVars) {
    var _a;
    const fsDocument = (0, firestore_1.getFirestore)().doc("vars/mailProcessing");
    const oldVarsDS = await fsDocument.get();
    const oldVars = {
        historyId: (_a = oldVarsDS.get("historyId")) !== null && _a !== void 0 ? _a : 0,
    };
    await fsDocument.set(newVars);
    return oldVars;
}
/**
 * Check the coerance of the body
 * @param {BodyStore} body The body to check
 * @return {BodyStore} the body checked
 */
function parseBodyStore(body) {
    if ((body === null || body === void 0 ? void 0 : body.fileFormat) == null ||
        body.fileSize == null ||
        body.title == null ||
        body.travelId == null ||
        body.visibility == null ||
        body.content == null) {
        throw new ParseError;
    }
    return body;
}
/**
 * Parse the body to extract the historyId given by pub/sub
 * @param {BodyGmail} body the body of the request
 * @return {number} the historyId
 */
function parseBodyGmail(body) {
    var _a;
    logger.debug(body);
    if (((_a = body === null || body === void 0 ? void 0 : body.message) === null || _a === void 0 ? void 0 : _a.data) != null) {
        const content = JSON.parse(atob(body.message.data));
        logger.debug("[parseBody] parsed content", content);
        if ((content === null || content === void 0 ? void 0 : content.historyId) != null) {
            logger.debug("[parseBody] history id", content.historyId);
            return content.historyId;
        }
    }
    throw new ParseError;
}
/**
 * Retrieve an access token
 * @return {Promise<string>} the access_token in a promise
 */
function getAccessToken() {
    const body = {
        grant_type: "refresh_token",
        refresh_token: REFRESH_TOKEN.value(),
        client_id: CLIENT_ID.value(),
        client_secret: CLIENT_SECRET.value(),
    };
    const request = new Request("https://oauth2.googleapis.com/token", {
        method: "POST",
        body: JSON.stringify(body),
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json",
        },
    });
    return fetch(request)
        .then((res) => res.json())
        .then((res) => {
        logger.debug("Access token fetched");
        return res.access_token;
    });
}
/**
 * Get a list of histories starting with the historyId
 * @param {number} historyId the historyId given by pub/sub
 * @param {string} accessToken the access token to connect to the gmail api
 * @return {Promise<string[]>} the histories in a promise
 */
function getNewMessagesId(historyId, accessToken) {
    const request = new Request("https://gmail.googleapis.com/gmail/v1/users/me/history?maxResults=500&startHistoryId=" + historyId, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json",
            "Authorization": `Bearer ${accessToken}`,
        },
    });
    return fetch(request)
        .then((res) => res.json())
        .then((res) => {
        logger.debug(res);
        return historiesToNewMessagesId(res);
    });
}
/**
 * Read a histories to find the id of the new messages
 * @param {Histories} histories in which to search for histories
 * @return {string[]} the id of the new messages
 */
function historiesToNewMessagesId(histories) {
    const messagesId = [];
    if ((histories === null || histories === void 0 ? void 0 : histories.history) != null && histories.history instanceof Array) {
        histories.history.forEach((v) => {
            if (v.messagesAdded instanceof Array) {
                v.messagesAdded.forEach((v) => {
                    var _a;
                    if (((_a = v.message) === null || _a === void 0 ? void 0 : _a.id) != null) {
                        messagesId.push(v.message.id);
                    }
                });
            }
        });
    }
    return messagesId;
}
/**
 * Get the message and parse it
 * @param {string} messageId The id of the message to get
 * @param {string} accessToken The access token to connect to the gmail api
 * @return {Promise<DestAttachments>} The destination and the attachments id
 */
function getDestAttachment(messageId, accessToken) {
    const request = new Request("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId, {
        method: "GET",
        headers: {
            "Accept": "application/json",
            "Authorization": `Bearer ${accessToken}`,
        },
    });
    return fetch(request)
        .then((res) => res.json())
        .then((res) => {
        return messageToDestAttachment(res);
    });
}
/**
 * Pare the content of a message
 * @param {Message} message The message to parse
 * @return {Promise<DestAttachments>} The destionation and the attachments id
 */
function messageToDestAttachment(message) {
    /**
     * Consolidate all the parts of the message mathing having a mime type
     * in the accepted one.
     * @param {Part} part The part of the message to analyse
     * @return {Part[]} a list of all matching subparts in the part
     */
    function searchAcceptedMimeTypes(part) {
        if (ACCEPTED_MIME_TYPES.includes(part.mimeType)) {
            return [part];
        }
        else if (part.parts != null) {
            return part.parts.flatMap((part) => searchAcceptedMimeTypes(part));
        }
        else {
            return [];
        }
    }
    const headersFiltered = message.payload.headers.filter((h) => h.name == "To");
    if (headersFiltered.length == 0) {
        throw new Error("No destination found in the email");
    }
    return {
        destination: headersFiltered[0].value,
        attachments: searchAcceptedMimeTypes(message.payload)
            .filter((p) => { var _a; return ((_a = p.body) === null || _a === void 0 ? void 0 : _a.attachmentId) != null; })
            .map((p) => {
            return {
                id: p.body.attachmentId,
                format: p.mimeType,
                title: p.filename,
                size: p.body.size,
            };
        }),
    };
}
/**
 * Get the data of the attachment encoded in base64url
 * @param {string} messageId The id of the message in which is the attachment
 * @param {string} attachmentId the id of the attachment
 * @param {string} accessToken The access token to connect to the gmail api
 * @return {Promise<FileContent>} The attachment of the file in base64url
 */
function getAttachment(messageId, attachmentId, accessToken) {
    const request = new Request(`https://gmail.googleapis.com/gmail/v1/users/me/messages/${messageId}/attachments/${attachmentId}`, {
        method: "GET",
        headers: {
            "Accept": "application/json",
            "Authorization": `Bearer ${accessToken}`,
        },
    });
    return fetch(request)
        .then((res) => res.json())
        .then((res) => {
        return res === null || res === void 0 ? void 0 : res.data;
    });
}
//# sourceMappingURL=index.mjs.map