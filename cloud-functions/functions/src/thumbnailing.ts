import {getStorage} from "firebase-admin/storage";
import * as mupdf from "mupdf";
import * as vips from "wasm-vips";
import {getDocumentFromId} from "./storage.js";
import {logger} from "firebase-functions";

const PDF_CONVERSION_DPI = 250;
const RATIO_WIDTH_HEIGHT = 210 / 297;

/**
 * Get the buffer of a document
 * @param {string} documentId The id of the document
 * @return {ArrayBuffer} The buffer of the document
 */
async function getDocumentBuffer(documentId: string): Promise<ArrayBuffer> {
  const storage = getStorage();
  const bucket = storage.bucket();
  const originalFileRef = bucket.file(documentId);
  const originalFile = (await originalFileRef.download())[0];
  return originalFile.buffer;
}

/**
 * Convert a pdf to a png image
 * @param {ArrayBuffer} buffer The pdf to convert
 * @return {Uint8Array} The png image
 */
function convertPdfToImage(buffer: ArrayBuffer): ArrayBuffer {
  const originalDocument = mupdf.Document.openDocument(buffer, "application/pdf");
  const page = originalDocument.loadPage(0);
  const matrix = mupdf.Matrix.scale(PDF_CONVERSION_DPI / 72, PDF_CONVERSION_DPI / 72);
  const pixmap = page.toPixmap(matrix, mupdf.ColorSpace.DeviceRGB, false, true);
  return pixmap.asPNG().buffer;
}

/**
 * Resize the image to a fill a rectangle with the given width and predefined aspect ratio
 * @param {ArrayBuffer} buffer The image to resize
 * @param {number} width The width of the resized image
 * @return {Uint8Array} The resized image
 */
async function resizeImage(buffer: ArrayBuffer, width: number): Promise<Uint8Array> {
  const Vips = await vips.default();
  const image = Vips.Image.newFromBuffer(buffer);
  logger.debug(`vips loaded size=${image.width}`);
  const resizedImage = image.thumbnailImage(width, {
    height: Math.round(width / RATIO_WIDTH_HEIGHT),
    crop: "entropy",
  });
  return resizedImage.writeToBuffer(".png");
}

/**
 * Save the thumbnail in the storage
 * @param {string} documentId The id of the document
 * @param {ArrayBuffer} buffer The thumbnail
 * @param {number} width The width of the thumbnail
 */
async function saveThumbnail(documentId: string, buffer: ArrayBuffer, width: number): Promise<void> {
  const storage = getStorage();
  const bucket = storage.bucket();
  const thumbFile = bucket.file(`${documentId}-thumb-${width}`);
  await thumbFile.save(new Uint8Array(buffer));
}

/**
 * Generate a thumbnail for a document
 * @param {string} travelId The id of the travel linked
 * @param {string} documentId The id of the document
 * @param {number} width The width of the thumbnail
 */
export async function generateThumbnailForDocument(travelId: string, documentId: string, width: number): Promise<void> {
  const metadata = await getDocumentFromId(travelId, documentId);
  let buffer = await getDocumentBuffer(documentId);
  logger.debug(`Document found title=${metadata.title}`);
  if (metadata.fileFormat === "application/pdf") {
    logger.debug("Converting pdf to image");
    buffer = convertPdfToImage(buffer);
  }
  logger.debug(`Resizing image to ${width}`);
  const resizedImage = await resizeImage(buffer, width);
  logger.debug(`Saving thumbnail documentId=${documentId},width=${width}`);
  await saveThumbnail(documentId, resizedImage, width);
}
