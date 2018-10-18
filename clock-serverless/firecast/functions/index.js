const functions = require("firebase-functions");

const getSongUrl = require("./functions/getSongUrl");
exports.getSongUrl = functions.https.onCall(getSongUrl);

const updateSongScore = require("./functions/updateSongScore");
exports.updateSongScore = functions.https.onCall(updateSongScore);

const updateUserSongHistory = require("./functions/updateUserSongHistory");
exports.updateUserSongHistory = functions.https.onCall(updateUserSongHistory)
