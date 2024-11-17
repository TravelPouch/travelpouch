import {DocumentData, getFirestore, Timestamp}
  from "firebase-admin/firestore";
import {getStorage} from "firebase-admin/storage";
import {Document} from "./common.js";
import {logger} from "firebase-functions";

/**
 * Parse a TravelPouch Document from firestore DocumentData
 * @param {DocumentData} doc The data to parse
 * @return {Document} The document
 */
export function parseDocumentFromFirestore(doc: DocumentData): Document {
  const data = doc.data();
  return {
    addedAt: data.addedAt,
    fileFormat: data.fileFormat,
    fileSize: data.fileSize,
    title: data.title,
    travelRef: data.travelRef,
    visibility: data.visibility,
  };
}

/**
 * Get a document from firestore using its id
 * @param {string} travelId The id of the travel linked
 * @param {string} documentId The id of the document
 * @return {Promise<Document>} The document
 */
export async function getDocumentFromId(travelId: string, documentId: string): Promise<Document> {
  const fs = getFirestore();
  const fsDocument = await fs.collection("allTravels").doc(travelId).collection("documents").doc(documentId).get();
  const document = parseDocumentFromFirestore(fsDocument);
  return document;
}

/**
 * Store a file in the cloud
 * @param {string} contentBase64url
 *  The content of the file to sotre in base64url encoding
 * @param {string} format The mime type of the file
 * @param {string} title The name of the file
 * @param {string} travelId The id of the travel linked
 * @param {number} size The size of the file in bytes
 */
export async function storeFile(
  contentBase64url: string,
  format: string,
  title: string,
  travelId: string,
  size: number) {
  const fs = getFirestore();
  const reference = fs.collection("allTravels").doc(travelId);
  const document = await reference.collection("documents").add({
    addedAt: Timestamp.now(),
    fileFormat: format,
    fileSize: size,
    title,
    travelRef: reference,
    visibility: "ME",
  } as Document);

  const storage = getStorage();
  const bucket = storage.bucket();
  const file = bucket.file(document.id);
  await file.save(Buffer.from(contentBase64url, "base64url"));

  logger.debug("Uploaded the", format, title,
    "of size", size,
    "with id", document.id);
}

