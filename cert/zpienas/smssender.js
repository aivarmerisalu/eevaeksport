"use strict";
exports.__esModule = true;
var fs = require("fs");
var https = require("https");
process.argv.forEach(function (val, index, array) {
  console.log(index + ': ' + val);
});
var messageid = process.argv[2];
var receiver = process.argv[3];
var msg = process.argv[4];
var username = "zpienas_https";
var password = "38JebXnd";
var authenticationHeader = "Basic " + new Buffer(username + ":" + password).toString("base64");
var options = {
  hostname: 'sslproxy.telia.lt',
  port: 443,
  path: '/sms6',
  method: 'POST',
  ca: fs.readFileSync('cert/zpienas/sslproxy-2017-CA.crt'),
  key: fs.readFileSync('cert/zpienas/zemaitijos-pienas.key'),
  cert: fs.readFileSync('cert/zpienas/zemaitijos-pienas.crt'),
  passphrase: 'o4XcayYHhVXJ7EIRViGN',
  headers: {
    'Content-Type': 'text/xml',
    'Authorization': authenticationHeader
  },
};
var req = https.request(options, function (res) {
  console.log(res.statusCode);
  res.on('data', function (d) {
    process.stdout.write(d);
  });
});
var data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message>\n <sms type=\"mt\">\n  <destination messageid=\"" + messageid + "\">\n   <address>\n    <number type=\"international\">" + receiver + "</number>\n   </address>\n  </destination>\n  <source>\n    <address>\n      <alphanumeric>ZPienas</alphanumeric>\n    </address>\n  </source>\n  <rsr type=\"all\"/>\n    <ud type=\"text\" encoding=\"unicode\">" + msg + "</ud>\n </sms>\n</message>\n";
req.write(data);
req.end();
