import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(__dirname, "..");
const target = path.resolve(rootDir, "packages/phone-control-protocol");
const destRoot = path.resolve(rootDir, "node_modules/@px27m/phone-control-protocol");
const destApp = path.resolve(rootDir, "apps/gateway/node_modules/@px27m/phone-control-protocol");

for (const dest of [destRoot, destApp]) {
  try {
    fs.mkdirSync(path.dirname(dest), { recursive: true });
    if (!fs.existsSync(dest)) {
      fs.symlinkSync(target, dest, "junction");
    }
  } catch (err) {
    // Ignore if already linked
  }
}
