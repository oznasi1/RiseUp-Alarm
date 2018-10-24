//Author: Alex Perry
//Date: 16.10.18
//updateUserSongHistory:
// get function: updates the db for users activity after he played the song
// and before he un/liked it
// gets: seconds played, songId
 
const db = require("./init");
const functions = require("firebase-functions");

async function updateUserSongHistory(data, context) {
  let uid = context.auth.uid;
  const update = {
    secondsPlayed: data.sec,
    isRanked: "0",
    songId: data.songId,
    isLiked: "0",
    timestamp: context.timestamp
  };
  var logMsg = {};
  const query = db.ref("users/" + uid).child("history");
  try {
    logMsg = update;
    logMsg["userId"] = uid;
    await query.push(update);
  } catch (err) {
    logMsg["error"] = err +" uid= " + context.auth.uid;
    throw new functions.https.HttpsError("Failed to update history ", logMsg);
  } finally {

    console.log(logMsg);
  }
};

module.exports = updateUserSongHistory;
