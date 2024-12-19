// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
import {Timestamp, DocumentReference} from "firebase-admin/firestore";

/**
 * This error is used for parsing errors
 */
export class ParseError extends Error { }

export interface Document {
    addedAt: Timestamp,
    fileFormat: string,
    fileSize: number,
    title: string,
    travelRef: DocumentReference,
    visibility: string
}
