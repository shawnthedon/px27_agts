export default function handler(req, res) {
  res.status(200).json({ status: "healthy", service: "px27m-phone-control-gateway", v: "1.0", env: "vercel" });
}
