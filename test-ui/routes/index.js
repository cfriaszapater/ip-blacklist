var express = require("express");
var router = express.Router();
const fetch = require("node-fetch");

/* GET home page. */
router.get("/", async function(req, res) {
  const ip = req.query.ip;
  if (!ip) {
    res.render("index", {});
  } else {
    try {
      const blacklistedResult = await fetchIp(ip);
      res.render("index", {
        ip: ip,
        blacklistedResult: blacklistedResult,
        goodResult: !blacklistedResult
      });
    } catch (err) {
      res.render("index", {
        ip: ip,
        error: err.message
      });
    }
  }
});

async function fetchIp(ip) {
  return await get(backendUrl() + "/ips/" + ip);
}

async function get(url) {
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(res.statusText);
  }
  if (res.status === 204) {
    return;
  } else {
    return await res.json();
  }
}

function backendUrl() {
  const devUrl = "http://localhost:8080";
  return process.env.BACKEND_URL || devUrl;
}
module.exports = router;
