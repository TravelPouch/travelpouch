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
