import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, "..");

const srcHtml = path.join(rootDir, "apps", "gateway", "public", "index.html");
const destRootHtml = path.join(rootDir, "index.html");
const destPublicDir = path.join(rootDir, "public");
const destPublicHtml = path.join(destPublicDir, "index.html");

if (fs.existsSync(srcHtml)) {
  if (!fs.existsSync(destPublicDir)) {
    fs.mkdirSync(destPublicDir, { recursive: true });
  }
  const content = fs.readFileSync(srcHtml, "utf8");
  fs.writeFileSync(destRootHtml, content, "utf8");
  fs.writeFileSync(destPublicHtml, content, "utf8");
  console.log("Successfully built static dashboard assets to /index.html and /public/index.html");
} else {
  console.error("Source index.html not found at:", srcHtml);
  process.exit(1);
}
