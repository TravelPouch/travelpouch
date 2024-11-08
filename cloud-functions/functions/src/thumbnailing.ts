import {getStorage} from "firebase-admin/storage";
import * as mupdf from "mupdf";

const PDF_CONVERSION_DPI = 250;

/**
 * Convert a pdf to a png image
 * @param {ArrayBuffer} buffer The pdf to convert
 * @return {Uint8Array} The png image
 */
function convertPdfToImage(buffer: ArrayBuffer): Uint8Array {
  const originalDocument = mupdf.Document.openDocument(buffer, "application/pdf");
  const page = originalDocument.loadPage(0);
  const matrix = mupdf.Matrix.scale(PDF_CONVERSION_DPI / 72, PDF_CONVERSION_DPI / 72);
  const pixmap = page.toPixmap(matrix, mupdf.ColorSpace.DeviceRGB, false, true);
  return pixmap.asPNG();
}

/**
 * Convert a pdf to a png image
 * @param {string} documentId The id of the document to convert
 */
export async function convertPdf(documentId: string): Promise<void> {
  const storage = getStorage();
  const bucket = storage.bucket();
  const originalFileRef = bucket.file(documentId);
  const originalFile = (await originalFileRef.download())[0];
  const pngBytes = convertPdfToImage(originalFile.buffer);
  const thumbFile = bucket.file(`${documentId}-thumb`);
  await thumbFile.save(pngBytes);
}


